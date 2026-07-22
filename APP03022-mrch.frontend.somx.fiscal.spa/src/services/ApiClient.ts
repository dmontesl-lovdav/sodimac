import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";
import { localHomeStore } from "../store/localStore";

export type HttpMethod = "get" | "post" | "put" | "patch" | "delete";
export type TokenProvider = string | (() => string | null | undefined);

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
    tokenProvider?: TokenProvider;
    timeoutMs?: number;
}): ApiClient {
    const APP_DEV = String(process.env.APP_DEV).toLowerCase() === "true";

    const baseUrl =
        options?.baseUrl ??
        process.env.REACT_APP_API_BASE_URL ??
        process.env.API_BASE_URL ??
        "";

    const timeoutMs = options?.timeoutMs ?? 15000;

    let baseEnd = baseUrl.length;
    while (baseEnd > 0 && baseUrl.charAt(baseEnd - 1) === "/") baseEnd--;
    const normalizedBase = baseUrl.slice(0, baseEnd);

    const instance: AxiosInstance = axios.create({
        baseURL: normalizedBase,
        timeout: timeoutMs,
    });

    function resolveToken(): string | null {
        if (APP_DEV) {
            return null;
        }

        if (typeof options?.tokenProvider === "function") {
            const providedToken = options.tokenProvider();
            if (providedToken?.trim()) {
                return providedToken;
            }
        }

        if (typeof options?.tokenProvider === "string" && options.tokenProvider.trim()) {
            return options.tokenProvider;
        }

        const storeToken =
            (localHomeStore.getState() as any)?.authentication?.token ||
            (localHomeStore.getState() as any)?.authentication?.idToken;

        if (storeToken?.trim()) {
            return storeToken;
        }

        const envToken =
            process.env.REACT_APP_AUTH_DEFAULT_TOKEN ??
            process.env.AUTH_DEFAULT_TOKEN;

        if (envToken?.trim()) {
            return envToken;
        }

        return null;
    }

    async function request<T = any>(
        path: string,
        method: HttpMethod,
        data?: any,
        extra?: AxiosRequestConfig
    ): Promise<T> {
        const token = resolveToken();

        if (!APP_DEV && !token) {
            throw new Error("No token");
        }

        const isFormData =
            typeof FormData !== "undefined" && data instanceof FormData;

        const headers: Record<string, string> = {
            Accept: "application/json",
        };

        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

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
    }

    async function requestBinary(
        path: string,
        method: HttpMethod,
        data?: any,
        filename?: string
    ): Promise<void> {
        const token = resolveToken();

        if (!APP_DEV && !token) {
            throw new Error("No token");
        }

        const headers: Record<string, string> = {};

        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        const res: AxiosResponse = await instance.request({
            url: `/${path.replace(/^\/+/, "")}`,
            method,
            data,
            responseType: "blob",
            headers,
        });

        const blob = new Blob([res.data]);

        const anchor = document.createElement("a");
        anchor.href = window.URL.createObjectURL(blob);
        anchor.download = filename ?? "file.bin";

        document.body.appendChild(anchor);
        anchor.click();

        document.body.removeChild(anchor);
        window.URL.revokeObjectURL(anchor.href);
    }

    return {
        request,
        requestBinary,
    };
}
