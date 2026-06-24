// src/services/ApiClient.ts
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
    const baseUrl =
        options?.baseUrl ??
        process.env.API_URL ??
        process.env.BACKEND_URL ??
        process.env.APP_URL ??
        process.env.API_BASE_URL ??
        process.env.REACT_APP_API_BASE_URL ??
        "";

    const timeoutMs = options?.timeoutMs ?? 15000;

    const instance: AxiosInstance = axios.create({
        baseURL: baseUrl.replace(/\/+$/, ""),
        timeout: timeoutMs,
    });

    function isLocalEnvironment(): boolean {
        return (
            process.env.NODE_ENV === "development" ||
            window.location.hostname === "localhost" ||
            window.location.hostname === "127.0.0.1"
        );
    }

    function resolveToken(): string | null {
        if (typeof options?.tokenProvider === "string" && options.tokenProvider.trim()) {
            return options.tokenProvider;
        }

        if (typeof options?.tokenProvider === "function") {
            const providedToken = options.tokenProvider();

            if (providedToken?.trim()) {
                return providedToken;
            }
        }

        const storeToken =
            (localHomeStore.getState() as any)?.authentication?.token;

        console.log("🔑 STORE TOKEN:", storeToken);
        console.log("🔑 AUTH STATE:", localHomeStore.getState()?.authentication);

        if (storeToken?.trim()) return storeToken;

        const envToken =
            process.env.REACT_APP_AUTH_DEFAULT_TOKEN ??
            process.env.AUTH_DEFAULT_TOKEN;

        if (envToken?.trim()) {
            console.warn("⚠️ Using fallback ENV token (dev mode)");
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
        const isLocal = isLocalEnvironment();

        if (!token && !isLocal) throw new Error("No token");

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
            ...extra,
            url: `/${path.replace(/^\/+/, "")}`,
            method,
            data: isFormData ? data : data ?? undefined,
            responseType: extra?.responseType,
            headers: {
                ...extra?.headers,
                ...headers,
            },
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
        const isLocal = isLocalEnvironment();

        if (!token && !isLocal) throw new Error("No token");

        const isFormData =
            typeof FormData !== "undefined" && data instanceof FormData;

        const headers: Record<string, string> = {
            Accept: "application/octet-stream",
        };

        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        if (!isFormData && method !== "get") {
            headers["Content-Type"] = "application/json";
        }

        const res: AxiosResponse = await instance.request({
            url: `/${path.replace(/^\/+/, "")}`,
            method,
            data: isFormData ? data : data ?? undefined,
            responseType: "blob",
            headers,
        });

        const blob = new Blob([res.data]);

        const anchor = document.createElement("a");
        anchor.href = window.URL.createObjectURL(blob);
        anchor.download = filename || "file.bin";

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

const apiClient = createApiClient();

export default apiClient;