import { Router, type Router as RouterType } from "express";
import * as controller from "@/controllers/activityLogs.controller.js";
import { validateBody } from "@/middlewares/validate.js";
import {
    CreateActivityLogSchema,
    ListActivityLogQuerySchema
} from "@/schemas/activityLog.schema.js";

const r: RouterType = Router();

r.get("/uuid", controller.uuid);
r.get("/", validateBody(ListActivityLogQuerySchema), controller.list);
r.post("/", validateBody(CreateActivityLogSchema), controller.create);

export default r;
