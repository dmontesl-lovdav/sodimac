package com.sodimac.aclaraciones.api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** PATCH body → cambia published ↔︎ unpublished. */
public record UpdateFaqCategoryPublicationRequest(
        @JsonProperty("published") Boolean published) {
}
