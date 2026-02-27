// src/features/.../apiClient.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

import Notice from '@/models/api/Notice';
import type { Feedback } from '@/features/feedback/api/Feedback';
import type RelatedInformation from '@/features/relatedInformation/api/RelatedInformation';
import Sla from '@/models/api/Sla';
import Attachment from '../models/api/Attachment';
import Catalog from '../models/api/Catalog';
import Comment from '../models/api/Comment';
import FaqCategory from '../models/api/FaqCategory';
import FaqDetail from '../models/api/FaqDetail';
import Request from '../models/api/Request';
import SearchBox from '../models/api/SearchBox';
import CreateFaqRequest from '../models/api/CreateFaqRequest';
import { localHomeStore } from '../store/localStore';

type SearchParams = Partial<SearchBox> & {
    orderId?: string;
    folio?: string;
    status?: number;
};

type HttpMethod = 'get' | 'post' | 'put' | 'patch' | 'delete';

/** Nuevos campos para PUT FAQ (alineado al backend) */
export type UpdateFaqRequest = CreateFaqRequest & {
    keepAttachmentIds?: number[];
    removeAttachmentIds?: number[];
};

export interface ApiClient {
    // util
    getAttachmentDownloadUrl(id: number): string;
    downloadFaqAttachment(id: number): Promise<Blob>;

    // Notices
    postNoticePublishingStatus(id: number, publised: boolean): Promise<void>;
    deleteNotice(id: number): Promise<void>;
    putNotice(id: number, notice: Notice): Promise<void>;
    postNotice(notice: Notice): Promise<void>;
    getNotice(id: number): Promise<Notice>;
    getNotices(): Promise<Notice[]>;

    // SLAs
    publishSla(id: number, publised: boolean): Promise<void>;
    deleteSla(id: number): Promise<void>;
    putSla(id: number, sla: Sla): Promise<void>;
    postSla(sla: Sla): Promise<number>;
    getSla(id: number): Promise<Sla>;
    getSlas(): Promise<Sla[]>;

    // Catalogs
    getCatalog(type: number, parentId?: number): Promise<Catalog[]>;

    // Requests
    getRequests(
        opts?: {
            criteria?: string;
            dateFrom?: string | Date;
            dateTo?: string | Date;
            reason?: number;
            clazz?: number;
            page?: number;
            size?: number;
        }
    ): Promise<{ data: Request[]; total: number; page: number; totalPages: number }>;

    getRequestsPaged(
        opts?: {
            criteria?: string;
            dateFrom?: string | Date;
            dateTo?: string | Date;
            reason?: number;
            clazz?: number;
            page?: number;
            size?: number;
        }
    ): Promise<{ data: Request[]; total: number; page: number; totalPages: number }>;

    getRequestsByModule(
        moduleId: number,
        opts?: {
            criteria?: string;
            dateFrom?: string | Date;
            dateTo?: string | Date;
            reason?: number;
            clazz?: number;
            page?: number;
            size?: number;
        }
    ): Promise<{ data: Request[]; total: number; page: number; totalPages: number }>;

    getRequestsByModules(
        moduleIds: number[],
        opts?: {
            criteria?: string;
            dateFrom?: string | Date;
            dateTo?: string | Date;
            reason?: number;
            clazz?: number;
            page?: number;
            size?: number;
        }
    ): Promise<{ data: Request[]; total: number; page: number; totalPages: number }>;

    getRequest(id: number): Promise<Request>;
    deleteRequest(id: number): Promise<Request>;
    getRequestComments(id: number): Promise<Comment[]>;
    getRequestAttachments(id: number): Promise<Attachment[]>;
    getRequestAttachment(requestId: number, attachmentId: number): Promise<Attachment>;
    postRequest(request: Request): Promise<string>;
    configureRequest(requestId: number, request: Request): Promise<string>;
    postRequestComments(id: number, comment: Comment): Promise<any>;
    postRequestAttachments(id: number, attachment: Attachment): Promise<void>;

