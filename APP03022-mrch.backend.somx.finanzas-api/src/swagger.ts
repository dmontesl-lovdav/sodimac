import swaggerUi from 'swagger-ui-express';
import type { Express } from 'express';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export async function mountSwagger(app: Express): Promise<void> {

    const spec = JSON.parse(
        fs.readFileSync(
            path.join(__dirname, 'docs', 'openapi.json'),
            'utf-8'
        )
    );

    // JSON
    app.get('/docs/openapi.json', (_req, res) => {
        res.json(spec);
    });

    // 👇 ESTA ES LA CLAVE
    app.use(
        '/docs',
        swaggerUi.serve,
        swaggerUi.setup(spec, {
            explorer: true,
            swaggerOptions: {
                url: '/docs/openapi.json'
            }
        })
    );
}
