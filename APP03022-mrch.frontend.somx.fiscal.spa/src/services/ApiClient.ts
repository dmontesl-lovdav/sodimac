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

    console.log("[ApiClient] APP_DEV:", APP_DEV);
    console.log("[ApiClient] baseUrl resolved:", baseUrl);
    console.log("[ApiClient] timeoutMs:", timeoutMs);

    const instance: AxiosInstance = axios.create({
        baseURL: baseUrl.replace(/\/+$/, ""),
        timeout: timeoutMs,
    });

    function resolveToken(): string | null {
        console.log("[ApiClient][resolveToken] start");

        if (APP_DEV) {
            console.log("[ApiClient][resolveToken] APP_DEV=true, skipping token resolution");
            return null;
        }

        console.log("[ApiClient][resolveToken] local store full:", localHomeStore.getState());
        console.log(
            "[ApiClient][resolveToken] local authentication:",
            (localHomeStore.getState() as any)?.authentication
        );

        if (typeof options?.tokenProvider === "function") {
            const providedToken = options.tokenProvider();

            console.log(
                "[ApiClient][resolveToken] tokenProvider(function) returned token?:",
                Boolean(providedToken?.trim())
            );

            if (providedToken?.trim()) {
                console.log("[ApiClient][resolveToken] using token from tokenProvider(function)");
                return providedToken;
            }
        }

        if (typeof options?.tokenProvider === "string" && options.tokenProvider.trim()) {
            console.log("[ApiClient][resolveToken] using token from tokenProvider(string)");
            return options.tokenProvider;
        }

        const storeToken =
            (localHomeStore.getState() as any)?.authentication?.token ||
            (localHomeStore.getState() as any)?.authentication?.idToken;

        console.log(
            "[ApiClient][resolveToken] storeToken exists?:",
            Boolean(storeToken?.trim())
        );
        console.log(
            "[ApiClient][resolveToken] storeToken preview:",
            storeToken ? `${storeToken.slice(0, 20)}...` : null
        );

        if (storeToken?.trim()) {
            console.log("[ApiClient][resolveToken] using token from localHomeStore");
            return storeToken;
        }

        const envToken =
            process.env.REACT_APP_AUTH_DEFAULT_TOKEN ||
            process.env.AUTH_DEFAULT_TOKEN;

        console.log(
            "[ApiClient][resolveToken] envToken exists?:",
            Boolean(envToken?.trim())
        );

        if (envToken?.trim()) {
            console.log("[ApiClient][resolveToken] using fallback env token");
            return envToken;
        }

        console.warn("[ApiClient][resolveToken] no token found");
        return null;
    }

    async function request<T = any>(
        path: string,
        method: HttpMethod,
        data?: any,
        extra?: AxiosRequestConfig
    ): Promise<T> {
        console.log("[ApiClient][request] start");
        console.log("[ApiClient][request] path:", path);
        console.log("[ApiClient][request] method:", method);
        console.log("[ApiClient][request] data:", data);
        console.log("[ApiClient][request] extra:", extra);

        const token = resolveToken();

        console.log("[ApiClient][request] token exists?:", Boolean(token));

        if (!APP_DEV && !token) {
            console.error("[ApiClient][request] No token - aborting request");
            console.error(
                "[ApiClient][request] local store at abort:",
                localHomeStore.getState()
            );
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

        console.log("[ApiClient][request] final url:", `/${path.replace(/^\/+/, "")}`);
        console.log("[ApiClient][request] headers:", headers);
        console.log("[ApiClient][request] isFormData:", isFormData);

        const res: AxiosResponse<T> = await instance.request({
            url: `/${path.replace(/^\/+/, "")}`,
            method,
            data: isFormData ? data : data ?? undefined,
            headers,
            responseType: extra?.responseType,
            ...extra,
        });

        console.log("[ApiClient][request] success status:", res.status);
        console.log("[ApiClient][request] success data:", res.data);

        return res.data as T;
    }

    async function requestBinary(
        path: string,
        method: HttpMethod,
        data?: any,
        filename?: string
    ): Promise<void> {
        console.log("[ApiClient][requestBinary] start");
        console.log("[ApiClient][requestBinary] path:", path);
        console.log("[ApiClient][requestBinary] method:", method);
        console.log("[ApiClient][requestBinary] filename:", filename);

        const token = resolveToken();

        console.log("[ApiClient][requestBinary] token exists?:", Boolean(token));

        if (!APP_DEV && !token) {
            console.error("[ApiClient][requestBinary] No token - aborting request");
            console.error(
                "[ApiClient][requestBinary] local store at abort:",
                localHomeStore.getState()
            );
            throw new Error("No token");
        }

        const headers: Record<string, string> = {};

        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        console.log("[ApiClient][requestBinary] final url:", `/${path.replace(/^\/+/, "")}`);
        console.log("[ApiClient][requestBinary] headers:", headers);

        const res: AxiosResponse = await instance.request({
            url: `/${path.replace(/^\/+/, "")}`,
            method,
            data,
            responseType: "blob",
            headers,
        });

        console.log("[ApiClient][requestBinary] success status:", res.status);

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