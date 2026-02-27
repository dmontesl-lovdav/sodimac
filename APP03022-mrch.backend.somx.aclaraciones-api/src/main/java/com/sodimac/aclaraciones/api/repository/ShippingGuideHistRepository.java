/*────────────────────────────────────────────────────────────
 * src/main/java/com/sodimac/aclaraciones/api/repository/ShippingGuideHistRepository.java
 *────────────────────────────────────────────────────────────*/
package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.ShippingGuideHist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingGuideHistRepository extends JpaRepository<ShippingGuideHist, Long> {
}
