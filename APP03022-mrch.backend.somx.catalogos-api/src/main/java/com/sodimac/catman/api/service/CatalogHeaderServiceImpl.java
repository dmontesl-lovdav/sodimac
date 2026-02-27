package com.sodimac.catman.api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sodimac.catman.api.mapper.CatalogDetailMapper;
import com.sodimac.catman.api.mapper.CatalogHeaderMapper;
import com.sodimac.catman.api.model.dto.CatalogDetailDto;
import com.sodimac.catman.api.model.dto.CatalogHeaderDto;
import com.sodimac.catman.api.model.entity.CatalogDetail;
import com.sodimac.catman.api.model.entity.CatalogHeader;
import com.sodimac.catman.api.model.entity.DictionaryLang;
import com.sodimac.catman.api.model.entity.CatalogDetailRelation;
import com.sodimac.catman.api.repository.CatalogDetailRelationRepository;
import com.sodimac.catman.api.repository.CatalogDetailRepository;
import com.sodimac.catman.api.repository.CatalogHeaderRepository;
import com.sodimac.catman.api.repository.DictionaryLangRepository;

@Service
public class CatalogHeaderServiceImpl implements CatalogHeaderService {

    private static final Integer DEFAULT_LANG_ID = 1;

    private final CatalogHeaderRepository headerRepository;
    private final CatalogDetailRepository detailRepository;
    private final CatalogDetailRelationRepository relationRepository;
    private final DictionaryLangRepository dictionaryRepository;
    private final CatalogHeaderMapper headerMapper;
    private final CatalogDetailMapper detailMapper;

    public CatalogHeaderServiceImpl(
            CatalogHeaderRepository headerRepository,
            CatalogDetailRepository detailRepository,
            CatalogDetailRelationRepository relationRepository,
            DictionaryLangRepository dictionaryRepository,
            CatalogHeaderMapper headerMapper,
            CatalogDetailMapper detailMapper) {
        this.headerRepository = headerRepository;
        this.detailRepository = detailRepository;
        this.relationRepository = relationRepository;
        this.dictionaryRepository = dictionaryRepository;
        this.headerMapper = headerMapper;
        this.detailMapper = detailMapper;
    }

    @Override
    public List<CatalogHeaderDto> findAllCatalogs(Integer langId) {
        Integer lang = langId != null ? langId : DEFAULT_LANG_ID;
        List<CatalogHeader> headers = headerRepository.findByStatus(CatalogHeader.STATUS_ACTIVE);

        return headerMapper.toDtoList(headers);
    }

    @Override
    public List<CatalogHeaderDto> findCatalogsByModule(String module, Integer langId) {
        Integer lang = langId != null ? langId : DEFAULT_LANG_ID;
        List<CatalogHeader> headers = headerRepository.findByModuleAndStatus(module, CatalogHeader.STATUS_ACTIVE);

        return headerMapper.toDtoList(headers);
    }

    @Override
    public Optional<CatalogHeaderDto> findCatalogByCode(String code, Integer langId) {
        Integer lang = langId != null ? langId : DEFAULT_LANG_ID;
        Map<Integer, String> dictionary = loadDictionary(lang);
        LocalDate today = LocalDate.now();

        return headerRepository.findByCode(code)
                .filter(h -> h.getStatus().equals(CatalogHeader.STATUS_ACTIVE))
                .map(h -> {
                    CatalogHeaderDto dto = headerMapper.toDto(h);
                    List<CatalogDetail> details = detailRepository
                            .findByHeaderIdAndStatusAndValidDate(h.getId(), CatalogDetail.STATUS_ACTIVE, today);
                    dto.setDetails(detailMapper.toDtoList(details, dictionary));
                    return dto;
                });
    }

    @Override
    public List<CatalogDetailDto> findDetailsByCode(String code, Integer langId) {
        Integer lang = langId != null ? langId : DEFAULT_LANG_ID;
        Map<Integer, String> dictionary = loadDictionary(lang);
        LocalDate today = LocalDate.now();

        List<CatalogDetail> details = detailRepository.findByHeaderCodeAndStatusAndValidDate(code, CatalogDetail.STATUS_ACTIVE, today);
        return detailMapper.toDtoList(details, dictionary);
    }

    @Override
    public Optional<CatalogDetailDto> findDetailByCodeAndKey(String code, String key, Integer langId) {
        Integer lang = langId != null ? langId : DEFAULT_LANG_ID;
        Map<Integer, String> dictionary = loadDictionary(lang);

        return headerRepository.findByCode(code)
                .flatMap(header -> detailRepository.findByHeaderIdAndKey(header.getId(), key))
                .filter(d -> d.getStatus().equals(CatalogDetail.STATUS_ACTIVE))
                .map(d -> detailMapper.toDto(d, dictionary));
    }

    @Override
    public Optional<CatalogDetailDto> findDetailByKey(String key, Integer langId) {
        Integer lang = langId != null ? langId : DEFAULT_LANG_ID;
        Map<Integer, String> dictionary = loadDictionary(lang);
        LocalDate today = LocalDate.now();

        return detailRepository.findByKeyAndStatusAndValidDate(key, CatalogDetail.STATUS_ACTIVE, today)
                .map(d -> detailMapper.toDto(d, dictionary));
    }

