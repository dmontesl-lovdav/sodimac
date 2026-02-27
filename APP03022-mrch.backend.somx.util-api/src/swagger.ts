// src/swagger.ts
import { Router, type Request, type Response } from 'express';
import swaggerUi from 'swagger-ui-express';
import type { Express } from 'express';

import { buildOpenAPISpec } from './docs/openapi.js';

export async function mountSwagger(app: Express): Promise<void> {
    const r = Router();
    const oas3 = await buildOpenAPISpec();

    // JSON OAS3
    r.get('/openapi.json', (_req: Request, res: Response) => {
        res.setHeader('Content-Type', 'application/json');
        res.send(oas3);
    });

    // UI principal con OAS3
    r.use('/docs', swaggerUi.serve, swaggerUi.setup(oas3, { explorer: true }));

    app.use(r);
}
