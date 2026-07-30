/** @type {import("jest").Config} */
module.exports = {
  testEnvironment: "jsdom",

  roots: ["<rootDir>/src"],

  testMatch: [
    "**/__tests__/**/*.[jt]s?(x)",
    "**/?(*.)+(spec|test).[jt]s?(x)",
  ],

  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",
    "^@shared/(.*)$": "<rootDir>/src/shared/$1",
    "^@assets/.*$": "<rootDir>/src/test/fileMock.js",
    "^@features/(.*)$": "<rootDir>/src/features/$1",
    "^@security/(.*)$": "<rootDir>/src/security/$1",
    "^@store/(.*)$": "<rootDir>/src/store/$1",
    "\\.(css|less|scss|sass)$": "<rootDir>/src/test/styleMock.js",
    "\\.(svg|png|jpg|jpeg|gif|webp)$":
      "<rootDir>/src/test/fileMock.js",
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
  // respaldados por pruebas unitarias reales.
  collectCoverageFrom: [
    "src/features/accountStatement/accountStatementActions.ts",
    "src/features/discounts/api/normalizeRebate.ts",
    "src/features/orders/receptionStatusDictionary.ts",
    "src/features/payments/paymentStatusDisplay.ts",
    "src/shared/components/ui/modal/GenericModal.tsx",
    "src/shared/components/ui/navigation/Breadcrumb.tsx",
    "src/utils/errorMessage.ts",
    "src/utils/fiscalSpaUrl.ts",
  ],

  modulePathIgnorePatterns: [
    "<rootDir>/dist/",
    "<rootDir>/coverage/",
  ],
};