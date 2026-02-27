package com.sodimac.aclaraciones.api.service.faq.command.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.aclaraciones.api.model.dto.FaqDetailResponse;
import com.sodimac.aclaraciones.api.model.dto.filter.FaqFilter;
import com.sodimac.aclaraciones.api.model.dto.view.FaqView;
import com.sodimac.aclaraciones.api.model.entity.Faq;
import com.sodimac.aclaraciones.api.model.entity.FaqAlias;
import com.sodimac.aclaraciones.api.repository.FaqRepository;
import com.sodimac.aclaraciones.api.repository.FaqCategoryLinkRepository;
import com.sodimac.aclaraciones.api.repository.FaqRelatedInformationLinkRepository;

@Service
public class FaqQueryService {

        private final FaqRepository faqs;
        private final FaqCategoryLinkRepository categoryLinkRepo;
        private final FaqRelatedInformationLinkRepository relatedInfoLinkRepo;

        public FaqQueryService(
                        FaqRepository faqs,
                        FaqCategoryLinkRepository categoryLinkRepo,
                        FaqRelatedInformationLinkRepository relatedInfoLinkRepo) {
                this.faqs = faqs;
                this.categoryLinkRepo = categoryLinkRepo;
                this.relatedInfoLinkRepo = relatedInfoLinkRepo;
        }

        /* ---------- lista / búsqueda ---------- */
        public List<FaqView> find(FaqFilter f) {
                int limit = f.limit();
                String term = (f.searchTerm() != null && !f.searchTerm().isBlank())
                                ? f.searchTerm().trim()
                                : null;

                return faqs.search(
                                f.normalizedCategoryId(),
                                term,
                                Boolean.TRUE.equals(f.popularOnly()),
                                PageRequest.of(0, limit));
        }

        /* ---------- proyección liviana ---------- */
        public FaqView findById(Long id) {
                return faqs.findViewById(id).orElse(null);
        }

        /* ---------- detalle completo (edición) ---------- */
        @Transactional(readOnly = true) // mantiene la sesión abierta para LAZY
        public FaqDetailResponse findDetailById(Long id) {

                return faqs.findById(id).map(f -> {

                        /* ----- alias ------ */
                        List<String> aliases = f.getAliases().stream()
                                        .filter(a -> Boolean.TRUE.equals(a.getIsActive()))
                                        .map(FaqAlias::getQuestion)
                                        .toList();

                        /* ----- relacionadas (FAQ↔FAQ) ------ */
                        List<Long> relatedIds = f.getRelated().stream()
                                        .map(Faq::getId)
                                        .toList();

                        /* ----- adjuntos (solo metadatos) ------ */
                        List<FaqDetailResponse.AttachmentInfo> atts = f.getAttachments().stream()
                                        .filter(a -> Boolean.TRUE.equals(a.getIsActive()))
                                        .map(a -> new FaqDetailResponse.AttachmentInfo(
                                                        a.getId(),
                                                        a.getFileName(),
                                                        a.getContentType(),
                                                        a.getSizeKb(),
                                                        // endpoint descarga
                                                        "/faqs/%d/attachments/%d".formatted(f.getId(), a.getId())))
                                        .toList();

                        /* ----- NUEVO: categorías múltiples y related-information ------ */
                        List<Long> categoryIds = categoryLinkRepo.findCategoryIdsByFaqId(f.getId());
                        List<Long> relatedInfoIds = relatedInfoLinkRepo.findRelatedInformationIdsByFaqId(f.getId());

                        return new FaqDetailResponse(
                                        f.getId(),
                                        f.getCategory().getId(), // principal (legacy)
                                        f.getQuestion(),
                                        f.getAnswer(),
                                        aliases,
                                        relatedIds,
                                        atts,
                                        categoryIds,
                                        relatedInfoIds);
                }).orElse(null);
        }
}
