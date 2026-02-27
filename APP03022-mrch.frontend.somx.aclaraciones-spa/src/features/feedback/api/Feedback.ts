export interface FeedbackAnswer {
    id?: number;
    text: string;
    position?: number;
}

/** Pregunta de feedback con sus respuestas */
export interface Feedback {
    id?: number;
    question: string;
    isActive?: boolean;
    answers: FeedbackAnswer[];
}
