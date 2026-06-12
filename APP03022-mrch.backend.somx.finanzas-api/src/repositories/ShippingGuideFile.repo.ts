import { datasource } from "@/config/typeorm-datasource.js";
import { ShippingGuideFile } from "@/entities/ShippingGuideFile.entity.js";
import multer from "multer";
import { Between, In, type FindOptionsWhere } from "typeorm";

export const repo = () => datasource.getRepository(ShippingGuideFile);



export async function createOne(data: Partial<ShippingGuideFile>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function createWithFile(file: Express.Multer.File, _shippingGuideDocumentId: string ) {
     //const savedFiles = [];
    const fileEntity = repo().create({
        //shippingGuideDocumentId: _shippingGuideDocumentId,
        fileName: file.originalname,
        mimeType: file.mimetype,
        data: file.buffer, // 👈 aquí guardas el buffer
        fileType: file.mimetype.includes("xml") ? "xml" : "csv",
    });

    const saved = await repo().save(fileEntity);
    //savedFiles.push(saved);

    return saved;
}