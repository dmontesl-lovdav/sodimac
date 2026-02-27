import { Router } from "express";
import * as ctrl from "@/controllers/vendorBlock.controller.js";

const router = Router();
router.get("/", ctrl.list);
router.get("/:uuid", ctrl.getById);
router.post("/", ctrl.create);
router.put("/:uuid", ctrl.update);
router.delete("/:uuid", ctrl.remove);

export default router;
