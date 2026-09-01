import { open, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import { runRebateSyncPreview } from '../services/rebateSync.service.js';

const MAX_INPUT_BYTES = 10 * 1024 * 1024;

function argumentsForPreview(args: string[]) {
    const allowedFlags = new Set(['--run', '--dry-run']);
    const allowedValues = new Set(['--input', '--output', '--page-size']);
    const flags = new Set<string>();
    const values = new Map<string, string>();
    for (let index = 0; index < args.length; index++) {
        const arg = args[index];
        if (arg === undefined) continue;
        if (flags.has(arg) || values.has(arg)) throw new Error(`Argumento repetido: ${arg}`);
        if (allowedFlags.has(arg)) { flags.add(arg); continue; }
        if (!allowedValues.has(arg)) throw new Error('Argumento no reconocido; usar --run --dry-run --input --output [--page-size]');
        const value = args[++index];
        if (!value || value.startsWith('--')) throw new Error(`Falta valor de ${arg}`);
        values.set(arg, value);
    }
    if (!flags.has('--run')) throw new Error('Se requiere --run');
    if (!flags.has('--dry-run')) {
        throw new Error('REBATE_SYNC_LIVE_NOT_IMPLEMENTED: faltan adaptadores SQL, autenticación e idempotencia FBC. Usar --dry-run.');
    }
    const input = values.get('--input');
    const output = values.get('--output');
    if (!input || !output) throw new Error('Se requieren --input y --output');
    if (resolve(input) === resolve(output)) throw new Error('El reporte no puede reemplazar la entrada');
    const size = values.get('--page-size') ?? '100';
    if (!/^\d+$/.test(size)) throw new Error('--page-size debe ser entero');
    return { input: resolve(input), output: resolve(output), pageSize: Number(size) };
}

/** Independiente de server.ts, app.ts y del DataSource PostgreSQL compartido. */
export async function executeRebateSync(args: string[], signal?: AbortSignal): Promise<number> {
    const config = argumentsForPreview(args);
    signal?.throwIfAborted();
    const handle = await open(config.input, 'r');
    let content: string;
    try {
        const stat = await handle.stat();
        if (!stat.isFile() || stat.size > MAX_INPUT_BYTES) throw new Error('Entrada: archivo regular, máximo 10 MiB');
        content = await handle.readFile({ encoding: 'utf8' });
        if (Buffer.byteLength(content, 'utf8') > MAX_INPUT_BYTES) throw new Error('Entrada demasiado grande');
    } finally {
        await handle.close();
    }
    let input: unknown;
    try { input = JSON.parse(content.replace(/^\uFEFF/, '')); }
    catch { throw new Error('Entrada JSON inválida'); }
    const report = await runRebateSyncPreview(input, { pageSize: config.pageSize, ...(signal ? { signal } : {}) });
    signal?.throwIfAborted();
    // Nunca sobrescribe archivos; el reporte contiene datos comerciales.
    await writeFile(config.output, JSON.stringify(report, null, 2) + '\n', { flag: 'wx', mode: 0o600 });
    console.log(JSON.stringify({
        event: 'REBATE_SYNC_PREVIEW_FINISHED', executionId: report.executionId,
        mode: report.mode, totals: report.totals, readyAmount: report.readyAmount,
        durationMs: report.durationMs,
    }));
    return report.totals.INVALID + report.totals.CONFLICT > 0 ? 2 : 0;
}

const invokedDirectly = process.argv[1] !== undefined &&
    import.meta.url === pathToFileURL(resolve(process.argv[1])).href;

if (invokedDirectly) {
    const controller = new AbortController();
    let signalExitCode: number | undefined;
    const onInt = () => { signalExitCode = 130; controller.abort(new Error('Interrumpido por SIGINT')); };
    const onTerm = () => { signalExitCode = 143; controller.abort(new Error('Interrumpido por SIGTERM')); };
    process.once('SIGINT', onInt);
    process.once('SIGTERM', onTerm);
    executeRebateSync(process.argv.slice(2), controller.signal)
        .then((code) => { process.exitCode = signalExitCode ?? code; })
        .catch((error: unknown) => {
            console.error(JSON.stringify({ event: 'REBATE_SYNC_ERROR', message: error instanceof Error ? error.message : 'Error no identificado' }));
            process.exitCode = signalExitCode ?? 1;
        })
        .finally(() => {
            process.removeListener('SIGINT', onInt);
            process.removeListener('SIGTERM', onTerm);
        });
}