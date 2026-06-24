import apiClient from '@/services/apiClient';

export function syncUserToCatalogs(): void {
    if (typeof window === 'undefined') return;
    void apiClient.request<{ success?: boolean; message?: string }>('security/user-utility', 'post', {}).catch(() => {
    });
}