    // FAQ categories
    getFaqCategories(params?: {
        page?: number;
        size?: number;
        sort?: string;
        dir?: 'asc' | 'desc';
        includeIcons?: boolean;
        includeIcon?: boolean;
        active?: string;
    }): Promise<{
        content: FaqCategory[];
        totalElements: number;
        totalPages: number;
        number: number;
        size: number;
    }>;
    getFaqCategory(id: number, params?: { includeIcons?: boolean; includeIcon?: boolean }): Promise<FaqCategory>;
    postFaqCategory(category: FaqCategory): Promise<FaqCategory>;
    putFaqCategory(id: number, category: FaqCategory): Promise<FaqCategory>;
    postCategoryPublish(id: number): Promise<any>;
    postCategoryUnpublish(id: number): Promise<any>;
    deleteCategory(id: number): Promise<void>;
    getCategoryTemplate(): Promise<Blob>;
    postCategoryBulkUpload(file: File): Promise<void>;

    // FAQ items
    getFaqTemplate(): Promise<Blob>;
    postFaqBulkUpload(file: File): Promise<void>;
    postFaq(payload: CreateFaqRequest): Promise<{ id: number }>;
    postFaqPublish(id: number): Promise<any>;
    postFaqUnpublish(id: number): Promise<any>;
    deleteFaq(id: number): Promise<void>;
    getFaqs(opts: { searchTerm?: string; categoryId?: number; size?: number; popularOnly?: boolean }): Promise<any[]>;
    getFaq(id: number): Promise<FaqDetail>;
    putFaq(id: number, payload: UpdateFaqRequest): Promise<any>;

    // Feedback
    getFeedbackList(opts?: { size?: number }): Promise<Feedback[]>;
    getFeedback(id: number): Promise<Feedback>;
    postFeedback(body: Feedback): Promise<Feedback>;
    putFeedback(id: number, body: Feedback): Promise<Feedback>;
    deleteFeedback(id: number): Promise<void>;
    publishFeedback(id: number): Promise<void>;
    unpublishFeedback(id: number): Promise<void>;

    // Related Information
    getRelatedInformationList(opts?: {
        businessUnit?: number | string;
        country?: string;
        search?: string;
        size?: number;
    }): Promise<RelatedInformation[]>;
    getRelatedInformation(id: number): Promise<RelatedInformation>;
    postRelatedInformation(body: RelatedInformation): Promise<RelatedInformation>;
    putRelatedInformation(id: number, body: RelatedInformation): Promise<RelatedInformation>;
    deleteRelatedInformation(id: number): Promise<void>;
    publishRelatedInformation(id: number): Promise<void>;
    unpublishRelatedInformation(id: number): Promise<void>;

    // Resolvers
    getResolverModules(email: string): Promise<number[]>;
    getResolverDetails(email: string): Promise<any[]>;
    getResolverById(id: number): Promise<any>;
    deleteResolver(id: number): Promise<void>;
    // (paginados)
    getAllResolvers(page?: number, size?: number): Promise<{
        content: any[];
        totalPages: number;
        totalElements: number;
        page: number;
        size: number;
    }>;

    getResolversByModule(moduleId: number, page?: number, size?: number): Promise<{
        content: any[];
        totalPages: number;
        totalElements: number;
        page: number;
        size: number;
    }>;
    upsertResolver(body: any): Promise<any>;
}

type TokenProvider = string | (() => string | null | undefined);

/**
 * Factory: prefer functional client over classes.
 * - Axios instance with baseURL and dynamic auth header (via tokenProvider)
 * - JSON/FormData/Blob handled automatically
 * - Clean typed surface; easy to test/mock
 */
