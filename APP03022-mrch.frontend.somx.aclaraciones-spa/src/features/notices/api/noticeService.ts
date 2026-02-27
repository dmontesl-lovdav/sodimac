import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import type { Notice } from './notice';

const client = ConfigurationBuilder.client as any;
const BASE = 'notices';

/** Listado de Notices (wrap al client existente) */
export async function getNotices(opts: { size?: number } = {}): Promise<Notice[]> {
    const data = await client.getNotices();
    return (Array.isArray(data) ? data : []) as Notice[];
}

/** Publicar / Despublicar */
export async function publishNotice(id: number) {
    await client.postNoticePublishingStatus(id, true);
}
export async function unpublishNotice(id: number) {
    await client.postNoticePublishingStatus(id, false);
}

/** Eliminar */
export async function deleteNotice(id: number) {
    await client.deleteNotice(id);
}

