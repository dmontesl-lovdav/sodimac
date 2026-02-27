import Attachment from "./Attachment";
import Comment from "./Comment";


export default interface Request {
    id: number;
    creationTime: Date;
    elapsedTime: number;
    status: number;
    orderId: string;
    reason: number;
    detail: number;
    description: string;
    requester: string;
    operator: string;

    attachments: Attachment[];
    comments: Comment[];
}
