package com.sodimac.aclaraciones.api.service.shipment;

import com.sodimac.aclaraciones.api.model.dto.CreateShipmentGuideRequest;
import com.sodimac.aclaraciones.api.model.entity.ShippingGuide;
import com.sodimac.aclaraciones.api.model.entity.ShippingStatus;
import com.sodimac.aclaraciones.api.repository.ShippingGuideRepository;
import com.sodimac.aclaraciones.api.repository.ShippingStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentGuideService {

    private final ShippingGuideRepository repo;
    private final ShippingStatusRepository statusRepo;

    public ShipmentGuideService(ShippingGuideRepository repo, ShippingStatusRepository statusRepo) {
        this.repo = repo;
        this.statusRepo = statusRepo;
    }

    @Transactional
    public long create(CreateShipmentGuideRequest r) {
        ShippingGuide g = new ShippingGuide();

        Long po = (r.purchaseOrders() == null || r.purchaseOrders().isEmpty())
                ? null
                : r.purchaseOrders().get(0);

        g.setPurchaseOrderId(po);
        g.setGuideNumber(r.guideNumber());
        g.setSupplierNumber(r.providerNumber());
        g.setPlate(r.plate());
        g.setTrailerPlate(r.trailerPlate());
        g.setOrigin(r.origin());
        g.setDeliveryType(r.deliveryType());
        g.setAmount(r.amount());
        g.setDeliveryDate(r.deliveryDate());
        g.setShippingDate(r.shippingDate());
        g.setComments(r.comments() == null ? "" : r.comments());
        g.setUserCreatedId(1L); // keep same default as before

        // default status = 1
        ShippingStatus st = statusRepo.findById(1).orElse(null);
        g.setStatus(st);

        return repo.save(g).getId();
    }
}
