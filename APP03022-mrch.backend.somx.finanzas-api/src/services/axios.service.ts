import axios from 'axios';
import FormData from 'form-data';
import 'dotenv/config';
import multer from 'multer';
import { logger } from "@/utils/logger.js";

/**
 * Envía archivos y datos adicionales a una API externa usando axios
 * @param {string} url - URL de la API externa
 * @param {Array} files - Arreglo de objetos { buffer, originalname }
 * @param {Object} extraData - Datos adicionales en formato clave-valor
 * @param {Object} headers - Headers adicionales (ej. Authorization)
 * @returns {Promise<Object>} Respuesta de la API externa
 */
async function sendFilesWithData(url: string, files: Express.Multer.File[], extraData = {}, headers = {}, validateResponse = null ) {
    try {
        const formData = new FormData();

        // Agregar archivos
        files.forEach(file => {
            formData.append('files', file.buffer, file.originalname);
        });

        // Agregar datos adicionales
        for (const key in extraData) {
            formData.append(key, extraData[key as keyof typeof extraData]);
        }

        // Enviar petición POST
        const response = await axios.post(url, formData, {
            headers: {
                ...formData.getHeaders(),
                ...headers,
                Authorization: 'Bearer TU_TOKEN_AQUI' // Si la API requiere autenticación
            }
        });

                // Validar respuesta si se proporciona función

        if (!response.data.success) {
            logger.error("❌ Error al enviar archivos  → data={}", response);
            throw new Error('Error al enviar archivos.');
        }
        
        logger.info("✅ Archivos enviados exitosamente  → data={}", response);
        return response.data.success;
    } catch (error) {
        logger.error("❌ Error al enviar archivos  → data={}", error);
        throw error;
    }
}

export async function sendFilesToBucket(files: Express.Multer.File[], folder: String){
    try {
        return true; //PARA PRUEBAS
        const extraData = {
             folder: folder,
        };

        const apiResponse = await sendFilesWithData(
            (process.env.UTILERIAS_API_URL_BBF ?? "") + (process.env.UTILIERIAS_API_URI_UPLOAD_FILES ?? ""),
            files,
            extraData,
            { Authorization: 'Bearer TU_TOKEN_AQUI' }
        );

        return apiResponse;
        //FALTA VALIDAR LA RESPUESTA Y ENVIAR EL RETURN CORRECTO
        //return apiResponse;

    } catch (error) {
        return false;
        //return res.status(500).json({ message: 'Error al enviar a la API', error: error.message });
    }


        // if (files.length > 0) {
        //     // Procesar cada archivo en memoria
        //     const formData = new FormData();
        //     files.forEach((file, index) => {
        //         console.log(`Archivo ${index + 1}:`);
        //         console.log('Nombre:', file.originalname);
        //         console.log('Tipo:', file.mimetype);
        //         console.log('Tamaño:', file.size);
        //         // Ejemplo: convertir a string si es texto
        //         const contenido = file.buffer.toString('utf-8');

        //         formData.append('files', file.buffer, file.originalname);
        //     });
  
            // Ejemplo: enviar a una API externa
            // const response = await axios.post('https://api.ejemplo.com/upload', formData, {
            //     headers: {
            //         ...formData.getHeaders(), // Necesario para multipart/form-data
            //         Authorization: 'Bearer TU_TOKEN_AQUI' // Si la API requiere autenticación
            //     }
            // });

        // } else {

        // }

}

export async function axiosGet(url: string ){


    //const response2 = await axios.get(url);

    let response : any;
    const res = await axios.get(url)
    .then(function (_response) {
        // manejar respuesta exitosa
        response = _response;
        console.log(_response);
    })
    .catch(function (error) {
        // manejar error
        console.log(error);
    })
    .finally(function () {
        // siempre sera executado
    });
    return response;
}