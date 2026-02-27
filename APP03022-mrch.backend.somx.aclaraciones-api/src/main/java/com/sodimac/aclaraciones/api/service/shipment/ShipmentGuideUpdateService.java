package com.sodimac.aclaraciones.api.service.shipment;

import com.sodimac.aclaraciones.api.model.dto.UpdateShipmentGuideRequest;
import com.sodimac.aclaraciones.api.model.entity.ShippingGuide;
import com.sodimac.aclaraciones.api.model.entity.ShippingGuideHist;
import com.sodimac.aclaraciones.api.model.entity.ShippingStatus;
import com.sodimac.aclaraciones.api.repository.ShippingGuideHistRepository;
import com.sodimac.aclaraciones.api.repository.ShippingGuideRepository;
import com.sodimac.aclaraciones.api.repository.ShippingStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class ShipmentGuideUpdateService {

    private final ShippingGuideRepository repo;
    private final ShippingGuideHistRepository histRepo;
    private final ShippingStatusRepository statusRepo;

    public ShipmentGuideUpdateService(ShippingGuideRepository repo,
                                      ShippingGuideHistRepository histRepo,
                                      ShippingStatusRepository statusRepo) {
        this.repo = repo;
        this.histRepo = histRepo;
        this.statusRepo = statusRepo;
    }

    @Transactional
    public void update(long id, UpdateShipmentGuideRequest u, String user) {
        ShippingGuide g = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ERR_NOT_FOUND"));

        // validate status if present
        if (u.getStatus() != null && statusRepo.findById(u.getStatus()).isEmpty()) {
            throw new IllegalArgumentException("ERR_STATUS_NOT_CATALOGED");
        }

        // backup into history
        ShippingGuideHist h = new ShippingGuideHist();
        h.setIdShippingGuide(g.getId());
        h.setPurchaseOrderId(g.getPurchaseOrderId());
        h.setGuideNumber(g.getGuideNumber());
        h.setSupplierNumber(g.getSupplierNumber());
        h.setPlate(g.getPlate());
        h.setTrailerPlate(g.getTrailerPlate());
        h.setOrigin(g.getOrigin());
        h.setDeliveryType(g.getDeliveryType());
        h.setAmount(g.getAmount());
        h.setDeliveryDate(g.getDeliveryDate());
        h.setShippingDate(g.getShippingDate());
        h.setCartaPorteXml(g.getCartaPorteXml());
        h.setCartaPorteCsv(g.getCartaPorteCsv());
        h.setComments(g.getComments());
        h.setStatusId(g.getStatus() != null ? g.getStatus().getId() : null);
        h.setUserCreatedId(g.getUserCreatedId());
        h.setCreatedAt(g.getCreatedAt());
        h.setUpdatedAt(g.getUpdatedAt());
        h.setArchivedAt(LocalDateTime.now());
        h.setArchivedBy(user);
        histRepo.save(h);

        // apply partial updates
        if (u.getStatus() != null) {
            ShippingStatus st = statusRepo.findById(u.getStatus()).orElse(null);
            g.setStatus(st);
        }
        if (u.getPlate() != null) g.setPlate(u.getPlate());
        if (u.getTrailerPlate() != null) g.setTrailerPlate(u.getTrailerPlate());
        if (u.getCartaPorteXml() != null) g.setCartaPorteXml(u.getCartaPorteXml());
        if (u.getCartaPorteCsv() != null) g.setCartaPorteCsv(u.getCartaPorteCsv());

        repo.save(g);
    }
}