    @Override
    public Optional<CatalogHeaderDto> findCatalogById(Integer id, Integer langId) {
        Integer lang = langId != null ? langId : DEFAULT_LANG_ID;
        Map<Integer, String> dictionary = loadDictionary(lang);
        LocalDate today = LocalDate.now();

        return headerRepository.findById(id)
                .filter(h -> h.getStatus().equals(CatalogHeader.STATUS_ACTIVE))
                .map(h -> {
                    CatalogHeaderDto dto = headerMapper.toDto(h);
                    List<CatalogDetail> details = detailRepository
                            .findByHeaderIdAndStatusAndValidDate(h.getId(), CatalogDetail.STATUS_ACTIVE, today);
                    dto.setDetails(detailMapper.toDtoList(details, dictionary));
                    return dto;
                });
    }

    @Override
    public Optional<CatalogHeaderDto> findCatalogByPrefix(String prefix, Integer langId) {
        Integer lang = langId != null ? langId : DEFAULT_LANG_ID;
        Map<Integer, String> dictionary = loadDictionary(lang);
        LocalDate today = LocalDate.now();

        return headerRepository.findByPrefixAndStatus(prefix, CatalogHeader.STATUS_ACTIVE)
                .map(h -> {
                    CatalogHeaderDto dto = headerMapper.toDto(h);
                    List<CatalogDetail> details = detailRepository
                            .findByHeaderIdAndStatusAndValidDate(h.getId(), CatalogDetail.STATUS_ACTIVE, today);
                    dto.setDetails(detailMapper.toDtoList(details, dictionary));
                    return dto;
                });
    }

    @Override
    public Optional<CatalogDetailDto> findDetailByKeyFormatted(String key, Integer langId, List<String> params) {
        Integer lang = langId != null ? langId : DEFAULT_LANG_ID;
        Map<Integer, String> dictionary = loadDictionary(lang);
        LocalDate today = LocalDate.now();

        return detailRepository.findByKeyAndStatusAndValidDate(key, CatalogDetail.STATUS_ACTIVE, today)
                .map(d -> {
                    CatalogDetailDto dto = detailMapper.toDto(d, dictionary);
                    // Formatear el mensaje con los parámetros
                    String formattedDescription = formatMessage(dto.getDescription(), params);
                    dto.setDescription(formattedDescription);
                    return dto;
                });
    }

    /**
     * Formatea un mensaje sustituyendo placeholders {0}, {1}, etc. con los parámetros.
     * Valida que la cantidad de parámetros coincida con los placeholders esperados.
     *
     * @param message Mensaje con placeholders
     * @param params Lista de parámetros
     * @return Mensaje formateado
     * @throws IllegalArgumentException si la cantidad de parámetros no coincide
     */
    private String formatMessage(String message, List<String> params) {
        if (message == null || params == null || params.isEmpty()) {
            return message;
        }

        // Contar placeholders en el mensaje
        int expectedParams = countPlaceholders(message);

        if (expectedParams == 0) {
            // No hay placeholders, retornar mensaje sin cambios
            return message;
        }

        if (params.size() != expectedParams) {
            throw new IllegalArgumentException(
                    String.format("El mensaje espera %d parámetro(s) pero se enviaron %d",
                            expectedParams, params.size()));
        }

        // Sustituir placeholders usando MessageFormat
        try {
            return java.text.MessageFormat.format(message, params.toArray());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error formateando mensaje: " + e.getMessage());
        }
    }

    /**
     * Cuenta la cantidad de placeholders {0}, {1}, etc. en el mensaje.
     */
    private int countPlaceholders(String message) {
        int count = 0;
        int index = 0;
        while (true) {
            String placeholder = "{" + index + "}";
            if (message.contains(placeholder)) {
                count++;
                index++;
            } else {
                break;
            }
        }
        return count;
    }

    @Override
    public List<CatalogDetailDto> findDetailsByCodeWithDependency(String code, String targetCatalogCode, String targetExternalKey, Integer langId) {
        Integer lang = langId != null ? langId : DEFAULT_LANG_ID;
        Map<Integer, String> dictionary = loadDictionary(lang);
        LocalDate today = LocalDate.now();

        // Buscar detalles que dependen del catálogo/clave especificados (con validación de vigencia)
        List<CatalogDetail> filteredDetails = relationRepository.findSourceDetailsByTargetCatalogAndKeyAndValidDate(
                targetCatalogCode,
                targetExternalKey,
                CatalogDetailRelation.RELATION_DEPENDS_ON,
                CatalogDetail.STATUS_ACTIVE,
                today);

        // Filtrar solo los detalles que pertenecen al catálogo solicitado
        List<CatalogDetail> detailsInCatalog = filteredDetails.stream()
                .filter(d -> d.getHeader().getCode().equals(code))
                .sorted((a, b) -> Integer.compare(
                        a.getSortOrder() != null ? a.getSortOrder() : 0,
                        b.getSortOrder() != null ? b.getSortOrder() : 0))
                .toList();

        // Si hay resultados de la relación, retornarlos
        if (!detailsInCatalog.isEmpty()) {
            return detailMapper.toDtoList(detailsInCatalog, dictionary);
        }

        // Si no hay relaciones, retornar todos los detalles del catálogo (comportamiento por defecto)
        return findDetailsByCode(code, lang);
    }

    private Map<Integer, String> loadDictionary(Integer langId) {
        return dictionaryRepository.findByLangId(langId).stream()
                .collect(Collectors.toMap(
                        DictionaryLang::getDictId,
                        DictionaryLang::getDescription,
                        (existing, replacement) -> existing));
    }
}
