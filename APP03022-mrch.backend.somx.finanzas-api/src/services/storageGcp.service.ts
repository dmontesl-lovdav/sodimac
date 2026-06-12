import type { Request, Response, NextFunction } from "express";
import { StatusCodes } from "http-status-codes";
import { Storage } from "@google-cloud/storage";
import path from "node:path";
import { ResponseHandler } from "@/response/ResponseHandler.js";
import { logger } from "@/utils/logger.js";
import { DownloadBulkSchema, DownloadFileSchema, UploadFilesSchema, type DownloadBulkDto, type DownloadFileDto, type UploadFilesDto } from "@/schemas/storageGcp.schema.js";
import archiver from "archiver";
import { logActivity, getTraceId } from '@/middlewares/logger.js';
import 'dotenv/config';

const bucketName = process.env.GCS_BUCKET;
if (!bucketName) throw new Error("Missing env var GCS_BUCKET for Google Cloud Storage integration");

const storage = new Storage();
const bucket = storage.bucket(bucketName);
const defaultPrefix = normalizePrefix(process.env.GCS_PREFIX ?? "");
const prefixMx = normalizePrefix(process.env.GCS_PREFIX_SOMX ?? defaultPrefix);
const prefixPe = normalizePrefix(process.env.GCS_PREFIX_SOPE ?? defaultPrefix);
const prefixCl = normalizePrefix(process.env.GCS_PREFIX_SOCL ?? defaultPrefix);
const publicBaseUrl = (process.env.GCS_PUBLIC_BASE ?? `https://storage.googleapis.com/${bucketName}`).replace(/\/+$/, "");
const shouldMakePublic = (process.env.GCS_MAKE_PUBLIC ?? "").toLowerCase() === "true";

function normalizePrefix(prefix: string): string {
    if (!prefix) return "";
    return prefix.replace(/(^\/+|\/+$)/g, "");
}

function sanitizeSegment(segment: string): string {
    return segment
        .split("/")
        .map(s => s.trim())
        .filter(Boolean)
        .filter(part => part !== "." && part !== "..")
        .join("/");
}

function resolvePrefix(req: Request): string {
    const countryHeader = (req.headers["x-country"] ?? req.headers["x-country-code"]) as string | undefined;
    const safeQuery = (req.query ?? {}) as Record<string, unknown>;
    const safeBody = (req.body ?? {}) as Record<string, unknown>;
    const countryQuery = (safeQuery.country ?? safeBody.country) as string | undefined;
    const raw = (countryHeader || countryQuery || "").toString().trim().toUpperCase();

    switch (raw) {
        case "MX":
            return prefixMx;
        case "CL":
            return prefixCl;
        case "PE":
            return prefixPe;
        default:
            return defaultPrefix;
    }
}

function buildObjectName(prefix: string, fileName: string, folder?: string): string {
    const safeFolder = sanitizeSegment(folder?? '');
    const safeFileName = path.posix.basename(fileName);
    if(folder == undefined || folder == ""){
        return [prefix, safeFileName].filter(Boolean).join("/");
    }
    return [prefix, safeFolder, safeFileName].filter(Boolean).join("/");
}

function toPublicUrl(objectName: string): string {
    const encoded = objectName.split("/").map(encodeURIComponent).join("/");
    return `${publicBaseUrl}/${encoded}`;
}


function serializeError(e: unknown) {
    if (e instanceof Error) {
        return {
            message: e.message,
            stack: e.stack,
            name: e.name
        };
    }
    return e;
}

function shouldRetry(err: any): boolean {
  if (!err) return false;

  const retryableCodes = [
    "ECONNRESET",
    "ETIMEDOUT",
    "EAI_AGAIN",
  ];

  if (retryableCodes.includes(err.code)) return true;

  const status = err.code || err.statusCode;

  // Retry únicamente errores de servidor
  if (status && status >= 500) return true;

  return false;
}

