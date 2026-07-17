/** @type {import('jest').Config} */
module.exports = {
  testEnvironment: "jsdom",
  roots: ["<rootDir>/src"],
  testMatch: ["**/__tests__/**/*.[jt]s?(x)", "**/?(*.)+(spec|test).[jt]s?(x)"],
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",
    "^@shared/(.*)$": "<rootDir>/src/shared/$1",
    "^@assets/.*$": "<rootDir>/src/test/fileMock.js",
    "^@features/(.*)$": "<rootDir>/src/features/$1",
    "^@security/(.*)$": "<rootDir>/src/security/$1",
    "^@store/(.*)$": "<rootDir>/src/store/$1",
    "\\.(css|less|scss|sass)$": "<rootDir>/src/test/styleMock.js",
    "\\.(svg|png|jpg|jpeg|gif|webp)$": "<rootDir>/src/test/fileMock.js",
  },
  transform: {
    "^.+\\.(js|jsx|ts|tsx)$": "babel-jest",
  },
  setupFilesAfterEnv: ["<rootDir>/src/test/setupTests.js"],
  collectCoverageFrom: [
    "src/**/*.{ts,tsx,js,jsx}",
    "!src/**/*.d.ts",
    "!src/**/__tests__/**",
  ],
};
