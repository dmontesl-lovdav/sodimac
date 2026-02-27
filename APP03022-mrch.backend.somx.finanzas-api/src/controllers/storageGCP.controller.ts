import type { Request, Response, NextFunction } from "express";
import { StatusCodes, ReasonPhrases } from 'http-status-codes';
import { ResponseHandler } from '@/response/ResponseHandler.js';
import {Storage} from '@google-cloud/storage'
import 'dotenv/config';
import path from "path";
import { logger } from "@/utils/logger.js";


//https://huongdanjava.com/emulate-google-cloud-storage-using-fake-gcs-server.html
// Configuración del cliente para fake-gcs-server
// const storage = new Storage({
//   projectId: 'test-project',
//   apiEndpoint: 'http://localhost:4443', // fake-gcs-server endpoint
  
// });


// Configura el cliente de GCS
const storage = new Storage();
const bucketName = 'mi-bucket';
//const bucketName = process.env.GCS_BUCKET ?? "";
const bucket = storage.bucket(bucketName);
let gcsPrefixTmp = (process.env.GCS_PREFIX ?? "");
gcsPrefixTmp = "";
const gcsPrefix = gcsPrefixTmp.endsWith("/") ? gcsPrefixTmp : (gcsPrefixTmp + "/");
// Replace with the path to your service account key file
//const keyFilename = path.join(__dirname, 'path/to/your-service-account-key.json');






async function ensureBucketExists() {
  try {
    const [exists] = await storage.bucket(bucketName).exists();
    if (!exists) {
      await storage.createBucket(bucketName);
      console.log(`Bucket ${bucketName} creado`);
    } else {
      console.log(`Bucket ${bucketName} ya existe`);
    }
  } catch (error) {
    console.error('Error al verificar/crear bucket:', error);
  }
}



// GET /storage-GCP
export async function downloadFile(req: Request, res: Response, next: NextFunction) {
    try {
        const folder = req.query.folder as string;      // Carpeta
        let fileName: string  = req.query.fileName as string;  // Nombre del archivo
        
        // Construye la ruta completa del archivo en el sistema local

        
        const fullPath = `${gcsPrefix}/${folder}/${fileName}`; // Ruta completa en el bucket
        //const fullPath = `${fileName}`; // Ruta completa en el bucket
        //const file = bucket.file(fullPath);
        const file = storage.bucket(bucketName).file(fullPath)

        
        // Descarga el archivo en memoria
        //const [contents] = await file.download();


        // Verificar si el archivo existe
        // const [exists] = await file.exists();
        // if (!exists) {
        // return res.status(404).json(ResponseHandler.responseBuilder("Archivo no encontrado.",null,-1, StatusCodes.NOT_FOUND, false, ""));
        // }

        // Configurar headers para descarga

        // Configura headers para la descarga
        res.setHeader('Content-Disposition', `attachment; filename="${fileName}"`);
        res.setHeader('Content-Type', 'application/octet-stream');
        //res.send(contents);


        // Crear stream y enviar al cliente
        file.createReadStream()
        .on('error', (err) => {
            console.error(err);
            res.status(500).json(ResponseHandler.responseBuilder("Error al descargar el archivo. ",null,-1, StatusCodes.BAD_REQUEST, false, ""));
        })
        .pipe(res);


    } catch (e) { 
        logger.error("❌ GCS download FAILED → bucket={} cause={}", bucket, e); 
        res.status(500).json(ResponseHandler.responseBuilder("ERROR: " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""));
        next(e); 
    }
}



export async function uploadMultipleV2(req: Request, res: Response, next: NextFunction) {
    try {
        const folder = req.params.folder; // Carpeta dinámica desde la URL


        const files = req.files as Express.Multer.File[];
        if (!files || files.length === 0) {
        return res.status(400).send('No se enviaron archivos.');
        }

        files.forEach(async (file, index) => {
                console.log(`Archivo ${index + 1}:`);
                console.log('Nombre:', file.originalname);
                console.log('Tipo:', file.mimetype);
                console.log('Tamaño:', file.size);
                // Ejemplo: convertir a string si es texto
                const contenido = file.buffer.toString('utf-8');
                //console.log('Contenido:', contenido);             
        });

        const uploadPromises = files.map((file) => {
            return new Promise((resolve, reject) => {
                //const blob = bucket.file(`${gcsPrefix}/${folder}/${file.originalname}`);
                  const blob = bucket.file(`${gcsPrefix}/${folder}/${file.originalname}`);
                const blobStream = blob.createWriteStream({
                resumable: false,
                contentType: file.mimetype,
                //contentType: "application/octet-stream",
                });

                blobStream.on('error', reject);

                blobStream.on('finish', () => {
                 blob.makePublic(); // Opcional
                const publicUrl = `https://storage.googleapis.com/${bucketName}/${blob.name}`;
                resolve(publicUrl);
                });

                blobStream.end(file.buffer);
            });
        });

        const urls = await Promise.all(uploadPromises);
        logger.info("✅ GCS upload OK → bucket={} urls={}", bucket, urls);
        res.status(200).json(ResponseHandler.responseBuilder("Archivos subidos correctamente. urls:" + urls ,null,0, StatusCodes.OK, true, ""));

    } catch (e) {
        logger.error("❌ GCS upload FAILED → bucket={} cause={}", bucket, e); 
        res.status(500).json(ResponseHandler.responseBuilder("ERROR: " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""));
        next(e); 
    }
}

// POST /storage-gcp
export async function uploadMultiple(req: Request, res: Response, next: NextFunction) {
    try {
        //await ensureBucketExists();
        const folder = req.body.folder; // Carpeta dinámica desde la URL


        const files = req.files as Express.Multer.File[];
        if (!files || files.length === 0) {
        return res.status(400).send('No se enviaron archivos.');
        }

        const uploadPromises = files.map((file) => {
            const blob = bucket.file(`${gcsPrefix}/${folder}/${file.originalname}`);
            //const blob = bucket.file(`${folder}/${file.originalname}`);

            const blobStream = blob.createWriteStream({
            resumable: false,
            contentType: file.mimetype,
            //contentType: "application/octet-stream",
            });

            return new Promise((resolve, reject) => {
                blobStream.on("error", (err) => {
                    logger.error("❌ GCS upload FAILED → bucket={} cause={}", bucket, err); 
                    res.status(500).json(ResponseHandler.responseBuilder("ERROR: " + err.message ,null,-1, StatusCodes.BAD_REQUEST, false, err));
                });
                blobStream.on('finish', async () => {
                    //await blob.makePublic(); // Opcional
                    const publicUrl = `http://localhost:4443/${bucketName}/${blob.name}`;
                    resolve(publicUrl);              
                });

                blobStream.end(file.buffer);
            });
        });

        const urls = await Promise.all(uploadPromises);
        logger.info("✅ GCS upload OK → bucket={} urls={}", bucket, "urls");
        res.status(200).json(ResponseHandler.responseBuilder("Archivos subidos correctamente. urls:" + urls ,null,0, StatusCodes.OK, true, ""));

    } catch (e) {
        logger.error("❌ GCS upload FAILED → bucket={} cause={}", bucket, e); 
        res.status(500).json(ResponseHandler.responseBuilder("ERROR: " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""));
        next(e); 
    }
}