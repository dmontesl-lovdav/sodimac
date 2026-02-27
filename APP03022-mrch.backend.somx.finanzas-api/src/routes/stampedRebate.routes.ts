import { Router } from "express";
import * as ctrl from "@/controllers/stampedRebate.controller.js";

const router = Router();
// Specific routes first
router.get("/export/csv", ctrl.exportCsv);

// CRUD routes
router.get("/", ctrl.list);
router.get("/:uuid", ctrl.getById);
router.post("/", ctrl.create);
router.put("/:uuid", ctrl.update);
router.delete("/:uuid", ctrl.remove);

export default router;
