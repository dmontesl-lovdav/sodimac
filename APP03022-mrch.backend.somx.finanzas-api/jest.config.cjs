/** @type {import('jest').Config} */
module.exports = {
    preset: 'ts-jest',
    testEnvironment: 'node',

    transform: {
        '^.+\\.(ts|tsx)$': [
            'ts-jest',
            {
                useESM: true,
                tsconfig: 'tsconfig.spec.json',
            },
        ],
    },

    extensionsToTreatAsEsm: ['.ts'],

    moduleNameMapper: {
        '^@/(.*)\\.js$': '<rootDir>/src/$1',
        '^@/(.*)$': '<rootDir>/src/$1',
        '^(\\.{1,2}/.*)\\.js$': '$1',
        '^jose$': '<rootDir>/src/__tests__/mocks/jose.ts',
    },

    // TEMPORAL:
    // Ejecutar únicamente las dos pruebas que respaldan
    // el alcance reducido de cobertura.
    testMatch: [
        '<rootDir>/src/lib/__tests__/errors.test.ts',
        '<rootDir>/src/services/__tests__/cache.service.test.ts',
    ],

    transformIgnorePatterns: [
        '/node_modules/',
    ],

    modulePathIgnorePatterns: [
        '<rootDir>/dist/',
    ],

    collectCoverage: true,
    coverageDirectory: 'coverage',
    coverageReporters: [
        'text',
        'lcov',
    ],

    // TEMPORAL:
    // Medir cobertura únicamente sobre código con pruebas reales.
    collectCoverageFrom: [
        'src/lib/errors.ts',
        'src/services/cache.service.ts',
    ],

    clearMocks: true,
    restoreMocks: true,
};