export function createApiClient(options: { baseUrl: string; tokenProvider?: TokenProvider; timeoutMs?: number }): ApiClient {
    const { baseUrl, tokenProvider, timeoutMs = 15000 } = options;

    const instance: AxiosInstance = axios.create({
        baseURL: baseUrl.replace(/\/+$/, ''),
        timeout: timeoutMs,
    });

    // Small helper to resolve token from provider or store
    function resolveToken(): string | null {
        if (typeof tokenProvider === 'function') return tokenProvider() ?? null;
        if (typeof tokenProvider === 'string' && tokenProvider.trim() !== '') return tokenProvider;
        return localHomeStore.getState()?.authentication?.token ?? null;
    }

    async function request<T = any>(
        path: string,
        method: HttpMethod,
        data?: any,
        extra?: AxiosRequestConfig
    ): Promise<T> {
        const token = resolveToken();
        if (!token) throw new Error("No token");

        const isFormData = typeof FormData !== "undefined" && data instanceof FormData;

        // 👇 obtener país desde el store global
        const state: any = localHomeStore.getState();
        const countryCode = state?.configuration?.selectedTenant?.country?.name ?? "MX";


        const headers: Record<string, string> = {
            Authorization: `Bearer ${token}`,
            Accept: "application/json",
            "X-Country": countryCode,
        };

        if (!isFormData && method !== "get")
            headers["Content-Type"] = "application/json";

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

    /* ----------------- Helpers FormData ----------------- */
    const appendArray = (fd: FormData, key: string, arr?: Array<string | number>) => {
        if (!arr?.length) return;
        for (const v of arr) fd.append(key, String(v));
    };

    // NUEVO: mapea files[] -> file1/file2/file3
    const appendFilesAsSeparateFields = (fd: FormData, files?: (File | null | undefined)[]) => {
        if (!files?.length) return;
        const [f1, f2, f3] = files.filter(Boolean) as File[];
        if (f1) fd.append('file1', f1, f1.name);
        if (f2) fd.append('file2', f2, f2.name);
        if (f3) fd.append('file3', f3, f3.name);
    };
    /* ----------------- API surface ----------------- */

    function getAttachmentDownloadUrl(id: number): string {
        return `${instance.defaults.baseURL}/faqs/attachments/${id}/download`;
    }

    async function downloadFaqAttachment(id: number): Promise<Blob> {
        const token = resolveToken();
        if (!token) throw new Error('No token');

        const url = getAttachmentDownloadUrl(id);
        const res = await instance.get<Blob>(url, {
            responseType: 'blob',
            headers: { Authorization: `Bearer ${token}` },
        });
        return res.data;
    }

    // Notices
    const postNoticePublishingStatus = (id: number, publised: boolean) =>
        request<void>(`notices/${id}/publish?publish=${publised}`, 'post');
    const deleteNotice = (id: number) => request<void>(`notices/${id}`, 'delete');
    const putNotice = (id: number, notice: Notice) => request<void>(`notices/${id}`, 'put', notice);
    const postNotice = (notice: Notice) => request<void>('notices', 'post', notice);
    const getNotice = (id: number) => request<Notice>(`notices/${id}`, 'get');
    const getNotices = () => request<Notice[]>(`notices`, 'get');

    // SLAs
    const publishSla = (id: number, publised: boolean) => request<void>(`slas/${id}/publish?publish=${publised}`, 'post');
    const deleteSla = (id: number) => request<void>(`slas/${id}`, 'delete');
    const putSla = (id: number, sla: Sla) => request<void>(`slas/${id}`, 'put', sla);
    const postSla = (sla: Sla) => request<number>('slas', 'post', sla);
    const getSla = (id: number) => request<Sla>(`slas/${id}`, 'get');
    const getSlas = () => request<Sla[]>(`slas`, 'get');

    // Catalogs
    const getCatalog = (type: number, parentId?: number) => {
        const extra = parentId ? `?parentId=${parentId}` : '';
        return request<Catalog[]>(`catalogs/${type}${extra}`, 'get');
    };

    // Requests
    const getRequests = (
        opts: {
            criteria?: string;
            dateFrom?: string | Date;
            dateTo?: string | Date;
            reason?: number;
            clazz?: number;
            page?: number;
            size?: number;
        } = {}
    ) => {
        const params = new URLSearchParams();

        if (opts.criteria) params.set("criteria", String(opts.criteria));
        if (opts.dateFrom) params.set("dateFrom", new Date(opts.dateFrom).toISOString().split("T")[0]);
        if (opts.dateTo) params.set("dateTo", new Date(opts.dateTo).toISOString().split("T")[0]);
        if (opts.reason) params.set("reason", String(opts.reason));
        if (opts.clazz) params.set("clazz", String(opts.clazz));
        if (opts.page != null) params.set("page", String(opts.page));
        if (opts.size != null) params.set("size", String(opts.size));

        const qs = params.toString();

        return request<{
            data: Request[];
            total: number;
            page: number;
            totalPages: number;
        }>(`requests?${qs}`, "get");
    };

    const getRequestsPaged = (
        opts: {
            criteria?: string;
            dateFrom?: string | Date;
            dateTo?: string | Date;
            reason?: number;
            clazz?: number;
            page?: number;
            size?: number;
        } = {}
    ) => {
        const params = new URLSearchParams();

        if (opts.criteria) params.set('criteria', String(opts.criteria));
        if (opts.dateFrom) params.set('dateFrom', new Date(opts.dateFrom).toISOString().split('T')[0]);
        if (opts.dateTo) params.set('dateTo', new Date(opts.dateTo).toISOString().split('T')[0]);
        if (opts.reason) params.set('reason', String(opts.reason));
        if (opts.clazz) params.set('clazz', String(opts.clazz));
        if (opts.page) params.set('page', String(opts.page));
        if (opts.size) params.set('size', String(opts.size));

        const qs = params.toString();

        return request<{
            data: Request[];
            total: number;
            page: number;
            totalPages: number;
        }>(
            `requests?${qs}`,
            'get'
        );
    };


    const getRequest = (id: number) => request<Request>(`requests/${id}`, 'get');
    const deleteRequest = (id: number) => request<Request>(`requests/${id}`, 'delete');
    const getRequestComments = (id: number) => request<Comment[]>(`requests/${id}/comments`, 'get');
    const getRequestAttachments = (id: number) => request<Attachment[]>(`requests/${id}/attachments`, 'get');
    const getRequestAttachment = (requestId: number, attachmentId: number) =>
        request<Attachment>(`requests/${requestId}/attachments/${attachmentId}`, 'get');

    const postRequest = (req: Request) => request<string>('requests', 'post', req);
    const configureRequest = (requestId: number, req: Request) =>
        request<string>(`requests/${requestId}/configure`, 'post', req);

    const postRequestComments = (id: number, comment: Comment) =>
        request<any>(`requests/${id}/comments`, 'post', comment);

    const postRequestAttachments = (id: number, attachment: Attachment) =>
        request<void>(`requests/${id}/attachments`, 'post', attachment);

    const getRequestsByModule = (
        moduleId: number,
        opts: {
            criteria?: string;
            dateFrom?: string | Date;
            dateTo?: string | Date;
            reason?: number;
            clazz?: number;
            page?: number;
            size?: number;
        } = {}
    ) => {
        const params = new URLSearchParams();
        params.set('moduleId', String(moduleId));
        if (opts.criteria) params.set('criteria', String(opts.criteria));
        if (opts.dateFrom) params.set('dateFrom', new Date(opts.dateFrom).toISOString().split('T')[0]);
        if (opts.dateTo) params.set('dateTo', new Date(opts.dateTo).toISOString().split('T')[0]);
        if (opts.reason) params.set('reason', String(opts.reason));
        if (opts.clazz) params.set('clazz', String(opts.clazz));
        if (opts.page) params.set('page', String(opts.page));
        if (opts.size) params.set('size', String(opts.size));

        const qs = params.toString();
        // 👇 ahora devuelve el objeto paginado
        return request<{ data: Request[]; total: number; page: number; totalPages: number }>(
            `requests/by-module?${qs}`,
            'get'
        );
    };

    const getRequestsByModules = (
        moduleIds: number[],
        opts: {
            criteria?: string;
            dateFrom?: string | Date;
            dateTo?: string | Date;
            reason?: number;
            clazz?: number;
            page?: number;
            size?: number;
        } = {}
    ) => {
        const params = new URLSearchParams();

        // enviar lista de ids como moduleIds=1,2,3
        params.set("moduleIds", moduleIds.join(","));

        if (opts.criteria) params.set("criteria", String(opts.criteria));
        if (opts.dateFrom) params.set("dateFrom", new Date(opts.dateFrom).toISOString().split("T")[0]);
        if (opts.dateTo) params.set("dateTo", new Date(opts.dateTo).toISOString().split("T")[0]);
        if (opts.reason) params.set("reason", String(opts.reason));
        if (opts.clazz) params.set("clazz", String(opts.clazz));
        if (opts.page) params.set("page", String(opts.page));
        if (opts.size) params.set("size", String(opts.size));

        const qs = params.toString();

        return request<{ data: Request[]; total: number; page: number; totalPages: number }>(
            `requests/by-modules?${qs}`,
            "get"
        );
    };

    // FAQ categories
    const getFaqCategories = (params?: {
        page?: number;
        size?: number;
        sort?: string;
        dir?: 'asc' | 'desc';
        includeIcons?: boolean;
        includeIcon?: boolean;
        active?: string;
    }) => {
        const query = new URLSearchParams();

        if (params?.page != null) query.set('page', String(params.page));
        if (params?.size != null) query.set('size', String(params.size));
        if (params?.sort) query.set('sort', params.sort);
        if (params?.dir) query.set('dir', params.dir);

        const include =
            typeof params?.includeIcons === 'boolean'
                ? params.includeIcons
                : typeof params?.includeIcon === 'boolean'
                    ? params.includeIcon
                    : undefined;

        if (include !== undefined) query.set('includeIcons', String(include));
        if (params?.active) query.set('active', params.active);

        const qs = query.toString();
        return request<{
            content: FaqCategory[];
            totalElements: number;
            totalPages: number;
            number: number;
            size: number;
        }>(`faq-categories${qs ? `?${qs}` : ''}`, 'get');
    };


    const getFaqCategory = (id: number, params?: { includeIcons?: boolean; includeIcon?: boolean }) => {
        const include =
            typeof params?.includeIcons === 'boolean'
                ? params.includeIcons
                : typeof params?.includeIcon === 'boolean'
                    ? params.includeIcon
                    : undefined;

        const qs = include !== undefined ? `?includeIcons=${include}` : '';
        return request<FaqCategory>(`faqs-categories/${id}${qs}`, 'get');
    };

    const postFaqCategory = (category: FaqCategory) =>
        request<FaqCategory>('faq-categories', 'post', category);
    const putFaqCategory = (id: number, category: FaqCategory) =>
        request<FaqCategory>(`faq-categories/${id}`, 'put', category);

    const postCategoryPublish = (id: number) => request<any>(`faq-categories/${id}/publish`, 'post');
    const postCategoryUnpublish = (id: number) => request<any>(`faq-categories/${id}/unpublish`, 'post');
    const deleteCategory = (id: number) => request<void>(`faq-categories/${id}`, 'delete');

    const getCategoryTemplate = async (): Promise<Blob> => {
        const res = await fetch('/templates/categorias-template.xlsx');
        if (!res.ok) throw new Error('Template not found');
        return await res.blob();
    };

    const postCategoryBulkUpload = (file: File) => {
        if (!/\.xlsx$/i.test(file.name)) return Promise.reject(new Error('Solo se permite .xlsx'));
        const form = new FormData();
        form.append('file', file, file.name); // 👈 el nombre del campo debe ser "file"
        return request<void>('faqs-categories/bulk-upload', 'post', form);
    };

    // FAQ items
    const getFaqTemplate = async (): Promise<Blob> => {
        const token = resolveToken();
        if (!token) throw new Error('No token');
        const url = `/${'faqs/template'}`;
        const res = await instance.get(url, {
            responseType: 'blob',
            headers: {
                Authorization: `Bearer ${token}`,
                Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            },
        });
        return res.data as Blob;
    };

    const postFaqBulkUpload = (file: File) => {
        if (!/\.xlsx$/i.test(file.name)) return Promise.reject(new Error('Solo se permite .xlsx'));
        const form = new FormData();
        form.append('file', file, file.name);
        return request<void>('faqs/bulk-upload', 'post', form);
    };

    const postFaq = (payload: CreateFaqRequest) => {
        const form = new FormData();
        form.append('categoryId', String(payload.categoryId));
        form.append('question', payload.question);
        form.append('answer', payload.answer);

        payload.aliases?.forEach(a => form.append('aliases', a));
        payload.relatedIds?.forEach(id => form.append('relatedIds', String(id)));
        payload.relatedInfoIds?.forEach(id => form.append('relatedInfoIds', String(id)));
        payload.categoryIds?.forEach(id => form.append('categoryIds', String(id)));

        // antes: form.append('files', ...)
        // ahora: file1/file2/file3
        appendFilesAsSeparateFields(form, payload.files);

        return request<{ id: number }>('faqs', 'post', form);
    };

    const postFaqPublish = (id: number) => request<any>(`faqs/${id}/publish`, 'post');
    const postFaqUnpublish = (id: number) => request<any>(`faqs/${id}/unpublish`, 'post');
    const deleteFaq = (id: number) => request<void>(`faqs/${id}`, 'delete');

    const getFaqs = (opts: { searchTerm?: string; categoryId?: number; size?: number; popularOnly?: boolean }) => {
        const params = new URLSearchParams();
        if (opts?.searchTerm && opts.searchTerm.trim()) params.append('searchTerm', opts.searchTerm.trim());
        if (typeof opts?.categoryId === 'number' && opts.categoryId > 0) params.append('categoryId', String(opts.categoryId));
        if (typeof opts?.size === 'number') params.append('size', String(Math.max(1, Math.min(opts.size, 50))));
        if (typeof opts?.popularOnly === 'boolean') params.append('popularOnly', String(opts.popularOnly));

        const url = params.toString() ? `faqs?${params.toString()}` : 'faqs';
        return request<any[]>(url, 'get');
    };

    const getFaq = (id: number) => request<FaqDetail>(`faqs/${id}`, 'get');

    const putFaq = (id: number, payload: UpdateFaqRequest) => {
        const form = new FormData();
        form.append('categoryId', String(payload.categoryId));
        form.append('question', payload.question);
        form.append('answer', payload.answer);

        payload.aliases?.forEach(a => form.append('aliases', a));
        payload.relatedIds?.forEach(rid => form.append('relatedIds', String(rid)));
        payload.relatedInfoIds?.forEach(v => form.append('relatedInfoIds', String(v)));
        payload.categoryIds?.forEach(v => form.append('categoryIds', String(v)));

        // nuevos adjuntos (máx 3) -> file1/file2/file3
        appendFilesAsSeparateFields(form, payload.files);

        appendArray(form, 'keepAttachmentIds', payload.keepAttachmentIds);
        appendArray(form, 'removeAttachmentIds', payload.removeAttachmentIds);

        return request<any>(`faqs/${id}`, 'put', form);
    };

    // Feedback
    const getFeedbackList = (opts: { size?: number } = {}) => {
        const params = new URLSearchParams();
        params.append('size', String(opts.size ?? 500));
        return request<Feedback[]>(`feedback?${params.toString()}`, 'get');
    };

    const getFeedback = (id: number) => request<Feedback>(`feedback/${id}`, 'get');
    const postFeedback = (body: Feedback) => request<Feedback>('feedback', 'post', body);
    const putFeedback = (id: number, body: Feedback) => request<Feedback>(`feedback/${id}`, 'put', body);
    const deleteFeedback = (id: number) => request<void>(`feedback/${id}`, 'delete');
    const publishFeedback = (id: number) => request<void>(`feedback/${id}/publish`, 'post');
    const unpublishFeedback = (id: number) => request<void>(`feedback/${id}/unpublish`, 'post');

    // Related Information
    const getRelatedInformationList = (opts: {
        id?: number;
        businessUnit?: number | string;
        country?: string;
        size?: number;
    } = {}) => {
        const params = new URLSearchParams();

        if (opts.id != null) {
            params.append('id', String(opts.id));
        } else {
            if (opts.businessUnit != null) params.append('businessUnit', String(opts.businessUnit));
            if (opts.country) params.append('country', String(opts.country));
            params.append('size', String(opts.size ?? 100));
        }

        return request<RelatedInformation[]>(
            `related-information?${params.toString()}`,
            'get'
        );
    };

    const getRelatedInformation = (id: number) => request<RelatedInformation>(`related-information/${id}`, 'get');
    const postRelatedInformation = (body: RelatedInformation) => request<RelatedInformation>('related-information', 'post', body);
    const putRelatedInformation = (id: number, body: RelatedInformation) =>
        request<RelatedInformation>(`related-information/${id}`, 'put', body);
    const deleteRelatedInformation = (id: number) => request<void>(`related-information/${id}`, 'delete');
    const publishRelatedInformation = (id: number) => request<void>(`related-information/${id}/publish`, 'post');
    const unpublishRelatedInformation = (id: number) => request<void>(`related-information/${id}/unpublish`, 'post');

    //  Resolvers
    const getResolverModules = (email: string) => {
        const qs = `?email=${encodeURIComponent(email)}`;
        return request<number[]>(`resolvers/modules${qs}`, 'get');
    };

    const getResolverDetails = (email: string) => {
        const qs = `?email=${encodeURIComponent(email)}`;
        return request<any[]>(`resolvers${qs}`, 'get');
    };

    const getResolverById = (id: number) => {
        return request<any>(`resolvers/${id}`, 'get');
    };

    const getAllResolvers = (page: number = 0, size: number = 10) => {
        const qs = `?page=${page}&size=${size}`;
        return request<any>(`resolvers/all${qs}`, 'get');
    };

    const getResolversByModule = (moduleId: number, page: number = 0, size: number = 10) => {
        const qs = `?moduleId=${moduleId}&page=${page}&size=${size}`;
        return request<any>(`resolvers/by-module${qs}`, 'get');
    };

    const upsertResolver = (body: any) => {
        return request<any>('resolvers/upsert', 'post', body);
    };

    const deleteResolver = (id: number) => {
        return request<void>(`resolvers/${id}`, 'delete');
    };

    return {
        // util
        getAttachmentDownloadUrl,
        downloadFaqAttachment,

        // notices
        postNoticePublishingStatus,
        deleteNotice,
        putNotice,
        postNotice,
        getNotice,
        getNotices,

        // slas
        publishSla,
        deleteSla,
        putSla,
        postSla,
        getSla,
        getSlas,

        // catalogs
        getCatalog,

        // requests
        getRequests,
        getRequest,
        deleteRequest,
        getRequestComments,
        getRequestAttachments,
        getRequestAttachment,
        postRequest,
        configureRequest,
        postRequestComments,
        postRequestAttachments,
        getRequestsByModule,
        getRequestsByModules,
        getRequestsPaged,

        // faq categories
        getFaqCategories,
        getFaqCategory,
        postFaqCategory,
        putFaqCategory,
        postCategoryPublish,
        postCategoryUnpublish,
        deleteCategory,
        getCategoryTemplate,
        postCategoryBulkUpload,

        // faq items
        getFaqTemplate,
        postFaqBulkUpload,
        postFaq,
        postFaqPublish,
        postFaqUnpublish,
        deleteFaq,
        getFaqs,
        getFaq,
        putFaq,

        // feedback
        getFeedbackList,
        getFeedback,
        postFeedback,
        putFeedback,
        deleteFeedback,
        publishFeedback,
        unpublishFeedback,

        // related information
        getRelatedInformationList,
        getRelatedInformation,
        postRelatedInformation,
        putRelatedInformation,
        deleteRelatedInformation,
        publishRelatedInformation,
        unpublishRelatedInformation,

        // resolvers
        getResolverModules,
        getResolverDetails,
        getResolverById,
        getAllResolvers,
        getResolversByModule,
        upsertResolver,
        deleteResolver

    };
}
