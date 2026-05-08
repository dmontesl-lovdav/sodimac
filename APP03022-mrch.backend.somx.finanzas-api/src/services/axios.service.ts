import axios from 'axios';
import FormData from 'form-data';
import dotenv from 'dotenv';
import path from 'path';
import multer from 'multer';
import { logger } from "@/utils/logger.js";
import { GenericCatalogDetails, Supplier, ValidStatus } from '@/response/GenericCatalogDetails.dto.js';
import { logActivity, getTraceId } from '@/middlewares/logger.js';
import * as constants from "@/constants/catalogConstantsCodes.js";
import {uploadMultiple} from '@/services/storageGcp.service.js';

import { StatusCodes } from "http-status-codes";
import { ResponseHandler } from "@/response/ResponseHandler.js";
import { fileURLToPath } from 'url';

// Load local .env with priority over system variables
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
dotenv.config({
    path: path.resolve(__dirname, '../../.env'),
    override: true,
});
/**
 * Envía archivos y datos adicionales a una API externa usando axios
 * @param {string} url - URL de la API externa
 * @param {Array} files - Arreglo de objetos { buffer, originalname }
 * @param {Object} extraData - Datos adicionales en formato clave-valor
 * @param {Object} headers - Headers adicionales (ej. Authorization)
 * @returns {Promise<Object>} Respuesta de la API externa
 */
async function sendFilesWithData(url: string, files: Express.Multer.File[], extraData = {}, headers = {}, validateResponse = null) {
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
            }
        });

        // Validar respuesta si se proporciona función

        if (!response.data.success) {
            logger.error("❌ Error al enviar archivos  → data={}", response);
            throw new Error('Error al enviar archivos.');
        }
        logActivity(false, 'Archivos enviados exitosamente a google cloud', null, JSON.stringify({ trace_id: getTraceId() }));
        logger.info("✅ Archivos enviados exitosamente  → data={}", response);
        return response.data.success;
    } catch (error) {
        logger.error("❌ Error al enviar archivos  → data={}", error);
        throw error;
    }
}

export async function sendFilesToBucket(files: Express.Multer.File[], folder: string, token: string) {
    try {
        //return true; //PARA PRUEBAS

        let apiResponse = ResponseHandler.responseBuilder("", null, 0, StatusCodes.CREATED, true, "", "");
        apiResponse = await uploadMultiple(files, folder);

        // const extraData = {
        //     folder: folder,
        // };

        // const apiResponse = await sendFilesWithData(
        //     (process.env.UTILERIAS_API_URL_BBF ?? "") + (process.env.UTILIERIAS_API_URI_UPLOAD_FILES ?? ""),
        //     files,
        //     extraData,
        //     { Authorization: `Bearer ${token}` }
        // );

        return apiResponse;


    } catch (e) {
        logActivity(true, 'ERROR : No fue posible registrar los archivos en google cloud', e, JSON.stringify({ trace_id: getTraceId() }));
        return ResponseHandler.responseBuilder("ERROR : No fue posible registrar los archivos en google cloud", e, -1, StatusCodes.CREATED, false, "", "");
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

export async function axiosGet(url: string, token: string, params?: any) {

    let response: any;
    if (params == undefined) {
        params = {};
    }

    if (token == undefined) {
        token = '';
    }

    await axios.get(url, { 
        params,
        headers: {
            Authorization: `Bearer ${token}`
        }

        })
        .then(function (_response) {
            response = _response;
            console.log(_response);
        })
        .catch(function (error) {
            response = error.response;
            console.log(error);
            logActivity(true, 'ERROR: EN AXIOS GET. URL:' + url, error, JSON.stringify({ trace_id: getTraceId() }));
        })
        .finally(function () {
            console.log();
        });
    return response;
}

export async function axiosPost(url: string, data: any, token: string) {

    //const response2 = await axios.get(url);
    if (token == undefined) {
        token = '';
    }
    let response: any;
    const res = await axios.post(url, data, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            })
        .then(function (_response) {
            // manejar respuesta exitosa
            response = _response;
            console.log(_response);
        })
        .catch(function (error) {
            // manejar error
            console.log(error);
            logActivity(true, 'ERROR: EN AXIOS POST. URL:' + url, error, JSON.stringify({ trace_id: getTraceId() }));
        })
        .finally(function () {
            // siempre sera executado
        });
    return response;
}

export async function GetSuppliers(token: string) {
    const allSuppliers: any = await axiosGet((process.env.CATALOGS_API_URL_BFF) +  constants.CatalogSupplierUrls.CATALOGS_API_GET_ALL_SUPPLIERS, token);
    const supplierList: Supplier[] = allSuppliers.data as Supplier[];
    return supplierList;
}

export async function GetSupplierBySupplierNumber(supplierNumber: number, token: string) {
    const supplierTmp: any = await axiosGet((process.env.CATALOGS_API_URL_BBF ?? "") + constants.CatalogSupplierUrls.CATALOGS_API_GET_SUPPLIER + "/" + supplierNumber, token);
    if (supplierTmp.data == '') {
        return undefined;
    } else {
        const supplier: Supplier = supplierTmp.data as Supplier;
        return supplier;
    }

}

export async function GetCatalogDetail(url: string, token: string) {
    const CatCatalog: any = await axiosGet(url, token, undefined);
    const msgObj: GenericCatalogDetails = CatCatalog.data as GenericCatalogDetails;
    return msgObj;
}

export async function GetCatalogDetailList(url: string, token: string) {
    const CatCatalog: any = await axiosGet(url,token, undefined);
    const msgObj: GenericCatalogDetails[] = CatCatalog.data as GenericCatalogDetails[];
    return msgObj;
}

export async function ValidStatus(url: string, optionId: number, sourceStatus: number, targetStatus: number, token: string) {
    const params = {
        optionId: optionId,
        sourceStatus: sourceStatus,
        targetStatus: targetStatus
    }
    const CatCatalog: any = await axiosGet(url,token, params);
    const msgObj: ValidStatus = CatCatalog.data as ValidStatus;
    if (msgObj.success && msgObj.valid) {
        return true;
    } else {
        return false;
    }

}