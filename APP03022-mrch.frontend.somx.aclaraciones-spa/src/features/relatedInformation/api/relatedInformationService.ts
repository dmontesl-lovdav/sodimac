import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import type RelatedInformation from './RelatedInformation';

const client = ConfigurationBuilder.client;

export const getRelatedInformation = (opts: {
    id?: number;
    businessUnit?: number | string;
    country?: string;
    search?: string;
    size?: number;
} = {}) => client.getRelatedInformationList(opts);

export const getRelatedInformationById = (id: number) =>
    client.getRelatedInformation(id);

export const createRelatedInformation = (body: RelatedInformation) =>
    client.postRelatedInformation(body);

export const updateRelatedInformation = (id: number, body: RelatedInformation) =>
    client.putRelatedInformation(id, body);

export const deleteRelatedInformation = (id: number) =>
    client.deleteRelatedInformation(id);

export const publishRelatedInformation = (id: number) =>
    client.publishRelatedInformation(id);

export const unpublishRelatedInformation = (id: number) =>
    client.unpublishRelatedInformation(id);
