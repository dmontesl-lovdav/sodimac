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
        process.env.API_BASE_URL ??
        "";

    const timeoutMs = options?.timeoutMs ?? 15000;

    const instance: AxiosInstance = axios.create({
        baseURL: baseUrl.replace(/\/+$/, ""),
        timeout: timeoutMs,
    });

    function resolveToken(): string | null {
        const storeToken =
            (localHomeStore.getState() as any)?.authentication?.token;

        console.log("🔑 STORE TOKEN:", storeToken);
        console.log("🔑 AUTH STATE:", localHomeStore.getState()?.authentication);

        if (storeToken?.trim()) return storeToken;

        const envToken =
            process.env.REACT_APP_AUTH_DEFAULT_TOKEN ||
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
        if (!token) throw new Error("No token");

        const isFormData =
            typeof FormData !== "undefined" && data instanceof FormData;

        const headers: Record<string, string> = {
            Authorization: `Bearer ${token}`,
            Accept: "application/json",
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
    }

    async function requestBinary(
        path: string,
        method: HttpMethod,
        data?: any,
        filename?: string
    ): Promise<void> {
        const token = resolveToken();
        if (!token) throw new Error("No token");

        const res: AxiosResponse = await instance.request({
            url: `/${path.replace(/^\/+/, "")}`,
            method,
            data,
            responseType: "blob",
            headers: {
                Authorization: `Bearer ${token}`,
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
    }

    return {
        request,
        requestBinary,
    };
}