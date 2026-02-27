package com.sodimac.aclaraciones.api.service.shipment;

import com.sodimac.aclaraciones.api.model.dto.ShipmentGuideFilter;
import com.sodimac.aclaraciones.api.model.dto.ShipmentGuideView;
import com.sodimac.aclaraciones.api.model.entity.ShippingGuide;
import com.sodimac.aclaraciones.api.repository.ShippingGuideRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ShipmentGuideQueryService {

    private final ShippingGuideRepository repo;

    public ShipmentGuideQueryService(ShippingGuideRepository repo) {
        this.repo = repo;
    }

    public List<ShipmentGuideView> find(ShipmentGuideFilter f) {
        Specification<ShippingGuide> spec = Specification.where(null);

        if (f.supplierNumber() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("supplierNumber"), f.supplierNumber()));
        }
        if (f.guideNumber() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("guideNumber"), f.guideNumber()));
        }
        if (f.purchaseOrder() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("purchaseOrderId"), f.purchaseOrder()));
        }
        if (f.status() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.join("status", JoinType.LEFT).get("id"), f.status()));
        }

        // g.SENTDATE between
        spec = spec.and(dateRangeLocalDate("shippingDate", f.fromShipDate(), f.toShipDate()));

        // g.CREATEDAT between (use LocalDateTime bounds)
        spec = spec.and(dateRangeLocalDateTime("createdAt", f.fromRegDate(), f.toRegDate()));

        var sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");

        return repo.findAll(spec, sort).stream()
                .map(ShipmentGuideQueryService::toView)
                .toList();
    }

    private static Specification<ShippingGuide> dateRangeLocalDate(
            String field, LocalDate from, LocalDate to) {
        return (r, q, cb) -> {
            var p = cb.conjunction();
            if (from != null) p = cb.and(p, cb.greaterThanOrEqualTo(r.get(field), from));
            if (to != null) p = cb.and(p, cb.lessThanOrEqualTo(r.get(field), to));
            return p;
        };
    }

    private static Specification<ShippingGuide> dateRangeLocalDateTime(
            String field, LocalDate from, LocalDate to) {
        return (r, q, cb) -> {
            var p = cb.conjunction();
            if (from != null)
                p = cb.and(p, cb.greaterThanOrEqualTo(r.get(field), from.atStartOfDay()));
            if (to != null)
                p = cb.and(p, cb.lessThanOrEqualTo(r.get(field), LocalDateTime.of(to, LocalTime.MAX)));
            return p;
        };
    }

    private static ShipmentGuideView toView(ShippingGuide g) {
        return new ShipmentGuideView(
                g.getId(),
                g.getSupplierNumber(),
                g.getSupplier() != null ? g.getSupplier().getName() : null,
                g.getGuideNumber(),
                g.getPlate(),
                g.getTrailerPlate(),
                g.getOrigin(),
                g.getDeliveryType(),
                g.getAmount(),
                g.getShippingDate(),
                g.getCreatedAt() != null ? g.getCreatedAt().toLocalDate() : null,
                g.getStatus() != null ? g.getStatus().getId() : null,
                g.getStatus() != null ? g.getStatus().getDescription() : null
        );
    }
}
