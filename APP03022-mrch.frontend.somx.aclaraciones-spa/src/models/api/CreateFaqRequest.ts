// src/models/api/CreateFaqRequest.ts
export default interface CreateFaqRequest {
    categoryId: number;          // categoría principal (obligatoria)
    question: string;
    answer: string;
    aliases?: string[];
    relatedIds?: number[];       // FAQs relacionadas (faq_related)
    relatedInfoIds?: number[];   // información relacionada (faq_related_information_link)
    categoryIds?: number[];      // categorías adicionales (faq_category_link)
    files?: File[];              // adjuntos (PDF, imágenes, etc.)
}
