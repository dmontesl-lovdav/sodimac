import { Router, type Router as RouterType } from "express";
import * as controller from "@/controllers/healthcheck.controller.js";
import { validateQuery } from "@/middlewares/validate.js";
import { HealthcheckQuerySchema } from "@/schemas/healthcheck.schema.js";

const r: RouterType = Router();

r.get("/", validateQuery(HealthcheckQuerySchema), controller.list);

export default r;