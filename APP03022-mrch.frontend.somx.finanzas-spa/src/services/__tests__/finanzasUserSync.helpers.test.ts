import { describe, expect, it } from "@jest/globals";
import {
  conversionMatchesMacroRole,
  conversionValueIds,
  deniedWarning,
  extractDetails,
  extractMacroRoles,
  extractMxTaxIds,
  findConversionRows,
  matchSupplierByRfc,
  assignmentsMatchExpected,
  parseUserCatalogAssignments,
  normalizeLabel,
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
      value: "0981",
      description: "Proveedor Mercancía",
      sortOrder: 1,
    };

    it("cruza solo por externalKey, no por value ni descripción", () => {
      expect(
        conversionMatchesMacroRole(conversion, "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER")
      ).toBe(true);
      expect(
        conversionMatchesMacroRole(conversion, "FBC_NATIONAL_SUPPLIER_FINANCE_USER")
      ).toBe(false);
      expect(conversionMatchesMacroRole(conversion, "0981")).toBe(false);
    });

    it("usa el value de la conversión como id numérico", () => {
      expect(conversionValueIds([conversion])).toEqual([981]);
      expect(conversionValueIds([{ value: "1022" }, { value: "1022" }])).toEqual([1022]);
    });

    it("toma coincidencias de varios macroroles del token", () => {
      const rows = findConversionRows(
        [
          conversion,
          {
            externalKey: "FBC_NATIONAL_SUPPLIER_FINANCE_USER",
            value: "0990",
          },
        ],
        [
          "FBC_NATIONAL_SUPPLIER_FINANCE_USER",
          "FBC_NATIONAL_LOGISTICS_SUPPLIER_USER",
          "FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER",
        ]
      );
      expect(conversionValueIds(rows)).toEqual([990, 981]);
    });
  });

  describe("catalog helpers", () => {
    it("extrae details tanto de envelope como de arreglo", () => {
      expect(extractDetails({ details: [{ id: 1, value: "A" }] })).toEqual([
        { id: 1, value: "A" },
      ]);
      expect(extractDetails([{ id: 2, value: "B" }])).toEqual([{ id: 2, value: "B" }]);
    });

    it("usa el texto local de WRN7038 sin consultar catálogo de mensajes", () => {
      expect(deniedWarning("profile")).toContain(
        "perfil asignado a su usuario no se encuentra configurado"
      );
      expect(deniedWarning("role")).toContain(
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
          { profileIds: [981], roleIds: [1022], multipleProfiles: false },
          981,
          [1022]
        )
      ).toBe(true);
    });

    it("exige reasignar si el macrorol mapea a otro perfil o hay perfiles extra", () => {
      expect(
        assignmentsMatchExpected(
          { profileIds: [10], roleIds: [1022], multipleProfiles: false },
          981,
          [1022]
        )
      ).toBe(false);
      expect(
        assignmentsMatchExpected(
          { profileIds: [981], roleIds: [1022], multipleProfiles: true },
          981,
          [1022]
        )
      ).toBe(false);
    });
  });

  describe("parseUserCatalogAssignments", () => {
    it("lee ids de perfil y roles del detalle de catálogo", () => {
      expect(
        parseUserCatalogAssignments({
          profile: { id: 981, name: "Proveedor" },
          multipleProfilesDetected: false,
          roles: { items: [{ id: 1022 }, { id: 1031 }] },
        })
      ).toEqual({
        profileIds: [981],
        roleIds: [1022, 1031],
        multipleProfiles: false,
      });
    });
  });

  describe("normalizeLabel", () => {
    it("iguala etiquetas equivalentes", () => {
      expect(normalizeLabel("FBC_NATIONAL_COMMERCIAL_SUPPLIER_USER")).toBe(
        normalizeLabel("fbc national commercial supplier user")
      );
    });
  });
});
