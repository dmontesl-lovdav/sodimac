import { z } from "zod";

const toString = z.preprocess((value) => {
    if (typeof value === "string") return value;
    if (Array.isArray(value)) return value[0];
    return "";
}, z.string().trim().min(1, { message: "El valor es requerido" }));

export const UploadFilesSchema = z.object({
    folder: toString,
});

export const DownloadFileSchema = z.object({
    folder: toString,
    fileName: toString,
});

export const DownloadBulkSchema = z.object({
    folder: toString,
    fileNames: z
        .preprocess((value) => {
            if (typeof value === "string") return [value];
            if (Array.isArray(value)) return value;
            if (value && typeof value === "object" && "fileNames" in (value as any)) return (value as any).fileNames;
            return [];
        }, z.array(z.string().trim().min(1, { message: "fileNames es requerido" })).min(1, { message: "Debe enviar al menos un archivo" })),
});

export type UploadFilesDto = z.infer<typeof UploadFilesSchema>;
export type DownloadFileDto = z.infer<typeof DownloadFileSchema>;
export type DownloadBulkDto = z.infer<typeof DownloadBulkSchema>;