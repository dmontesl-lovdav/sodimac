// src/features/.../apiClient.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { localHomeStore } from '../store/localStore';

type HttpMethod = 'get' | 'post' | 'put' | 'patch' | 'delete';

export abstract class ApiClient {
    private defaultToken?: string;
    private axios: AxiosInstance;
    private baseUrl: string;

    protected constructor(baseUrl = process.env.API_BASE_URL || "") {
        this.defaultToken = process.env.AUTH_DEFAULT_TOKEN && process.env.AUTH_DEFAULT_TOKEN.trim() !== ''
            ? process.env.AUTH_DEFAULT_TOKEN
            : undefined;
        this.baseUrl = baseUrl.replace(/\/+$/, '');
        this.axios = axios.create();
    }

    protected resolveToken(): string | null {
        return this.defaultToken || ((localHomeStore.getState() as any)?.authentication?.token ?? null);
    }

    protected async execute<T>(path: string, method: HttpMethod, data?: any, options?: AxiosRequestConfig): Promise<T> {
        const token = this.resolveToken();
        if (!token) { throw new Error('No token'); }

        const headers: Record<string, string> = {
            Authorization: `Bearer ${token}`,
            Accept: 'application/json',
            'Content-Type': 'application/json',
        };

        const res: AxiosResponse<T> = await axios.request({
            url: `${this.baseUrl}/${path.replace(/^\/+/, '')}`,
            method,
            data: data ?? undefined,
            headers,
            responseType: options?.responseType,
            ...options,
        });

        return res.data as T;
    }

    protected async fetchDocument(path: string) {
        const token = this.resolveToken();
        
        if (!token) { throw new Error('No token'); }
        const response: AxiosResponse = await axios.get(`${this.baseUrl}/${path.replace(/^\/+/, '')}`);
        return response;

    }

    protected async executeRequestBinary(invoice: File, path: string) {
        const formdata = new FormData();
        formdata.append("file", invoice);

        const requestOptions = {
            method: "POST",
            body: formdata
        };

        return await fetch(`${this.baseUrl}/${path.replace(/^\/+/, '')}`, requestOptions)
            .then((response) => response.json())
            .then((result) => result)
            .catch((error) => error);
    }

    protected async executeBinary(path: string, method: HttpMethod, data?: any, options?: AxiosRequestConfig, filename?: string) {
        const token = this.resolveToken();
        if (!token) { throw new Error('No token'); }

        const headers: Record<string, string> = {
            Authorization: `Bearer ${token}`,
            Accept: 'application/json',
            'Content-Type': 'application/json',
        };

        const response: AxiosResponse = await axios.request({
            url: `${this.baseUrl}/${path.replace(/^\/+/, '')}`,
            method,
            data: data ?? undefined,
            headers,
            responseType: "blob",
            ...options,
        });

        const anchor = document.createElement('a');
        anchor.href = window.URL.createObjectURL(response.data);
        anchor.download = filename || "file.bin";

        document.body.appendChild(anchor);
        anchor.click();

        document.body.removeChild(anchor);
        window.URL.revokeObjectURL(anchor.href);
    }
}