// src/main/java/com/sodimac/aclaraciones/api/service/relatedInformation/command/RelatedInformationCommandService.java
package com.sodimac.aclaraciones.api.service.relatedInformation.command;

import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sodimac.aclaraciones.api.model.dto.RelatedInformationDto;
import com.sodimac.aclaraciones.api.model.entity.RelatedInformation;
import com.sodimac.aclaraciones.api.repository.RelatedInformationRepository;

@Service
@Transactional
public class RelatedInformationCommandService {

    private final RelatedInformationRepository repo;

    public RelatedInformationCommandService(RelatedInformationRepository repo) {
        this.repo = repo;
    }

    /* ===================== CREATE ====================== */
    public RelatedInformationDto create(RelatedInformationDto dto) {
        RelatedInformation ent = new RelatedInformation();
        ent.setTitle(dto.title());
        ent.setLink(dto.link());

        // Save image only if provided with content
        if (dto.image() != null && !dto.image().isBlank()) {
            ent.setImageData(toBytes(dto.image()));
        }
        if (dto.imageName() != null && !dto.imageName().isBlank()) {
            ent.setImagePath(dto.imageName());
        }

        ent.setIsActive(dto.isActive() == null ? Boolean.TRUE : dto.isActive());

        // new fields
        ent.setBusinessUnitId(dto.businessUnitId());
        ent.setCountryId(dto.countryId());

        return toDto(repo.save(ent));
    }

    /* ===================== UPDATE ====================== */
    public RelatedInformationDto update(Long id, RelatedInformationDto dto) {
        RelatedInformation ent = repo.findById(id)
                .orElseThrow(() -> notFound(id));

        ent.setTitle(dto.title());
        ent.setLink(dto.link());

        /*
         * Image update policy:
         * - dto.image == null -> do not touch
         * - dto.image == "" -> clear imageData
         * - dto.image has content -> replace imageData
         *
         * Same rule for imageName.
         */
        if (dto.image() != null) {
            if (dto.image().isBlank()) {
                ent.setImageData(null); // clear
            } else {
                ent.setImageData(toBytes(dto.image())); // replace
            }
        }

        if (dto.imageName() != null) {
            if (dto.imageName().isBlank()) {
                ent.setImagePath(null); // clear
            } else {
                ent.setImagePath(dto.imageName()); // replace
            }
        }

        if (dto.isActive() != null) {
            ent.setIsActive(dto.isActive());
        }

        // idempotent fields
        ent.setBusinessUnitId(dto.businessUnitId());
        ent.setCountryId(dto.countryId());

        return toDto(ent); // @Transactional flush
    }

    /* ============ PUBLICAR / DESPUBLICAR ================ */
    public RelatedInformationDto updatePublication(Long id, Boolean published) {
        RelatedInformation ent = repo.findById(id)
                .orElseThrow(() -> notFound(id));
        ent.setIsActive(Boolean.TRUE.equals(published));
        return toDto(ent);
    }

    /* ===================== DELETE ====================== */
    public void delete(Long id) {
        if (!repo.existsById(id))
            throw notFound(id);
        repo.deleteById(id);
    }

    /* ===================== helpers ===================== */
    private byte[] toBytes(String b64) {
        try {
            return Base64.getDecoder().decode(b64);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Imagen Base64 inválida", ex);
        }
    }

    private RelatedInformationDto toDto(RelatedInformation e) {
        String img = (e.getImageData() == null) ? null
                : Base64.getEncoder().encodeToString(e.getImageData());

        return new RelatedInformationDto(
                e.getId(),
                e.getTitle(),
                e.getLink(),
                img,
                e.getImagePath(),
                e.getIsActive(),
                e.getBusinessUnitId(),
                e.getCountryId());
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "RelatedInformation %d no existe".formatted(id));
    }
}
