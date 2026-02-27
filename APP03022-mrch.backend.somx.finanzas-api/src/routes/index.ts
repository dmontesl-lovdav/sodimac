// src/routes/index.ts
import { Router, type Router as RouterType } from "express";
import accountsPayableRouter from "./accountsPayable.routes.js";
import fiscalPaymentRouter from "./fiscalPayment.routes.js";
import rebateRouter from "./rebate.routes.js";
import sapDocumentRouter from "./sapDocument.routes.js";
import shippingGuideRouter from "./shippingGuide.routes.js";
import stampedRebateRouter from "./stampedRebate.routes.js";
import vendorBlockRouter from "./vendorBlock.routes.js";
import finanzasPaymentsRouter from "./finanzasPayments.routes.js";
import purchaseOrderController from "@/controllers/purchaseOrder.controller.js";
import cartaPorteRouter from "./cartaPorte.routes.js";
import storageGCPRouter from "./storageGCP.routes.js";


const router: RouterType = Router();

// monta las rutas con prefijo
//router.use(contextMiddleware);
router.use("/accounts-payable", accountsPayableRouter);
router.use("/fiscal-payments", fiscalPaymentRouter);
router.use("/rebates", rebateRouter);
router.use("/stamped-rebates", stampedRebateRouter);
router.use("/sap-documents", sapDocumentRouter);
router.use("/shipping-guide", shippingGuideRouter);
router.use("/shipping-guide/:id/documents", shippingGuideRouter);
router.use("/vendor-blocks", vendorBlockRouter);
router.use("/purchase-orders", purchaseOrderController);
router.use("/finanzas-payment", finanzasPaymentsRouter);
router.use("/carta-porte", cartaPorteRouter);
router.use("/storage-gcp", storageGCPRouter);

export default router;
