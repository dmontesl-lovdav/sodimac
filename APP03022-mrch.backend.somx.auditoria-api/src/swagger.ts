// src/swagger.ts
import { Router, type Request, type Response, type NextFunction } from 'express';
import swaggerUi from 'swagger-ui-express';
import type { Express } from 'express';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import type { OpenAPIV2 } from 'openapi-types';

import { buildOpenAPISpec } from './docs/openapi.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Extensión de Request para cargar el spec v2 temporalmente
declare module 'express-serve-static-core' {
    interface Request {
        __swaggerV2__?: OpenAPIV2.Document;
    }
}

export async function mountSwagger(app: Express): Promise<void> {
    const r = Router();
    const oas3 = await buildOpenAPISpec();

    // JSON OAS3
    r.get('/openapi.json', (_req: Request, res: Response) => {
        res.setHeader('Content-Type', 'application/json');
        res.send(oas3);
    });

    // JSON Swagger 2.0 (archivo generado en src/docs/swagger.json)
    r.get('/swagger.json', (_req: Request, res: Response) => {
        const p = path.join(__dirname, 'docs', 'swagger.json'); // ⟵ apunta a src/docs
        if (fs.existsSync(p)) {
            res.sendFile(p);
        } else {
            res.status(404).json({ error: 'swagger.json not found. Run: npm run openapi:gen' });
        }
    });

    // UI principal con OAS3
    r.use('/docs', swaggerUi.serve, swaggerUi.setup(oas3, { explorer: true }));

    // UI secundaria leyendo el archivo swagger.json (v2)
    r.use(
        '/docs-v2',
        (req: Request, res: Response, next: NextFunction) => {
            const p = path.join(__dirname, 'docs', 'swagger.json'); // ⟵ src/docs
            if (!fs.existsSync(p)) {
                return res.status(404).send('swagger.json not found. Run: npm run openapi:gen');
            }
            const raw = fs.readFileSync(p, 'utf-8');
            req.__swaggerV2__ = JSON.parse(raw) as OpenAPIV2.Document;
            return next();
        },
        swaggerUi.serve,
        (req: Request, res: Response, next: NextFunction) => {
            const spec = req.__swaggerV2__;
            return swaggerUi.setup(spec, { explorer: true })(req, res, next);
        }
    );

    app.use(r);
}
