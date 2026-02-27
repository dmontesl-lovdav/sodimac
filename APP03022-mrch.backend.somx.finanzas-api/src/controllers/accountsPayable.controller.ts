import type { Request, Response, NextFunction } from "express";
import * as svc from "@/services/accountsPayable.service.js";
import { HttpError } from "@/utils/HttpError.js";
import {
    CreateAccountsPayableSchema,
    UpdateAccountsPayableSchema,
    ListAccountsQuerySchema,
    IdParamSchema,
    type CreateAccountsPayableDto,
    type UpdateAccountsPayableDto,
    type ListAccountsQuery,
} from "@/schemas/accountsPayable.schema.js";

// GET /accounts-payable
export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const q: ListAccountsQuery = ListAccountsQuerySchema.parse(req.query);
        const rows = await svc.list(q);

        if (!rows.length) throw new HttpError(404, "No records found for that filter");

        res.json(rows);
    } catch (e) {
        next(e);
    }
}

// GET /accounts-payable/:id
export async function getById(req: Request, res: Response, next: NextFunction) {
    try {
        const { id } = IdParamSchema.parse(req.params);
        const row = await svc.get(id);
        if (!row) return res.status(404).json({ message: "Not found" });
        res.json(row);
    } catch (e) { next(e); }
}

// POST /accounts-payable
export async function create(req: Request, res: Response, next: NextFunction) {
    try {
        const dto: CreateAccountsPayableDto = CreateAccountsPayableSchema.parse(req.body);
        const created = await svc.create(dto);
        res.status(201).json(created);
    } catch (e) { next(e); }
}

// PUT /accounts-payable/:id
export async function update(req: Request, res: Response, next: NextFunction) {
    try {
        const { id } = IdParamSchema.parse(req.params);
        const dto: UpdateAccountsPayableDto = UpdateAccountsPayableSchema.parse(req.body);
        const updated = await svc.update(id, dto);
        if (!updated) return res.status(404).json({ message: "Not found" });
        res.json(updated);
    } catch (e) { next(e); }
}

// DELETE /accounts-payable/:id
export async function remove(req: Request, res: Response, next: NextFunction) {
    try {
        const { id } = IdParamSchema.parse(req.params);
        await svc.remove(id);
        res.status(204).end();
    } catch (e) { next(e); }
}
