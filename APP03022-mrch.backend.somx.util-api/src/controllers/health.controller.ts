// src/controllers/health.controller.ts
import type { Request, Response } from 'express';
import { getDataSource } from '@/config/typeorm-datasource.js';

/**
 * Health check endpoint similar a Spring Boot Actuator
 * Retorna el estado de salud de la aplicacion incluyendo conectividad de BD
 */
export async function healthCheck(_req: Request, res: Response) {
    const startTime = Date.now();

    const health: {
        status: 'UP' | 'DOWN';
        timestamp: string;
        service: string;
        environment: string;
        checks: {
            database?: {
                status: 'UP' | 'DOWN';
                responseTime?: number;
                error?: string;
            };
            application: {
                status: 'UP';
                uptime: number;
                memory: {
                    used: string;
                    total: string;
                };
            };
        };
    } = {
        status: 'UP',
        timestamp: new Date().toISOString(),
        service: 'utils-api',
        environment: process.env.NODE_ENV ?? 'development',
        checks: {
            application: {
                status: 'UP',
                uptime: process.uptime(),
                memory: {
                    used: `${Math.round(process.memoryUsage().heapUsed / 1024 / 1024)}MB`,
                    total: `${Math.round(process.memoryUsage().heapTotal / 1024 / 1024)}MB`
                }
            }
        }
    };

    // Check database connectivity
    try {
        const dbCheckStart = Date.now();
        const dataSource = getDataSource();

        if (dataSource?.isInitialized) {
            await dataSource.query('SELECT 1');
            const dbCheckEnd = Date.now();

            health.checks.database = {
                status: 'UP',
                responseTime: dbCheckEnd - dbCheckStart
            };
        } else {
            health.checks.database = {
                status: 'DOWN',
                error: 'Database not initialized'
            };
            health.status = 'DOWN';
        }
    } catch (error) {
        health.checks.database = {
            status: 'DOWN',
            error: error instanceof Error ? error.message : 'Unknown database error'
        };
        health.status = 'DOWN';
    }

    const responseTime = Date.now() - startTime;
    const statusCode = health.status === 'UP' ? 200 : 503;

    res.status(statusCode).json({
        ...health,
        responseTime: `${responseTime}ms`
    });
}

/**
 * Liveness probe - solo verifica si la aplicacion esta corriendo
 * Adecuado para Kubernetes liveness probe
 */
export function livenessProbe(_req: Request, res: Response) {
    res.status(200).json({
        status: 'UP',
        timestamp: new Date().toISOString()
    });
}

/**
 * Readiness probe - verifica si la aplicacion esta lista para recibir trafico
 * Adecuado para Kubernetes readiness probe
 */
export async function readinessProbe(_req: Request, res: Response) {
    try {
        const dataSource = getDataSource();

        if (dataSource?.isInitialized) {
            await dataSource.query('SELECT 1');
            res.status(200).json({
                status: 'UP',
                timestamp: new Date().toISOString()
            });
        } else {
            res.status(503).json({
                status: 'DOWN',
                timestamp: new Date().toISOString(),
                reason: 'Database not initialized'
            });
        }
    } catch (error) {
        res.status(503).json({
            status: 'DOWN',
            timestamp: new Date().toISOString(),
            reason: error instanceof Error ? error.message : 'Unknown error'
        });
    }
}
