/* features/faq/api/faqService.ts */
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';

const client = ConfigurationBuilder.client;

/** Categorías (para Temas de ayuda) */
export const getCategories = () => client.getFaqCategories();

/** FAQ detail */
export const getFaq = (id: number) => client.getFaq(id);

/** Crear FAQ */
export const postFaq = (payload: any) => client.postFaq(payload);

/** Actualizar FAQ */
export const putFaq = (id: number, payload: any) => client.putFaq(id, payload);

/** >>> Información relacionada <<< */
export const getRelatedInformationList = (opts?: {
    businessUnit?: number | string;
    country?: string;
    search?: string;
    size?: number;
}) => client.getRelatedInformationList(opts);
