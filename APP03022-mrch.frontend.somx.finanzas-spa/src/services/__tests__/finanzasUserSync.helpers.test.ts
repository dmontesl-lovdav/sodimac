import { describe, expect, it } from "@jest/globals";
import {
  resolveDeniedWarning,
  conversionMatchesMacroRole,
  extractCatalogHeaders,
  extractDetails,
  extractMacroRoles,
  extractMxTaxIds,
  findConversionRows,
  loadCatalogByCandidates,
  localCatalogMatches,
  matchSupplierByRfc,
  assignmentsMatchExpected,
  parseUserCatalogAssignments,
  normalizeLabel,
  resolveLocalCatalogIds,
  splitCatalogValues,
} from "../finanzasUserSync.helpers";

describe("finanzasUserSync.helpers", () => {
  describe("extractMacroRoles", () => {
    it("toma realm_access.roles y macroRole.name", () => {
      const roles = extractMacroRoles({
        realm_access: {
          roles: [
            "default-roles-corp",
            "FBC_NATIONAL_SUPPLIER_FINANCE_USER",
            "offline_access",
          ],
        },
        macroRole: { name: "FBC_NATIONAL_SUPPLIER_FINANCE_USER" },
      });
      expect(roles).toEqual([
        "default-roles-corp",
        "FBC_NATIONAL_SUPPLIER_FINANCE_USER",
        "offline_access",
      ]);
    });
  });

  describe("extractMxTaxIds", () => {
    it("filtra vendors-taxs de país MX", () => {
      const rfcs = extractMxTaxIds({
        "vendors-taxs": [
          {
            name: "METAL MECANICA",
            taxId: "MMM031205NG4",
            country: "MX",
            operation: [{ businessUnit: "SOD", country: ["MX"] }],
          },
          {
            name: "OTRO",
            taxId: "ABC010101AAA",
            country: "CL",
          },
        ],
      });
      expect(rfcs).toEqual(["MMM031205NG4"]);
    });
  });

  describe("conversión de macrorol", () => {
    const conversion = {
      id: 1470,
      key: "FBC0001",
      externalKey: "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER",
      value: "Proveedor Mercancía",
      description: "Perfil local",
      sortOrder: 1,
    };

    it("cruz a por externalKey del catálogo, no por datos fijos", () => {
      expect(
        conversionMatchesMacroRole(
          conversion,
          "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER"
        )
      ).toBe(true);
      expect(
        conversionMatchesMacroRole(
          conversion,
          "FBC_NATIONAL_SUPPLIER_FINANCE_USER"
        )
      ).toBe(false);
    });

    it("no asigna si el macrorol no está en el catálogo de conversión", () => {
      const rows = findConversionRows(
        [conversion],
        ["FBC_NATIONAL_SUPPLIER_FINANCE_USER"]
      );
      expect(rows).toEqual([]);
    });
  });

  describe("resolveLocalCatalogIds", () => {
    it("resuelve CatPerfil y CatRol por value ignorando acentos y guiones", () => {
      const profileIds = resolveLocalCatalogIds(
        [
          {
            id: 10,
            key: "PER001",
            value: "Proveedor Mercancía",
            description: "Proveedor Mercancía",
          },
        ],
        ["Proveedor Mercancía"]
      );
      const roleIds = resolveLocalCatalogIds(
        [
          {
            id: 20,
            key: "ROL001",
            value: "Proveedor - Mercancía",
            description: "Proveedor - Mercancía",
          },
        ],
        ["Proveedor - Mercancía"]
      );
      expect(profileIds).toEqual([10]);
      expect(roleIds).toEqual([20]);
    });

    it("acepta varios roles separados en el value del catálogo", () => {
      expect(splitCatalogValues("Rol A, Rol B")).toEqual(["Rol A", "Rol B"]);
      expect(
        localCatalogMatches(
          { id: 3, value: "Rol A", description: "Rol A" },
          "rol a"
        )
      ).toBe(true);
    });
  });

  describe("loadCatalogByCandidates", () => {
    it("usa el código real del API y, si falta, busca por nombre en el listado", async () => {
      const fetchCatalog = async (path: string) => {
        if (path === "CatMacroRolPerfil") return null;
        if (path === "") {
          return [
            { code: "CATMACROROLPERFIL", name: "CatMacroRolPerfil" },
          ];
        }
        if (path === "CATMACROROLPERFIL") {
          return {
            code: "CATMACROROLPERFIL",
            details: [{ id: 1, externalKey: "FBC_X", value: "Perfil X" }],
          };
        }
        return null;
      };

      const catalog = await loadCatalogByCandidates(fetchCatalog, [
        "CatMacroRolPerfil",
        "CATMACROROLPERFIL",
      ]);
      expect(catalog?.code).toBe("CATMACROROLPERFIL");
      expect(catalog?.details).toHaveLength(1);
    });
  });

  describe("catalog helpers", () => {
    it("extrae details tanto de envelope como de arreglo", () => {
      expect(extractDetails({ details: [{ id: 1, value: "A" }] })).toEqual([
        { id: 1, value: "A" },
      ]);
      expect(extractDetails([{ id: 2, value: "B" }])).toEqual([
        { id: 2, value: "B" },
      ]);
      expect(extractCatalogHeaders([{ code: "CatPerfil", name: "Perfil" }])).toEqual([
        { code: "CatPerfil", name: "Perfil" },
      ]);
    });

    it("usa el texto de perfil del escenario 3 si el catálogo no trae WRN7038", () => {
      expect(resolveDeniedWarning("", "profile")).toContain(
        "perfil asignado a su usuario no se encuentra configurado"
      );
      expect(resolveDeniedWarning("WRN7038", "role")).toContain(
        "rol asignado a su usuario no se encuentra configurado"
      );
    });
  });

  describe("matchSupplierByRfc", () => {
    it("cruza RFC MX con el catálogo de proveedores", () => {
      const supplier = matchSupplierByRfc(
        [
          { id: 77, supplierNumber: "1001", rfc: "mmm031205ng4", businessName: "METAL" },
        ],
        ["MMM031205NG4"]
      );
      expect(supplier).toEqual({
        id: "77",
        supplierNumber: "1001",
        rfc: "mmm031205ng4",
      });
    });
  });

  describe("assignmentsMatchExpected", () => {
    it("detecta que el perfil y rol locales coinciden con la conversión", () => {
      expect(
        assignmentsMatchExpected(
          { profileIds: [10], roleIds: [20, 21], multipleProfiles: false },
          10,
          [21, 20]
        )
      ).toBe(true);
    });

    it("exige reasignar si el macrorol mapea a otro perfil o hay perfiles extra", () => {
      expect(
        assignmentsMatchExpected(
          { profileIds: [10], roleIds: [20], multipleProfiles: false },
          11,
          [20]
        )
      ).toBe(false);
      expect(
        assignmentsMatchExpected(
          { profileIds: [10], roleIds: [20], multipleProfiles: true },
          10,
          [20]
        )
      ).toBe(false);
    });
  });

  describe("parseUserCatalogAssignments", () => {
    it("lee ids de perfil y roles del detalle de catálogo", () => {
      expect(
        parseUserCatalogAssignments({
          profile: { id: 10, name: "Proveedor" },
          multipleProfilesDetected: false,
          roles: { items: [{ id: 20 }, { id: 21 }] },
        })
      ).toEqual({
        profileIds: [10],
        roleIds: [20, 21],
        multipleProfiles: false,
      });
    });
  });

  describe("normalizeLabel", () => {
    it("iguala etiquetas equivalentes", () => {
      expect(normalizeLabel("Proveedor - Mercancía")).toBe(
        normalizeLabel("proveedor mercancia")
      );
    });
  });
});
