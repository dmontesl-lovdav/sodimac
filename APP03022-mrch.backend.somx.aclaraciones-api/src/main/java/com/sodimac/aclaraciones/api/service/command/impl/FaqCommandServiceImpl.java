/*---------------------------------------------------------------------------*/
/* src/main/java/com/sodimac/aclaraciones/api/service/command/impl/FaqCommandServiceImpl.java
/*---------------------------------------------------------------------------*/
package com.sodimac.aclaraciones.api.service.command.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sodimac.aclaraciones.api.model.dto.CreateFaqRequest;
import com.sodimac.aclaraciones.api.model.dto.FaqResponse;
import com.sodimac.aclaraciones.api.model.dto.UpdateFaqRequest;
import com.sodimac.aclaraciones.api.model.entity.Faq;
import com.sodimac.aclaraciones.api.model.entity.FaqAlias;
import com.sodimac.aclaraciones.api.model.entity.FaqAttachment;
import com.sodimac.aclaraciones.api.model.entity.FaqCategory;
import com.sodimac.aclaraciones.api.model.entity.FaqCategoryLink;
import com.sodimac.aclaraciones.api.model.entity.FaqRelatedInformationLink;
import com.sodimac.aclaraciones.api.model.entity.RelatedInformation; // <- nombre correcto

import com.sodimac.aclaraciones.api.repository.FaqAliasRepository;
import com.sodimac.aclaraciones.api.repository.FaqAttachmentRepository;
import com.sodimac.aclaraciones.api.repository.FaqCategoryLinkRepository;
import com.sodimac.aclaraciones.api.repository.FaqCategoryRepository;
import com.sodimac.aclaraciones.api.repository.FaqRelatedInformationLinkRepository;
import com.sodimac.aclaraciones.api.repository.FaqRepository;
import com.sodimac.aclaraciones.api.repository.RelatedInformationRepository;

