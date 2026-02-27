// src/server.ts
import 'dotenv/config';
import app from './app.js';
import { initDataSource } from './config/typeorm-datasource.js';
import { mountSwagger } from './swagger.js';

// Puerto
const port = Number(process.env.PORT ?? process.env.APP_PORT ?? 3711);

// Trust proxy (opcional y seguro)
const TRUST_PROXY = (process.env.TRUST_PROXY ?? '').trim();
if (TRUST_PROXY) {
    app.set('trust proxy', /^\d+$/.test(TRUST_PROXY) ? Number(TRUST_PROXY) : TRUST_PROXY);
} else {
    app.set('trust proxy', process.env.NODE_ENV === 'production' ? 1 : 0);
}

let server: import('http').Server;

async function bootstrap() {
    try {
        // DB primero
        await initDataSource();

        // Swagger después (async)
        await mountSwagger(app);

        // Luego arrancamos HTTP
        server = app.listen(port, '0.0.0.0', () => {
            console.log(`API listening on http://localhost:${port}`);
        });
    } catch (err) {
        console.error("CAN'T INITIALIZE SERVER", err);
        process.exit(1);
    }
}

bootstrap();

// Graceful shutdown
const shutDown = (signal: string) => {
    console.log(`${signal} received. Shutting down...`);
    if (server) {
        server.close(() => {
            console.log('HTTP server closed');
            process.exit(0);
        });
    } else {
        process.exit(0);
    }
};

process.on('SIGTERM', () => shutDown('SIGTERM'));
process.on('SIGINT', () => shutDown('SIGINT'));
