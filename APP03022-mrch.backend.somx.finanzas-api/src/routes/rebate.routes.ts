import { Router } from "express";
import * as ctrl from "@/controllers/rebate.controller.js";
import * as fiscalCtrl from "@/controllers/rebateFiscal.controller.js";

const router = Router();
// ============================================================================
// NUEVOS ENDPOINTS STM-973 & STM-875 (Integración Fiscal)
// ============================================================================
// POST /rebates/relate - STM-973: Relacionar descuento con NC
router.post("/relate", fiscalCtrl.relateRebate);

// POST /rebates/filter - STM-875: Filtrar descuentos comerciales
router.post("/filter", fiscalCtrl.searchRebates);

// ============================================================================
// ENDPOINTS EXISTENTES
// ============================================================================
// Rutas específicas primero (antes de las rutas con parámetros)
router.get("/published", ctrl.getPublishedRebates);
router.get("/published/export/csv", ctrl.exportPublishedRebatesToCsv);
router.get("/search", ctrl.searchRebates);
router.get("/export/csv", ctrl.exportFilteredRebatesToCsv);
router.get("/vendor/:vendorNumber", ctrl.getRebatesByVendor);

// Rutas CRUD generales
router.get("/", ctrl.list);
router.get("/:uuid", ctrl.getById);
router.post("/", ctrl.create);
router.put("/:uuid", ctrl.update);
router.delete("/:uuid", ctrl.remove);

export default router;
