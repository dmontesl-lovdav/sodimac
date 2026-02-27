package com.sodimac.aclaraciones.api.model.dto;

import java.util.List;

public record FaqResponse(
                Long id,
                String question,
                String answer,
                String categoryName,
                List<Long> categoryIds,
                List<Long> relatedIds,
                List<Long> relatedInfoIds) {
}
