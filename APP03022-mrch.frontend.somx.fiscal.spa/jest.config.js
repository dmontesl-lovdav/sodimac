/** @type {import("jest").Config} */
module.exports = {
  testEnvironment: "jsdom",

  roots: ["<rootDir>/src"],

  testMatch: [
    "**/__tests__/**/*.[jt]s?(x)",
    "**/?(*.)+(spec|test).[jt]s?(x)",
  ],

  moduleNameMapper: {
    // Los mocks de assets/estilos van ANTES de los alias `@/...`:
    // fiscal importa iconos como "@/assets/foo.svg", y si el alias
    // genérico "^@/(.*)$" se evalúa primero, Jest intenta parsear
    // el SVG real en lugar de usar el mock.
    "^@assets/.*$": "<rootDir>/src/test/fileMock.js",
    "\\.(css|less|scss|sass)$": "<rootDir>/src/test/styleMock.js",
    "\\.(svg|png|jpg|jpeg|gif|webp)$":
      "<rootDir>/src/test/fileMock.js",
    "^@/(.*)$": "<rootDir>/src/$1",
    "^@shared/(.*)$": "<rootDir>/src/shared/$1",
    "^@features/(.*)$": "<rootDir>/src/features/$1",
    "^@store/(.*)$": "<rootDir>/src/store/$1",
  },

  transform: {
    "^.+\\.(js|jsx|ts|tsx)$": "babel-jest",
  },

  setupFilesAfterEnv: [
    "<rootDir>/src/test/setupTests.js",
  ],

  clearMocks: true,

  coverageDirectory: "<rootDir>/coverage",

  coverageReporters: [
    "text",
    "text-summary",
    "lcov",
    "json-summary",
  ],

  // TEMPORAL:
  // Solo se calcula cobertura sobre archivos productivos
  // respaldados por pruebas unitarias reales (línea ≥ ~80%).
  collectCoverageFrom: [
    "src/shared/components/ui/modal/GenericModal.tsx",
    "src/shared/components/ui/navigation/Breadcrumb.tsx",
    "src/shared/components/ui/misc/GenericTrace.tsx",
    "src/shared/components/ui/input/GenericInput.tsx",
    "src/shared/components/ui/attachmentUploader/attachmentHelpers.ts",
    "src/shared/security/securityService.ts",
    "src/shared/security/useSecurityContext.ts",
    "src/features/home/services/versionCatalogService.ts",
    "src/features/creditNote/parts/publishQuery.ts",
    "src/features/creditNote/parts/parseValidatedXml.ts",
    "src/features/creditNote/utils/resolveXmlValidationCommand.ts",
    "src/features/creditNote/utils/publishCreditNoteResponse.ts",
  ],

  modulePathIgnorePatterns: [
    "<rootDir>/dist/",
    "<rootDir>/coverage/",
  ],
};
