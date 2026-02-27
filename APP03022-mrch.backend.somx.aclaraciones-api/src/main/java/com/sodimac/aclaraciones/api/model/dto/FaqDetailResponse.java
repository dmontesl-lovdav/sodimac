package com.sodimac.aclaraciones.api.model.dto;

import java.util.List;

public record FaqDetailResponse(
                Long id,
                Long categoryId, // principal (legacy)
                String question,
                String answer,
                List<String> aliases,
                List<Long> relatedIds, // FAQ ↔ FAQ (tabla faq_related)
                List<AttachmentInfo> attachments,

                // === NUEVOS ===
                List<Long> categoryIds, // todas las categorías (faq_category_link)
                List<Long> relatedInfoIds // related_information (faq_related_information_link)
) {

        public record AttachmentInfo(
                        Long id,
                        String fileName,
                        String contentType,
                        Integer sizeKb,
                        String downloadUrl) {
        }
}
