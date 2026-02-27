import { z } from "zod/v4";

const multerFile = z.object({
    fieldname: z.string(),
    originalname: z.string(),
    encoding: z.string(),
    mimetype: z.string(),
    destination: z.string().optional(),
    filename: z.string().optional(),
    path: z.string().optional(),
    size: z.number()
});
const ACCEPTED_IMAGE_TYPES = ["text/csv","application/xml","text/xml"];


export const BaseSchemaParent = z.object({
    id: z.string().optional(),  //Identificaror unico Para CartaPorte
    extra1: z.string().optional(),
    extra2: z.string().optional(),
    content: z.string(),
});

export const BaseArrayFilesSchemaParent = BaseSchemaParent.extend({
    folder: z.string(),
    origen: z.string(),
    files: z.array(multerFile).optional().refine((files) => {
        if(files != undefined){
            return files.every((file) => ACCEPTED_IMAGE_TYPES.includes(file.mimetype));
        }
        return undefined;
    })
}).superRefine((data, ctx) => {

   //Origen Carta Porte
    if(data.origen == "2" && (data.files == undefined || data.files == null || data.files.length != 2)){
            ctx.issues.push({
                //code: z.ZodIssueCode.custom, // ZodIssueCode.custom is still available for defining the code, but not explicitly required for custom issues in ctx.issues.push
                message: "Para Carta Porte se requieren los dos archivos .csv y .xml'",
                path: ["files: undefined"],
                input: undefined,
                code: "custom"
            });
    }
    
});

export type BaseArrayFilesDtoParent = z.infer<typeof BaseArrayFilesSchemaParent>;
export type BaseDtoParent = z.infer<typeof BaseSchemaParent>;