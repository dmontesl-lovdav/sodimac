import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";

export type HttpMethod = "get" | "post" | "put" | "patch" | "delete";

export type ApiClient = {
    request: <T = any>(
        path: string,
        method: HttpMethod,
        data?: any,
        extra?: AxiosRequestConfig
    ) => Promise<T>;
    requestBinary: (
        path: string,
        method: HttpMethod,
        data?: any,
        filename?: string
    ) => Promise<void>;
};

export function createApiClient(options?: {
    baseUrl?: string;
    timeoutMs?: number;
}): ApiClient {
    const baseUrl =
        options?.baseUrl ??
        process.env.API_URL ??
        "/api";

    const timeoutMs = options?.timeoutMs ?? 15000;

    const instance: AxiosInstance = axios.create({
        baseURL: baseUrl.replace(/\/+$/, ""),
        timeout: timeoutMs,
        headers: {
            "Content-Type": "application/json",
            "X-User-Id": "TEST_USER_01",
        },
    });

    async function request<T = any>(
        path: string,
        method: HttpMethod,
        data?: any,
        extra?: AxiosRequestConfig
    ): Promise<T> {
        try {
            const isFormData =
                typeof FormData !== "undefined" && data instanceof FormData;

            const headers: Record<string, string> = {
                Accept: "application/json",
                "X-User-Id": "TEST_USER_01",
            };

            if (!isFormData && method !== "get") {
                headers["Content-Type"] = "application/json";
            }

            const res: AxiosResponse<T> = await instance.request({
                url: `/${path.replace(/^\/+/, "")}`,
                method,
                data: isFormData ? data : data ?? undefined,
                headers,
                responseType: extra?.responseType,
                ...extra,
            });

            return res.data as T;
        } catch (error: any) {
            const errorData = error.response?.data;
            const statusCode = error.response?.status;
            const url = error.config?.url || "unknown";

            if (errorData && errorData.error) {
                const errorObj = new Error(errorData.message || "Operation error");
                (errorObj as Error & { code?: string; details?: unknown; statusCode?: number }).code = errorData.code;
                (errorObj as Error & { code?: string; details?: unknown; statusCode?: number }).details = errorData.details;
                (errorObj as Error & { code?: string; details?: unknown; statusCode?: number }).statusCode = statusCode;
                throw errorObj;
            }

            let message = "Server connection error";

            if (statusCode === 500) {
                message = `Internal server error at ${url}. Please contact the administrator.`;
            } else if (statusCode === 404) {
                message = `Resource not found: ${url}`;
            } else if (statusCode === 400) {
                message = errorData?.message || "Invalid request data";
            } else if (statusCode === 401 || statusCode === 403) {
                message = "You do not have permission to perform this operation";
            } else if (error.message) {
                message = error.message;
            }

            const errorObj = new Error(message);
            (errorObj as Error & { statusCode?: number; url?: string }).statusCode = statusCode;
            (errorObj as Error & { statusCode?: number; url?: string }).url = url;
            throw errorObj;
        }
    }

    async function requestBinary(
        path: string,
        method: HttpMethod,
        data?: any,
        filename?: string
    ): Promise<void> {
        try {
            const res: AxiosResponse = await instance.request({
                url: `/${path.replace(/^\/+/, "")}`,
                method,
                data,
                responseType: "blob",
                headers: {
                    "X-User-Id": "TEST_USER_01",
                },
            });

            const blob = new Blob([res.data]);

            const anchor = document.createElement("a");
            anchor.href = window.URL.createObjectURL(blob);
            anchor.download = filename || "file.bin";

            document.body.appendChild(anchor);
            anchor.click();

            document.body.removeChild(anchor);
            window.URL.revokeObjectURL(anchor.href);
        } catch (error: any) {
            const statusCode = error.response?.status;
            const url = error.config?.url || "unknown";

            let message = "File download error";

            if (statusCode === 500) {
                message = `Internal server error at ${url}. Please contact the administrator.`;
            } else if (statusCode === 404) {
                message = `Resource not found: ${url}`;
            } else if (statusCode === 401 || statusCode === 403) {
                message = "You do not have permission to download this file";
            } else if (error.message) {
                message = error.message;
            }

            const errorObj = new Error(message);
            (errorObj as Error & { statusCode?: number; url?: string }).statusCode = statusCode;
            (errorObj as Error & { statusCode?: number; url?: string }).url = url;
            throw errorObj;
        }
    }

    return {
        request,
        requestBinary,
    };
}

const apiClient = createApiClient();

export default apiClient;