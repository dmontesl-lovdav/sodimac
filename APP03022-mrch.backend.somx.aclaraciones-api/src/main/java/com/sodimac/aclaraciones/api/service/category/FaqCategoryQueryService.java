package com.sodimac.aclaraciones.api.service.category;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sodimac.aclaraciones.api.model.dto.FaqCategoryDto;
import com.sodimac.aclaraciones.api.model.entity.FaqCategory;
import com.sodimac.aclaraciones.api.repository.FaqCategoryRepository;

@Service
public class FaqCategoryQueryService {

    private final FaqCategoryRepository repo;

    public FaqCategoryQueryService(FaqCategoryRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<FaqCategoryDto> listActive(boolean includeIcons) {
        return repo.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(e -> FaqCategoryMapper.toDto(e, includeIcons))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FaqCategoryDto> listByActive(String mode, boolean includeIcons) {
        List<FaqCategory> data;

        switch (mode == null ? "true" : mode.trim().toLowerCase()) {
            case "all":
                data = repo.findAllByOrderByNameAsc();
                break;
            case "false":
            case "0":
                data = repo.findByIsActiveFalseOrderByNameAsc();
                break;
            case "true":
            case "1":
            default:
                data = repo.findByIsActiveTrueOrderByNameAsc();
                break;
        }

        return data.stream()
                .map(e -> FaqCategoryMapper.toDto(e, includeIcons))
                .toList();
    }

    @Transactional(readOnly = true)
    public FaqCategoryDto findById(Long id, boolean includeIcons) {
        return repo.findById(id)
                .map(e -> FaqCategoryMapper.toDto(e, includeIcons))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<FaqCategoryDto> listByActivePaged(
            String mode,
            boolean includeIcons,
            Pageable pageable) {

        Page<FaqCategory> page;

        switch (mode == null ? "true" : mode.trim().toLowerCase()) {
            case "all":
                page = repo.findAll(pageable);
                break;
            case "false":
            case "0":
                page = repo.findByIsActiveFalse(pageable);
                break;
            case "true":
            case "1":
            default:
                page = repo.findByIsActiveTrue(pageable);
                break;
        }

        return page.map(e -> FaqCategoryMapper.toDto(e, includeIcons));
    }

    @Transactional(readOnly = true)
    public Page<FaqCategoryDto> listByActiveAndSearchPaged(
            String mode,
            String search,
            boolean includeIcons,
            Pageable pageable) {

        Page<FaqCategory> page;

        boolean hasSearch = search != null && !search.trim().isEmpty();
        String q = hasSearch ? "%" + search.trim().toLowerCase() + "%" : null;

        switch (mode == null ? "true" : mode.trim().toLowerCase()) {
            case "all":
                page = hasSearch
                        ? repo.searchAll(q, pageable)
                        : repo.findAll(pageable);
                break;

            case "false":
            case "0":
                page = hasSearch
                        ? repo.searchInactive(q, pageable)
                        : repo.findByIsActiveFalse(pageable);
                break;

            case "true":
            case "1":
            default:
                page = hasSearch
                        ? repo.searchActive(q, pageable)
                        : repo.findByIsActiveTrue(pageable);
                break;
        }

        return page.map(e -> FaqCategoryMapper.toDto(e, includeIcons));
    }
}
