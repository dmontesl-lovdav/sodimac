/*---------------------------------------------------------------------------*/
/* src/main/java/com/sodimac/aclaraciones/api/service/command/FaqCommandService.java
/*---------------------------------------------------------------------------*/
package com.sodimac.aclaraciones.api.service.faq.command.impl;

import java.io.IOException;

import com.sodimac.aclaraciones.api.model.dto.CreateFaqRequest;
import com.sodimac.aclaraciones.api.model.dto.FaqResponse;
import com.sodimac.aclaraciones.api.model.dto.UpdateFaqRequest;

/**
 * Operaciones de escritura (Command side) para FAQ.
 */
public interface FaqCommandService {

    /** Alta de una nueva FAQ (con alias, adjuntos, etc.). */
    FaqResponse createFaq(CreateFaqRequest request) throws IOException;

    /** Activa / desactiva la publicación y devuelve el estado resultante. */
    FaqResponse updatePublication(Long faqId, Boolean published);

    /** Eliminación física de la FAQ (404 si no existe). */
    void deleteFaq(Long faqId);

    /** Edición completa de la FAQ. */
    FaqResponse updateFaq(Long faqId, UpdateFaqRequest request) throws IOException;
}
