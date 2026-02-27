package com.sodimac.aclaraciones.api.service.relatedInformation.query;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sodimac.aclaraciones.api.model.dto.RelatedInformationDto;
import com.sodimac.aclaraciones.api.model.entity.RelatedInformation;
import com.sodimac.aclaraciones.api.repository.RelatedInformationRepository;

@Service
public class RelatedInformationQueryService {

    private final RelatedInformationRepository repo;

    public RelatedInformationQueryService(RelatedInformationRepository repo) {
        this.repo = repo;
    }

    public List<RelatedInformationDto> listFiltered(Integer businessUnitId, Integer countryId, Integer size) {
        List<RelatedInformation> data;

        if (businessUnitId != null && countryId != null) {
            data = repo.findByBusinessUnitIdAndCountryId(businessUnitId, countryId);
        } else if (businessUnitId != null) {
            data = repo.findByBusinessUnitId(businessUnitId);
        } else if (countryId != null) {
            data = repo.findByCountryId(countryId);
        } else {
            data = repo.findAll();
        }

        if (size != null && size > 0 && data.size() > size) {
            data = data.subList(0, size);
        }

        return data.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<RelatedInformationDto> listAll() {
        return repo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public RelatedInformationDto findById(Long id) {
        return repo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "RelatedInformation %d no existe".formatted(id)));
    }

    private RelatedInformationDto toDto(RelatedInformation e) {
        String img = e.getImageData() == null ? null
                : Base64.getEncoder().encodeToString(e.getImageData());

        return new RelatedInformationDto(
                e.getId(),
                e.getTitle(),
                e.getLink(),
                img,
                e.getImagePath(),
                e.getIsActive(),
                e.getBusinessUnitId(),
                e.getCountryId()
        );
    }
}
