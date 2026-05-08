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
import cartaPorteRouter from "./cartaPorte.routes.js";
import storageGCPRouter from "./storageGCP.routes.js";
import threeWayMatchRoutes from "./threeWayMatch.routes.js";
import auditLogRoutes from "./auditLog.routes.js";
import accountStatementRouter from "./accountStatement.routes.js";
import migoRouter from "./migo.routes.js";
import transactionIdRouter from "./transactionId.routes.js";
import purchaseOrderRouter from "./purchaseOrder.routes.js";
import healthcheckRouter from "./healthcheck.routes.js";

const router: RouterType = Router();

router.use("/accounts-payable", accountsPayableRouter);
router.use("/fiscal-payments", fiscalPaymentRouter);
router.use("/rebates", rebateRouter);
router.use("/stamped-rebates", stampedRebateRouter);
router.use("/sap-documents", sapDocumentRouter);
router.use("/shipping-guide", shippingGuideRouter);
router.use("/shipping-guide/:id/documents", shippingGuideRouter);
router.use("/vendor-blocks", vendorBlockRouter);
router.use("/purchase-orders", purchaseOrderRouter);
router.use("/finanzas-payment", finanzasPaymentsRouter);
router.use("/carta-porte", cartaPorteRouter);
router.use("/storage-gcp", storageGCPRouter);
router.use("/three-way-match", threeWayMatchRoutes);
router.use("/audit-logs", auditLogRoutes);
router.use("/account-statement", accountStatementRouter);
router.use("/migo", migoRouter);
router.use("/transaction-ids", transactionIdRouter);
router.use("/healthcheck", healthcheckRouter);

export default router;
