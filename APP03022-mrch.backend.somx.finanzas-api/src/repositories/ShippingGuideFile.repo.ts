import { datasource } from "@/config/typeorm-datasource.js";
import { ShippingGuideFile } from "@/entities/ShippingGuideFile.entity.js";

export const repo = () => datasource.getRepository(ShippingGuideFile);



export async function createOne(data: Partial<ShippingGuideFile>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function createWithFile(file: Express.Multer.File, _shippingGuideDocumentId: string ) {
    const fileEntity = repo().create({
        //shippingGuideDocumentId: _shippingGuideDocumentId,
        fileName: file.originalname,
        mimeType: file.mimetype,
        data: file.buffer, // 👈 aquí guardas el buffer
        fileType: file.mimetype.includes("xml") ? "xml" : "csv",
    });

    const saved = await repo().save(fileEntity);
    return saved;
}