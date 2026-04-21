import { Router } from "express";
import * as controller from "@/controllers/threeWayMatch.controller.js";
import { validateQuery } from "@/middlewares/validate.js";
import { ListThreeWayMatchQuerySchema } from "@/schemas/threeWayMatch.schema.js";

const r = Router();

// GET /three-way-match
r.get(
    "/",
    validateQuery(ListThreeWayMatchQuerySchema),
    controller.list
);

// GET /three-way-match/export/csv
r.get(
    "/export/csv",
    validateQuery(ListThreeWayMatchQuerySchema),
    controller.exportCsv
);

// GET /three-way-match/export/xlsx
r.get(
    "/export/xlsx",
    validateQuery(ListThreeWayMatchQuerySchema),
    controller.exportXlsx
);

// POST /three-way-match/run
r.post(
    "/run",
    controller.run
);

export default r;