/** Implementación de comandos (writes) para FAQ. */
@Service
@Transactional
public class FaqCommandServiceImpl
        implements com.sodimac.aclaraciones.api.service.faq.command.impl.FaqCommandService {

    private static final long MAX_FAQ_BYTES = 10L * 1024 * 1024; // 10 MB

    private final FaqRepository faqRepo;
    private final FaqCategoryRepository categoryRepo;
    private final FaqAliasRepository aliasRepo;
    private final FaqAttachmentRepository attachRepo;

    // Repos de vínculos e información relacionada
    private final FaqCategoryLinkRepository categoryLinkRepo;
    private final FaqRelatedInformationLinkRepository relatedInfoLinkRepo;
    private final RelatedInformationRepository relatedInformationRepo;

    public FaqCommandServiceImpl(
            FaqRepository faqRepo,
            FaqCategoryRepository categoryRepo,
            FaqAliasRepository aliasRepo,
            FaqAttachmentRepository attachRepo,
            FaqCategoryLinkRepository categoryLinkRepo,
            FaqRelatedInformationLinkRepository relatedInfoLinkRepo,
            RelatedInformationRepository relatedInformationRepo) {
        this.faqRepo = faqRepo;
        this.categoryRepo = categoryRepo;
        this.aliasRepo = aliasRepo;
        this.attachRepo = attachRepo;
        this.categoryLinkRepo = categoryLinkRepo;
        this.relatedInfoLinkRepo = relatedInfoLinkRepo;
        this.relatedInformationRepo = relatedInformationRepo;
    }

    /* ------------------------------------------------------------------ */
    /* 1. Alta */
    /* ------------------------------------------------------------------ */
    @Override
    public FaqResponse createFaq(CreateFaqRequest r) throws IOException {

        /* -------- categoría principal -------- */
        FaqCategory cat = categoryRepo.findById(r.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no existe"));

        /* -------- FAQ borrador -------- */
        Faq faq = new Faq();
        faq.setCategory(cat);
        faq.setQuestion(r.question());
        faq.setAnswer(r.answer());

        /* guarda y obtén el definitivo (con ID) */
        faq = faqRepo.save(faq);

        /* -------- alias -------- */
        if (r.aliases() != null && !r.aliases().isEmpty()) {
            for (String q : r.aliases()) {
                if (q != null && !q.isBlank()) {
                    aliasRepo.save(new FaqAlias(faq, q.trim()));
                }
            }
        }

        /* -------- adjuntos (valida tope 10MB) -------- */
        addNewFilesCheckingQuota(faq, r.files(), 0L);

        /* -------- FAQ-FAQ relacionadas (existente) -------- */
        if (r.relatedIds() != null && !r.relatedIds().isEmpty()) {
            faq.setRelated(faqRepo.findAllById(r.relatedIds())); // @ManyToMany en Faq
        }

        /*
         * -------- CATEGORÍAS (link table) --------
         * SIEMPRE insertar la principal en faq_category_link
         */
        categoryLinkRepo.save(new FaqCategoryLink(faq, cat));

        // extras (evita null, duplicados y la principal)
        if (r.categoryIds() != null && !r.categoryIds().isEmpty()) {
            for (Long cid : r.categoryIds().stream().distinct().toList()) {
                if (cid == null || cid.equals(r.categoryId()))
                    continue;
                FaqCategory extra = categoryRepo.findById(cid)
                        .orElseThrow(() -> new IllegalArgumentException("Categoría no existe: " + cid));
                categoryLinkRepo.save(new FaqCategoryLink(faq, extra));
            }
        }

        /* -------- Información relacionada (múltiple) -------- */
        if (r.relatedInfoIds() != null && !r.relatedInfoIds().isEmpty()) {
            List<RelatedInformation> items = relatedInformationRepo.findAllById(r.relatedInfoIds());
            for (RelatedInformation ri : items) {
                relatedInfoLinkRepo.save(new FaqRelatedInformationLink(faq, ri));
            }
        }

        // --- armar respuesta de 7 campos ---
        List<Long> categoryIds = categoryLinkRepo.findCategoryIdsByFaqId(faq.getId());
        List<Long> relatedIds = faq.getRelated() == null
                ? List.of()
                : faq.getRelated().stream().map(Faq::getId).toList();
        List<Long> relatedInfoIds = relatedInfoLinkRepo.findRelatedInformationIdsByFaqId(faq.getId());

        return new FaqResponse(
                faq.getId(),
                faq.getQuestion(),
                faq.getAnswer(),
                cat.getName(),
                categoryIds,
                relatedIds,
                relatedInfoIds);
    }

    /* ------------------------------------------------------------------ */
    /* 2. Publicar / despublicar */
    /* ------------------------------------------------------------------ */
    @Override
    @Transactional
    public FaqResponse updatePublication(Long faqId, Boolean published) {

        if (published == null) {
            throw new IllegalArgumentException("`published` no puede ser null");
        }

        int rows = faqRepo.setPublication(faqId, published);
        if (rows == 0) {
            throw new IllegalArgumentException("FAQ no existe");
        }

        Faq faq = faqRepo.findById(faqId)
                .orElseThrow(() -> new IllegalStateException("Inconsistencia al refrescar FAQ"));

        FaqCategory cat = faq.getCategory();

        // --- armar respuesta de 7 campos ---
        List<Long> categoryIds = categoryLinkRepo.findCategoryIdsByFaqId(faqId);
        List<Long> relatedIds = faq.getRelated() == null
                ? List.of()
                : faq.getRelated().stream().map(Faq::getId).toList();
        List<Long> relatedInfoIds = relatedInfoLinkRepo.findRelatedInformationIdsByFaqId(faqId);

        return new FaqResponse(
                faq.getId(),
                faq.getQuestion(),
                faq.getAnswer(),
                (cat != null ? cat.getName() : null),
                categoryIds,
                relatedIds,
                relatedInfoIds);
    }

    /* ------------------------------------------------------------------ */
    /* 3. Borrado */
    /* ------------------------------------------------------------------ */
    @Override
    public void deleteFaq(Long faqId) {

        Faq faq = faqRepo.findById(faqId)
                .orElseThrow(() -> new IllegalArgumentException("FAQ no existe"));

        /*
         * Si los mapeos llevan cascade = ALL + orphanRemoval = true,
         * delete(faq) cascarea a alias / adjuntos
         */
        faqRepo.delete(faq);
    }

    /* ------------------------------------------------------------------ */
    /* 1b. Edición (incremental + cuota 10MB) */
    /* ------------------------------------------------------------------ */
    @Override
    public FaqResponse updateFaq(Long id, UpdateFaqRequest r) throws IOException {
        Faq faq = faqRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ no existe"));

        /* -------- categoría principal -------- */
        if (r.categoryId() != null) {
            if (faq.getCategory() == null || !r.categoryId().equals(faq.getCategory().getId())) {
                FaqCategory nueva = categoryRepo.findById(r.categoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Categoría no existe"));
                faq.setCategory(nueva);
            }
        }

        /* -------- pregunta / respuesta -------- */
        if (r.question() != null)
            faq.setQuestion(r.question());
        if (r.answer() != null)
            faq.setAnswer(r.answer());

        /* -------- alias (reemplazo completo) -------- */
        faq.getAliases().clear();
        if (r.aliases() != null) {
            for (String a : r.aliases()) {
                if (a != null && !a.isBlank()) {
                    faq.getAliases().add(new FaqAlias(faq, a.trim()));
                }
            }
        }

        /* -------- FAQ-FAQ relacionadas (replace) -------- */
        faq.getRelated().clear();
        if (r.relatedIds() != null && !r.relatedIds().isEmpty()) {
            faq.getRelated().addAll(faqRepo.findAllById(r.relatedIds()));
        }

        /* -------- CATEGORÍAS (replace): principal + extras -------- */
        categoryLinkRepo.deleteAllByFaqId(id);

        // principalId = el que viene en request, o el ya asignado
        Long principalId = (r.categoryId() != null)
                ? r.categoryId()
                : (faq.getCategory() != null ? faq.getCategory().getId() : null);

        if (principalId == null) {
            throw new IllegalArgumentException("categoryId requerido para actualizar categorías");
        }

        FaqCategory principal = categoryRepo.findById(principalId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no existe"));

        // inserta principal en link table
        categoryLinkRepo.save(new FaqCategoryLink(faq, principal));

        // inserta extras evitando duplicar
        if (r.categoryIds() != null && !r.categoryIds().isEmpty()) {
            for (Long cid : r.categoryIds().stream().distinct().toList()) {
                if (cid == null || cid.equals(principalId))
                    continue;
                FaqCategory extra = categoryRepo.findById(cid)
                        .orElseThrow(() -> new IllegalArgumentException("Categoría no existe: " + cid));
                categoryLinkRepo.save(new FaqCategoryLink(faq, extra));
            }
        }

        /* -------- información relacionada (replace) -------- */
        relatedInfoLinkRepo.deleteAllByFaqId(id);
        if (r.relatedInfoIds() != null && !r.relatedInfoIds().isEmpty()) {
            List<RelatedInformation> items = relatedInformationRepo.findAllById(r.relatedInfoIds());
            for (RelatedInformation ri : items) {
                relatedInfoLinkRepo.save(new FaqRelatedInformationLink(faq, ri));
            }
        }

        /*
         * -------- adjuntos (gestión incremental) --------
         */
        long freedKbByRemove = 0L;
        if (r.removeAttachmentIds() != null && !r.removeAttachmentIds().isEmpty()) {
            var current = attachRepo.findByFaq_IdAndIsActiveTrue(id);
            Set<Long> toRemove = new HashSet<>(r.removeAttachmentIds());
            for (FaqAttachment a : current) {
                if (toRemove.contains(a.getId()) && Boolean.TRUE.equals(a.getIsActive())) {
                    a.setIsActive(false); // soft delete
                    freedKbByRemove += a.getSizeKb();
                    attachRepo.save(a);
                }
            }
        }

        long freedKbByKeep = 0L;
        if (r.keepAttachmentIds() != null && !r.keepAttachmentIds().isEmpty()) {
            var current = attachRepo.findByFaq_IdAndIsActiveTrue(id);
            Set<Long> keep = new HashSet<>(r.keepAttachmentIds());
            for (FaqAttachment a : current) {
                if (!keep.contains(a.getId()) && Boolean.TRUE.equals(a.getIsActive())) {
                    a.setIsActive(false); // soft delete
                    freedKbByKeep += a.getSizeKb();
                    attachRepo.save(a);
                }
            }
        }

        long freedKbTotal = freedKbByRemove + freedKbByKeep;
        addNewFilesCheckingQuota(faq, r.files(), freedKbTotal);

        Faq saved = faqRepo.save(faq);

        // --- armar respuesta de 7 campos ---
        List<Long> categoryIds = categoryLinkRepo.findCategoryIdsByFaqId(saved.getId());
        List<Long> relatedIds = saved.getRelated() == null
                ? List.of()
                : saved.getRelated().stream().map(Faq::getId).toList();
        List<Long> relatedInfoIds = relatedInfoLinkRepo.findRelatedInformationIdsByFaqId(saved.getId());

        return new FaqResponse(
                saved.getId(),
                saved.getQuestion(),
                saved.getAnswer(),
                saved.getCategory() != null ? saved.getCategory().getName() : null,
                categoryIds,
                relatedIds,
                relatedInfoIds);
    }

    /* ============================ Helpers ============================ */

    private void addNewFilesCheckingQuota(Faq faq, List<MultipartFile> files, long freedKb) throws IOException {
        if (files == null || files.isEmpty())
            return;

        final Long usedKbRaw = attachRepo.sumSizeKbByFaqId(faq.getId()); // puede ser null
        final long usedKb = usedKbRaw == null ? 0L : Math.max(0L, usedKbRaw);
        final long availableBytes = MAX_FAQ_BYTES - Math.max(0, (usedKb - freedKb) * 1024L);

        long incomingBytes = 0L;
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty())
                incomingBytes += f.getSize();
        }
        if (incomingBytes > Math.max(availableBytes, 0)) {
            throw new IllegalArgumentException("El tamaño total de adjuntos por FAQ no puede superar 10 MB.");
        }

        for (MultipartFile f : files) {
            if (f == null || f.isEmpty())
                continue;

            byte[] data = f.getBytes();

            // Constructor público (faq, fileName, contentType, data)
            FaqAttachment att = new FaqAttachment(
                    faq,
                    f.getOriginalFilename(),
                    f.getContentType(),
                    data);
            att.setIsActive(true);

            attachRepo.save(att);
        }
    }
}
