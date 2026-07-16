import axios, {AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import axiosRetry from "axios-retry";
import type { Request } from "express";
import { createBreaker } from "@/services/circuitBreaker.service.js";
import { getBreaker } from "@/services/breakerRegistry.js";
import { getCache, setCache } from "@/services/cache.service.js";

import FormData from 'form-data';

import { logger } from "@/utils/logger.js";
import { GenericCatalogDetails, Supplier, ValidStatus } from '@/response/GenericCatalogDetails.dto.js';
import { logActivity, getTraceId } from '@/middlewares/logger.js';
import * as constants from "@/constants/catalogConstantsCodes.js";
import {uploadMultiple} from '@/services/storageGcp.service.js';

import { StatusCodes } from "http-status-codes";
import { ResponseHandler } from "@/response/ResponseHandler.js";
import 'dotenv/config';


/**
 * Cliente axios base
 */
const axiosClient = axios.create({
  timeout: 5000,
});


/**
 * Politica de reintentos
 */
axiosRetry(axiosClient, {
  retries: 3, // 🔁 número de reintentos
  retryDelay: (count) => Math.pow(2, count) * 1000,
//   retryDelay: (retryCount) => {
//     console.log(`🔁 Retry intento #${retryCount}`);
//     return retryCount * 1000; // backoff simple: 1s, 2s, 3s
//   },

  retryCondition: (error: AxiosError) => {
    // ✅ solo retry en estos casos
    
    const method = error.config?.method;

    if (method !== "get") return false;  //EVITAR RETRY EN POST PARA EVITAR DUPLICADOS

    // error de red
    if (axiosRetry.isNetworkError(error)) return true;

    // status 5xx (server down)
    const status = error.response?.status;
    if (status !== undefined && status >= 500) {
    return true;
    }


    // timeout
    if (error.code === "ECONNABORTED") return true;

    return false; // ❌ NO retry en 4xx
  },
});

/**
 * Interceptor de request (agrega headers automáticamente)
 */
axiosClient.interceptors.request.use((config) => {
  // puedes agregar headers globales aquí si quieres
  console.log("➡️ Request:", config.method?.toUpperCase(), config.url);
  return config;
});

/**
 * Interceptor de response
 */
axiosClient.interceptors.response.use(
  (response) => {
    console.log("✅ Response:", response.status, response.config.url);
    return response;
  },
  (error: unknown) => {
    console.error("❌ Axios error:", (error as any)?.response?.data || (error as any)?.message);
    const url = (error as any)?.config?.url;

    logActivity(
      true,
      'ERROR: EN AXIOS GET. URL:' + url,
      error,
      JSON.stringify({ trace_id: getTraceId() })
    );

    // ✅ FIX Sonar: garantizar Error
    if (error instanceof Error) {
      return Promise.reject(error);
    }

    return Promise.reject(new Error('Unknown Axios error'));
  }
);

/**
 * request base sin resiliencia
 */
const axiosGetRaw = async (url: string, token?: string, params?: any) => {

    console.log("🔥 Llamando API:", url)
    if (params == undefined) {
        params = {};
    }
    const config: AxiosRequestConfig = {
      url,
      method: "GET",
      params,
      headers: {},
      
      validateStatus: (status) => {  //NO TOMA COMO ERROR LOS 404
          return status < 500;
      }

    };

    if (token) {
      config.headers!["Authorization"] = `Bearer ${token}`;
    }


    const response: AxiosResponse = await axiosClient(config);
    console.log("✅ API respondió:", response.status)
    return response.data;
};



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

export async function sendFilesToBucket(req: Request,files: Express.Multer.File[], token: string, folder?: string) {
    try {



        const apiResponse = await uploadMultiple(req, files, folder);

        return apiResponse;

    } catch (e) {
        logActivity(true, 'ERROR : No fue posible registrar los archivos en google cloud', e, JSON.stringify({ trace_id: getTraceId() }));
        return ResponseHandler.responseBuilder("ERROR : No fue posible registrar los archivos en google cloud", e, -1, StatusCodes.CREATED, false, "", "");
    }

}


/**
 * GET generico
 */
export async function axiosGet<T>(
  url: string,
  token?: string,
  params?: any
): Promise<T | null> {

    const cacheKey = url;

    // ✅ 1. revisar cache primero
    const cached = getCache<T>(cacheKey);
    if (cached) {
        return cached;
    }

    const type = resolveEndpointType(url);
    //const key = type; // o combinación si quieres más granular
    const key = url;
  try {
    
    
    // ✅ breaker por endpoint
    const breaker = getBreaker(key, axiosGetRaw, type);

    
    // ✅ 2. usar breaker + retry
    const data = await breaker.fire(url, token, params);

    
    if (data) {
      // ✅ 3. guardar en cache (ej: 30 min)
      setCache(cacheKey, data, 30 * 60 * 1000);
    }

    return data;

  } catch (error: any) {
    console.error("axiosGet failed:", error?.response?.data || error.message);
    logActivity(true, 'ERROR: EN AXIOS GET. URL:' + url, error, JSON.stringify({ trace_id: getTraceId() }));
    
    // ✅ fallback manual
    const cachedFallback = getCache<T>(cacheKey);
    if (cachedFallback) return cachedFallback;

    return buildDefaultCatalog("error", url) as T;
  }
}


const axiosPostRaw = async (
  url: string,
  token?: string,
  body?: any
) => {

  console.log("🔥 POST API:", url);

  if (!body) {
    body = {};
  }

  const config: AxiosRequestConfig = {
    url,
    method: "POST",
    data: body, // 👈 🔥 CAMBIO IMPORTANTE
    headers: {},
  };

  if (token) {
    config.headers!["Authorization"] = `Bearer ${token}`;
  }

  const response: AxiosResponse = await axiosClient(config);

  console.log("✅ API respondió:", response.status);

  return response.data;
};

/**
 * POST generico
 */
export async function axiosPost<T>(
  url: string,
  body: any,
  token?: string
): Promise<T | null> {
  try {
    console.log("🔥 POST API:", url);

    if (!body) {
        body = {};
    }

    const config: AxiosRequestConfig = {
        url,
        method: "POST",
        data: body, // 👈 🔥 CAMBIO IMPORTANTE
        headers: {},
    };

    if (token) {
        config.headers!["Authorization"] = `Bearer ${token}`;
    }

    const response: AxiosResponse = await axiosClient(config);

    console.log("✅ API respondió:", response.status);

    return response.data;


  } catch (error: any) {
    console.error("axiosPost failed:", error?.response?.data || error.message);
    return null;
  }
}


export async function GetSuppliers(token: string) {
    const allSuppliers: any = await axiosGet((process.env.CATALOGS_API_URL_BFF) +  constants.CatalogSupplierUrls.CATALOGS_API_GET_ALL_SUPPLIERS, token);
    const supplierList: Supplier[] = allSuppliers as Supplier[];
    return supplierList;
}

export async function GetStores(token: string) {
    const allStores: any = await axiosGet((process.env.CATALOGS_API_URL_BFF) +  constants.CatalogStores.CATALOGS_API_STORES, token);
    const storeList: GenericCatalogDetails[] = allStores as GenericCatalogDetails[];
    return storeList;
}

export async function GetSupplierBySupplierNumber(supplierNumber: number, token: string) {
    const supplierTmp: any = await axiosGet((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogSupplierUrls.CATALOGS_API_GET_SUPPLIER + "/" + supplierNumber, token);
    if (supplierTmp == '' || supplierTmp == undefined) {
        return undefined;
    } else {
        const supplier: Supplier = supplierTmp.data as Supplier;
        return supplier;
    }

}


export async function GetCatalogDetail(
  url: string,
  token: string
): Promise<GenericCatalogDetails> {

  const data = await axiosGet<GenericCatalogDetails>(url, token);

  if (!data) {
    console.warn("⚠️ catálogo no disponible");
    logActivity(true, 'ERROR: EN AXIOS POST. URL:' + url, "catálogo no disponible", JSON.stringify({ trace_id: getTraceId() }));
    return buildDefaultCatalog("empty",url)
  }

  return data;
}


function buildDefaultCatalog(type: "error" | "empty", url?: string): GenericCatalogDetails {
    return {
        key: type,
        description: `Sin información (${url ?? "unknown"})`,
        value: '',
        color: '',
        externalKey: '',
        internalStatus: 0,
        success : false,
    };
}


export async function GetCatalogDetailList(url: string, token: string) {
    const CatCatalog: any = await axiosGet(url,token);
    const msgObj: GenericCatalogDetails[] = CatCatalog as GenericCatalogDetails[];
    return msgObj;
}

export async function ValidStatus(url: string, optionId: number, sourceStatus: number, targetStatus: number, token: string) {
    const params = {
        optionId: optionId,
        sourceStatus: sourceStatus,
        targetStatus: targetStatus
    }
    const CatCatalog: any = await axiosGet(url,token, params);
    const msgObj: ValidStatus = CatCatalog as ValidStatus;
    if (msgObj.success && msgObj.valid) {
        return true;
    } else {
        return false;
    }

}

function resolveEndpointType(url: string): string {
  if (url.includes("/catalog")) return "catalog";
  if (url.includes("/suppliers")) return "suppliers";
  if (url.includes("/status")) return "status";

  return "default";
}