async function retry<T>(
  fn: () => Promise<T>,
  retries = 3,
  baseDelay = 300,
    context?: { fileName?: string }
): Promise<T> {
  let lastError: unknown;

  for (let attempt = 1; attempt <= retries; attempt++) {
    try {
      return await fn();
    } catch (err) {
      lastError = err;

      if (!shouldRetry(err) || attempt === retries) {
        break;
      }

      const delay = baseDelay * Math.pow(2, attempt - 1);
      const jitter = Math.random() * 100;

      logger.warn(
        `Retry ${attempt}/${retries} en ${delay}ms → file=${context?.fileName ?? "unknown"} → error=${(err as Error).message}`
      );


      await new Promise((res) => setTimeout(res, delay + jitter));
    }
  }

  throw lastError;
}

async function uploadFileBack(prefix: string, folder: string, file: Express.Multer.File): Promise<string> {
    const objectName = buildObjectName(prefix, folder, file.originalname);
    const blob = bucket.file(objectName);

    return new Promise<string>((resolve, reject) => {
        const blobStream = file.mimetype
            ? blob.createWriteStream({ resumable: false, contentType: file.mimetype })
            : blob.createWriteStream({ resumable: false });

        blobStream.on("error", (err) => {
            logger.error(`GCS upload failed: ${err.message}`);         
            reject(err);
        });

        blobStream.on("finish", async () => {
            if (shouldMakePublic) {
                try {
                    await blob.makePublic();
                } catch (err) {
                    logger.warn(`⚠️ Unable to make object public: ${objectName} → ${(err as Error).message}`);
                }
            }
            resolve(toPublicUrl(objectName));
        });

        blobStream.end(file.buffer);
    });
}

async function uploadFile(prefix: string, file: Express.Multer.File, folder?: string): Promise<string> {
  return retry(() => {
    const objectName = buildObjectName(prefix, file.originalname, folder);
    const blob = bucket.file(objectName);

    return new Promise<string>((resolve, reject) => {
      const blobStream = file.mimetype
        ? blob.createWriteStream({ resumable: false, contentType: file.mimetype })
        : blob.createWriteStream({ resumable: false });

        blobStream.on("error", reject);
    //   blobStream.on("error", (err) => {
    //     logger.error(`GCS upload error: ${err.message}`);
    //     reject(err);
    //   });

      blobStream.on("finish", async () => {
        try {
          if (shouldMakePublic) {
            await blob.makePublic();
          }
        } catch (err) {
          logger.warn(`makePublic falló: ${(err as Error).message}`);
        }

        resolve(toPublicUrl(objectName));
      });

      blobStream.end(file.buffer);
    });
  }, 3, 300, { fileName: file.originalname });
}


// POST /storage-gcp/upload
export async function uploadMultiple(req: Request, files: Express.Multer.File[], folder?: string) {
    try {
        const files = req.files as Express.Multer.File[];

        if (!files?.length) {
            return ResponseHandler.responseBuilder("No se enviaron archivos", null, -1, StatusCodes.BAD_REQUEST, false, null)
            
        }
        const prefix = resolvePrefix(req);
        const urls = await Promise.all(files.map((file) => uploadFile(prefix, file, folder)));
        logger.info(`✅ GCS upload OK → bucket=${bucketName} prefix=${prefix} count=${urls.length}`);

        return ResponseHandler.responseBuilder("Archivos subidos correctamente", { urls }, 0, StatusCodes.OK, true, null)
        
    } catch (e) {
        logger.error(`GCS upload FAILED → bucket=${bucketName} cause=${(e as Error).message}`);
        logActivity(true, `GCS upload FAILED → bucket=${bucketName}`, e, JSON.stringify({ trace_id: getTraceId() }));

        const errorInfo = JSON.stringify(serializeError(e));  

        return ResponseHandler.responseBuilder("Hubo un error en el guardado de los arcvhios en GCP", errorInfo, -1, StatusCodes.INTERNAL_SERVER_ERROR, false, errorInfo)
    }
}

