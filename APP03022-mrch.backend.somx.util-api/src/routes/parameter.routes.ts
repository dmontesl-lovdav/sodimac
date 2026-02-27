// src/routes/parameter.routes.ts
// STM-1213: Sistema de versionado de parametros
import { Router } from "express";
import * as parameterController from "@/controllers/parameter.controller.js";

const router = Router();

// ========================================
// CRUD BASICO
// ========================================

// GET /api/parameters - Listar parametros con paginacion y filtros
router.get("/", parameterController.list);

// GET /api/parameters/:id - Obtener parametro por ID
router.get("/:id", parameterController.getById);

// POST /api/parameters - Crear parametro (version inicial 1.0)
router.post("/", parameterController.create);

// PATCH /api/parameters/:id - Actualizar metadatos (NO permite cambiar value)
router.patch("/:id", parameterController.update);

// DELETE /api/parameters/:id - Eliminar parametro (soft delete)
router.delete("/:id", parameterController.remove);

// ========================================
// SISTEMA DE VERSIONADO
// ========================================

// GET /api/parameters/:id/versions - Obtener historial de versiones
router.get("/:id/versions", parameterController.getVersions);

// POST /api/parameters/:id/versions - Crear nueva version (cuando cambia value)
router.post("/:id/versions", parameterController.createVersion);

// ========================================
// GESTION DE ESTATUS
// ========================================

// PATCH /api/parameters/:id/status - Cambiar estatus (1=Activo, 0=Inactivo)
router.patch("/:id/status", parameterController.updateStatus);

export default router;
