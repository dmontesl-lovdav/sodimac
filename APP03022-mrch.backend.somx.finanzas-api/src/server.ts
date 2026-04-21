import 'dotenv/config';
import http from 'http';
import app from './app.js';
import { initDataSource } from './config/typeorm-datasource.js';

const port = Number(process.env.PORT ?? process.env.APP_PORT ?? 3711);
const maxHeaderSize = Number(process.env.MAX_HEADER_SIZE ?? 65536);

const TRUST_PROXY = (process.env.TRUST_PROXY ?? '').trim();
if (TRUST_PROXY) {
    app.set('trust proxy', /^\d+$/.test(TRUST_PROXY) ? Number(TRUST_PROXY) : TRUST_PROXY);
} else {
    app.set('trust proxy', process.env.NODE_ENV === 'production' ? 1 : 0);
}

let server: import('http').Server;

async function bootstrap() {
    try {
        await initDataSource();

        console.log("PORT ENV:", process.env.PORT);
        console.log("APP_PORT ENV:", process.env.APP_PORT);
        console.log("Resolved port:", port);
        console.log("MAX_HEADER_SIZE:", maxHeaderSize);

        server = http.createServer({ maxHeaderSize }, app);

        server.listen(port, '0.0.0.0', () => {
            console.log(`API listening on http://localhost:${port}`);
        });
    } catch (err) {
        console.error("CAN'T INITIALIZE SERVER", err);
        process.exit(1);
    }
}

bootstrap();

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