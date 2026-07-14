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
export async function list(
    req: Request,
    res: Response,
    next: NextFunction,
): Promise<void> {
    try {
        const query: ListAccountsQuery = ListAccountsQuerySchema.parse(req.query);
        const rows = await svc.list(query);

        if (rows.length === 0) {
            throw new HttpError(
                404,
                "No records found for that filter",
            );
        }

        res.status(200).json(rows);
    } catch (error: unknown) {
        next(error);
    }
}

// GET /accounts-payable/:id
export async function getById(
    req: Request,
    res: Response,
    next: NextFunction,
): Promise<void> {
    try {
        const { id } = IdParamSchema.parse(req.params);
        const row = await svc.get(id);

        if (!row) {
            res.status(404).json({
                message: "Not found",
            });
            return;
        }

        res.status(200).json(row);
    } catch (error: unknown) {
        next(error);
    }
}

// POST /accounts-payable
export async function create(
    req: Request,
    res: Response,
    next: NextFunction,
): Promise<void> {
    try {
        const dto: CreateAccountsPayableDto =
            CreateAccountsPayableSchema.parse(req.body);

        const created = await svc.create(dto);

        res.status(201).json(created);
    } catch (error: unknown) {
        next(error);
    }
}

// PUT /accounts-payable/:id
export async function update(
    req: Request,
    res: Response,
    next: NextFunction,
): Promise<void> {
    try {
        const { id } = IdParamSchema.parse(req.params);

        const dto: UpdateAccountsPayableDto =
            UpdateAccountsPayableSchema.parse(req.body);

        const updated = await svc.update(id, dto);

        if (!updated) {
            res.status(404).json({
                message: "Not found",
            });
            return;
        }

        res.status(200).json(updated);
    } catch (error: unknown) {
        next(error);
    }
}

// DELETE /accounts-payable/:id
export async function remove(
    req: Request,
    res: Response,
    next: NextFunction,
): Promise<void> {
    try {
        const { id } = IdParamSchema.parse(req.params);

        await svc.remove(id);

        res.status(204).end();
    } catch (error: unknown) {
        next(error);
    }
}