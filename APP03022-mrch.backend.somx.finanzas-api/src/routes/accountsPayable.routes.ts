import { Router, type Router as RouterType } from "express";
import * as controller from "@/controllers/accountsPayable.controller.js";
import { validateBody, validateParams, validateQuery } from "@/middlewares/validate.js";
import {
    CreateAccountsPayableSchema,
    UpdateAccountsPayableSchema,
    IdParamSchema,
    ListAccountsQuerySchema
} from "@/schemas/accountsPayable.schema.js";

const r: RouterType = Router();

r.get("/", validateQuery(ListAccountsQuerySchema), controller.list);
r.get("/:uuid", validateParams(IdParamSchema), controller.getById);
r.post("/", validateBody(CreateAccountsPayableSchema), controller.create);
r.patch("/:uuid", validateParams(IdParamSchema), validateBody(UpdateAccountsPayableSchema), controller.update);
r.delete("/:uuid", validateParams(IdParamSchema), controller.remove);

export default r;
