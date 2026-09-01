// Verificación aislada compatible con Node 20.19.4 y TypeScript del proyecto.
// No instala dependencias, no conecta bases, no envía HTTP, no sustituye tsc.
import assert from 'node:assert/strict';
import ts from 'typescript';
import { mkdtemp, mkdir, readFile, writeFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join, dirname } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { spawnSync } from 'node:child_process';

const root = fileURLToPath(new URL('../', import.meta.url));
const temp = await mkdtemp(join(tmpdir(), 'rebate-sync-test-'));
let passed = 0;
try {
    await writeFile(join(temp, 'package.json'), '{"type":"module"}');
    for (const file of ['services/rebateSync.service', 'workers/rebateSync.batch']) {
        const source = await readFile(join(root, 'src', file + '.ts'), 'utf8');
        const output = join(temp, file + '.js');
        await mkdir(dirname(output), { recursive: true });
        const compiled = ts.transpileModule(source, {
            fileName: file + '.ts',
            reportDiagnostics: true,
            compilerOptions: {
                target: ts.ScriptTarget.ES2022,
                module: ts.ModuleKind.ESNext,
            },
        });
        const errors = (compiled.diagnostics ?? []).filter(
            (diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error
        );
        if (errors.length > 0) {
            throw new Error(errors.map((diagnostic) =>
                ts.flattenDiagnosticMessageText(diagnostic.messageText, '\n')
            ).join('\n'));
        }
        await writeFile(output, compiled.outputText);
    }
    const svc = await import(pathToFileURL(join(temp, 'services/rebateSync.service.js')).href);
    const fixture = JSON.parse(await readFile(join(root, 'src/examples/rebate-sync.input.json'), 'utf8'));
    const base = structuredClone(fixture.records[0]);
    const run = (records, opts) => svc.runRebateSyncPreview({ rules: fixture.rules, records }, opts);
    async function check(name, fn) { await fn(); passed++; console.log(`OK ${name}`); }

    await check('ejemplo: un listo, un repetido, uno no contabilizado; cero envíos', async () => {
        const result = await svc.runRebateSyncPreview(fixture, { pageSize: 1 });
        assert.deepEqual(result.totals, { read: 3, pages: 3, sent: 0, READY: 1, EXCLUDED: 1, DUPLICATE: 1, INVALID: 0, CONFLICT: 0 });
        assert.equal(result.readyAmount, '5800.00');
        assert.deepEqual(result.items[0].payload, { documentNumber: 'DEMO-REBATE-001', referenceNumber: 'DEMO-ACUERDO-001', sapDocument: 'DEMO-SAP-001', vendorNumber: 34786, amount: '5800.00', source: 1, periodId: 202607, dueDate: '2026-07-31', postingDate: '2026-07-02', status: 1 });
    });
    await check('precisión exacta de centavos y límite numeric(16,2)', async () => {
        assert.equal(svc.normalizeRebateAmount('99999999999999.99'), '99999999999999.99');
        const a = structuredClone(base), b = structuredClone(base); a.data.IMPORTE = '0.10'; b.data.IMPORTE = '0.20'; b.sourceKey = 'other';
        assert.equal((await run([a, b])).readyAmount, '0.30');
        for (const invalid of ['100000000000000.00', '-1.00', '1.001', '1e2', '1,000.00', 100]) assert.throws(() => svc.normalizeRebateAmount(invalid));
    });
    await check('rechaza fechas imposibles y timestamps ambiguos', () => {
        for (const value of ['2026-02-30', '2026-13-01', '2026-07-01T00:00:00Z']) assert.throws(() => svc.transformRebate({ ...base.data, FECHA_RECEPCION: value }));
    });
    await check('no deduce éxito SAP de DOC_SAP', async () => {
        const result = await run([{ ...base, sapPosted: 'true' }, { ...base, sapPosted: false }]);
        assert.equal(result.totals.INVALID, 1); assert.equal(result.totals.EXCLUDED, 1); assert.equal(result.totals.READY, 0);
    });
    await check('filtros de cuenta, fecha y frontera inclusiva', async () => {
        const result = await run([{ ...base, eligibilityDate: '2026-06-30' }, { ...base, account: 'other' }, { ...base, eligibilityDate: '2026-07-01' }]);
        assert.equal(result.totals.EXCLUDED, 2); assert.equal(result.totals.READY, 1);
    });
    await check('misma llave con distinto contenido invalida ambas ocurrencias', async () => {
        const different = structuredClone(base); different.data.IMPORTE = '100.00';
        const result = await run([base, different], { pageSize: 1 });
        assert.equal(result.totals.CONFLICT, 2); assert.equal(result.totals.READY, 0); assert.equal(result.readyAmount, '0.00');
    });
    await check('documento repetido con distinta llave no se elimina', async () => {
        assert.equal((await run([base, { ...base, sourceKey: 'other' }])).totals.READY, 2);
    });
    await check('continúa tras registro inválido', async () => {
        const result = await run([null, base]); assert.equal(result.totals.INVALID, 1); assert.equal(result.totals.READY, 1);
    });
    await check('enteros inválidos, vacíos y fuera de rango', () => {
        for (const value of ['', true, '12x', '1.5', 2147483648]) assert.throws(() => svc.transformRebate({ ...base.data, CODIGO_PROVEEDOR: value }));
    });
    await check('configuración inválida falla; entrada vacía es válida', async () => {
        await assert.rejects(run([base], { pageSize: 0 }));
        await assert.rejects(svc.runRebateSyncPreview({ rules: { subtotalAccounts: [], fromDate: '2026-07-01' }, records: [] }));
        assert.equal((await run([])).totals.read, 0);
    });
    await check('cancelación', async () => {
        const controller = new AbortController(); controller.abort(); await assert.rejects(run([base], { signal: controller.signal }));
    });
    await check('plan de reintentos: dos a una hora; tercero agotado', () => {
        const now = new Date('2026-07-01T11:00:00Z');
        assert.equal(svc.nextRebateRetryAt(1, now).toISOString(), '2026-07-01T12:00:00.000Z');
        assert.equal(svc.nextRebateRetryAt(2, now).toISOString(), '2026-07-01T12:00:00.000Z');
        assert.equal(svc.nextRebateRetryAt(3, now), null); assert.throws(() => svc.nextRebateRetryAt(0, now));
    });
    const worker = join(temp, 'workers/rebateSync.batch.js');
    const input = join(root, 'src/examples/rebate-sync.input.json');
    const output = join(temp, 'report.json');
    const invoke = (args) => spawnSync(process.execPath, [worker, ...args], { encoding: 'utf8' });
    await check('CLI produce reporte; segunda ejecución no sobrescribe', async () => {
        const args = ['--run', '--dry-run', '--input', input, '--output', output];
        const first = invoke(args); assert.equal(first.status, 0, first.stderr);
        const original = await readFile(output, 'utf8'); assert.equal(JSON.parse(original).totals.sent, 0);
        assert.equal(invoke(args).status, 1); assert.equal(await readFile(output, 'utf8'), original);
    });
    await check('CLI bloquea modo real y opciones erróneas', () => {
        const result = invoke(['--run']); assert.equal(result.status, 1); assert.match(result.stderr, /LIVE_NOT_IMPLEMENTED/);
        assert.equal(invoke(['--run', '--dry-run', '--foo']).status, 1);
    });
    await check('CLI devuelve código 2 con conflictos y guarda evidencia', async () => {
        const conflict = structuredClone(base); conflict.data.IMPORTE = '1.00';
        const src = join(temp, 'conflicts.json'), dst = join(temp, 'conflicts-report.json');
        await writeFile(src, JSON.stringify({ rules: fixture.rules, records: [base, conflict] }));
        assert.equal(invoke(['--run', '--dry-run', '--input', src, '--output', dst]).status, 2);
        assert.equal(JSON.parse(await readFile(dst, 'utf8')).totals.CONFLICT, 2);
    });
    console.log(`${passed} pruebas aprobadas. Sin conexiones externas.`);
} finally { await rm(temp, { recursive: true, force: true }); }
