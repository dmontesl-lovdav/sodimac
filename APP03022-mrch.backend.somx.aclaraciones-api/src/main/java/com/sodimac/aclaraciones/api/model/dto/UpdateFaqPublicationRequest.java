/* src/main/java/com/sodimac/aclaraciones/api/model/dto/UpdateFaqPublicationRequest.java */
package com.sodimac.aclaraciones.api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateFaqPublicationRequest(
        @JsonProperty("published") Boolean published) {
}
