package com.sodimac.aclaraciones.api.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.sodimac.aclaraciones.api.model.entity.ModuleResolver;
import com.sodimac.aclaraciones.api.repository.ModuleResolverRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/resolvers")
public class MeResolversController {

    private final ModuleResolverRepository repo;

    @Autowired
    public MeResolversController(ModuleResolverRepository repo) {
        this.repo = repo;
    }

    // ============================================================
    // 1. Obtener módulos por email
    // ============================================================
    @Operation(operationId = "getResolverModulesByEmail", summary = "Returns the modules assigned to a specific resolver email.")
    @GetMapping("/modules")
    public List<Integer> getModulesByEmail(@RequestParam String email) {
        return repo.findByResolverEmailIgnoreCase(email)
                .stream()
                .map(ModuleResolver::getModuleId)
                .distinct()
                .toList();
    }

    // ============================================================
    // 2. Obtener detalles por email
    // ============================================================
    @Operation(operationId = "getResolverDetailsByEmail", summary = "Returns detailed resolver rows for a specific email.")
    @GetMapping
    public List<ModuleResolver> getResolversByEmail(@RequestParam String email) {
        return repo.findByResolverEmailIgnoreCase(email);
    }

    // ============================================================
    // 3. Obtener ALL paginado
    // ============================================================
    @Operation(operationId = "getAllModuleResolvers", summary = "Returns all module resolvers (paginated).")
    @GetMapping("/all")
    public Page<ModuleResolver> getAllResolvers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        return repo.findAll(pageable);
    }

    // ============================================================
    // 4. Obtener por moduleId paginado
    // ============================================================
    @Operation(operationId = "getResolversByModuleId", summary = "Returns module resolver rows filtered by moduleId (paginated).")
    @GetMapping("/by-module")
    public Page<ModuleResolver> getResolversByModule(
            @RequestParam Integer moduleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        return repo.findByModuleId(moduleId, pageable);
    }

    // ============================================================
    // ⭐ 5. OBTENER RESOLVER POR ID
    // ============================================================
    @Operation(operationId = "getResolverById", summary = "Returns a resolver entry by its ID.")
    @GetMapping("/{id}")
    public ModuleResolver getById(@PathVariable Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Resolver not found: " + id));
    }

    // ============================================================
    // 6. Crear o actualizar
    // ============================================================
    @Operation(operationId = "upsertModuleResolver", summary = "Creates or updates a module resolver entry.")
    @PostMapping("/upsert")
    public ModuleResolver upsert(@RequestBody ModuleResolver incoming) {

        if (incoming.getId() != null) {
            Optional<ModuleResolver> existing = repo.findById(incoming.getId());
            if (existing.isPresent()) {
                ModuleResolver entity = existing.get();
                entity.setModuleId(incoming.getModuleId());
                entity.setModuleName(incoming.getModuleName());
                entity.setPersonName(incoming.getPersonName());
                entity.setResolverEmail(incoming.getResolverEmail());
                entity.setArea(incoming.getArea());
                return repo.save(entity);
            }
        }

        return repo.save(incoming);
    }

    // ============================================================
    // 7. ELIMINAR RESOLVER POR ID
    // ============================================================
    @Operation(operationId = "deleteResolverById", summary = "Deletes a resolver entry by its ID.")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Integer id) {

        ModuleResolver found = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Resolver not found: " + id));

        repo.delete(found);
    }
}