// GET /storage-gcp/download
export async function downloadFile(req: Request, res: Response, next: NextFunction) {
    const query: DownloadFileDto = (res.locals.query as DownloadFileDto | undefined) ?? DownloadFileSchema.parse(req.query);
    const prefix = resolvePrefix(req);
    const objectName = buildObjectName(prefix, query.folder?? '', query.fileName);
    try {


        const file = bucket.file(objectName);

        const [exists] = await file.exists();
        if (!exists) {
            return res.status(StatusCodes.NOT_FOUND).json(
                ResponseHandler.responseBuilder("Archivo no encontrado", null, -1, StatusCodes.NOT_FOUND, false, null)
            );
        }

        const safeFileName = path.posix.basename(query.fileName).replace(/"/g, "");
        const [metadata] = await file.getMetadata();
        res.setHeader("Content-Disposition", `attachment; filename="${safeFileName}"`);
        res.setHeader("Content-Type", metadata.contentType || "application/octet-stream");
        
        if (metadata.size) {
            res.setHeader("Content-Length", metadata.size);
        }

        return file
            .createReadStream()
            .on("error", (err) => {
                logger.error(`GCS download FAILED → object=${objectName} cause=${err.message}`);
                if (!res.headersSent) {
                    res
                        .status(StatusCodes.INTERNAL_SERVER_ERROR)
                        .json(ResponseHandler.responseBuilder("Error al descargar el archivo", null, -1, StatusCodes.INTERNAL_SERVER_ERROR, false, null));
                } else {
                    res.destroy(err);
                }
            })
            .pipe(res);
    } catch (e) {
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logger.error(`GCS download FAILED → bucket=${bucketName}, object=${objectName}, cause=${(e as Error).message}`);
        logActivity(true, `GCS download FAILED → bucket=${bucketName} object=${objectName} cause=${(e as Error).message}` , err, JSON.stringify({ trace_id: getTraceId() }));
        next(e);
    }
}

// POST /storage-gcp/download/bulk
export async function downloadBulk(req: Request, res: Response, next: NextFunction) {
    try {
        const body: DownloadBulkDto = (res.locals.body as DownloadBulkDto | undefined) ?? DownloadBulkSchema.parse(req.body);
        const prefix = resolvePrefix(req);
        const { folder, fileNames } = body;

        // Validar existencia previa para evitar enviar zip vacío
        const existence = await Promise.all(
            fileNames.map(async (name) => {
                const objectName = buildObjectName(prefix, folder, name);
                const file = bucket.file(objectName);
                const [exists] = await file.exists();
                return { name, objectName, file, exists };
            })
        );

        const existingFiles = existence.filter(item => item.exists);
        if (existingFiles.length === 0) {
            return res
                .status(StatusCodes.NOT_FOUND)
                .json(ResponseHandler.responseBuilder("No se encontraron archivos para descargar", null, -1, StatusCodes.NOT_FOUND, false, null));
        }

        const missing = existence.filter(item => !item.exists).map(item => item.name);
        if (missing.length) {
            res.setHeader("X-Missing-Files", missing.join(","));
        }

        res.setHeader("Content-Type", "application/zip");
        res.setHeader("Content-Disposition", `attachment; filename="descargas-${Date.now()}.zip"`);

        const archive = archiver("zip", { zlib: { level: 9 } });
        archive.on("error", (err: unknown) => {
            const msg = err instanceof Error ? err.message : String(err);
            logger.error(`GCS bulk download ARCHIVE ERROR → ${msg}`);
            if (!res.headersSent) res.status(StatusCodes.INTERNAL_SERVER_ERROR).end();
        });

        archive.pipe(res);

        for (const item of existingFiles) {
            const safeName = path.posix.basename(item.name);
            archive.append(item.file.createReadStream(), { name: safeName });
        }

        await new Promise<void>((resolve, reject) => {
            res.on("finish", () => resolve());
            archive.on("error", reject);
            archive.finalize();
        });
    } catch (e) {
        logger.error(`GCS bulk download FAILED → bucket=${bucketName} cause=${(e as Error).message}`);
        next(e);
    }
}

// Solo para pruebas unitarias
export const __testUtils = {
    resolvePrefix,
    buildObjectName,
    normalizePrefix,
    sanitizeSegment,
};