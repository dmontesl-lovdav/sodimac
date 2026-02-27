// src/features/feedback/api/feedbackService.ts
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import type { Feedback } from './Feedback';

const client = ConfigurationBuilder.client;

/* list */
export const getFeedback = (opts: { size?: number } = {}): Promise<Feedback[]> =>
    client.getFeedbackList(opts);

/* get by id */
export const getFeedbackById = (id: number): Promise<Feedback> =>
    client.getFeedback(id);

/* create */
export const createFeedback = (body: Feedback): Promise<Feedback> =>
    client.postFeedback(body);

/* update */
export const updateFeedback = (id: number, body: Feedback): Promise<Feedback> =>
    client.putFeedback(id, body);

/* delete */
export const deleteFeedback = (id: number): Promise<void> =>
    client.deleteFeedback(id);

/* publication */
export const publishFeedback = (id: number): Promise<void> =>
    client.publishFeedback(id);

export const unpublishFeedback = (id: number): Promise<void> =>
    client.unpublishFeedback(id);
