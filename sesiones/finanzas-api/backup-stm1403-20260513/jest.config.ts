// jest.config.ts
import type { Config } from 'jest';

const config: Config = {
    preset: 'ts-jest',
    testEnvironment: 'node',

    transform: {
        '^.+\\.(ts|tsx)$': [
            'ts-jest',
            { useESM: true, tsconfig: 'tsconfig.spec.json' },
        ],
    },
    extensionsToTreatAsEsm: ['.ts'],

    moduleNameMapper: {
        // 1) PRIMERO: alias @/ que VIENEN con .js en el import (NodeNext)
        '^@/(.*)\\.js$': '<rootDir>/src/$1',
        // 2) LUEGO: alias @/ genérico
        '^@/(.*)$': '<rootDir>/src/$1',
        // 3) Quitar .js en imports relativos dentro de TS
        '^(\\.{1,2}/.*)\\.js$': '$1',
        // 4) Mock ESM conflictivo
        '^jose$': '<rootDir>/src/__tests__/mocks/jose.ts',
    },

    testMatch: [
        '<rootDir>/src/__tests__/**/*.test.ts',
        '<rootDir>/src/tests/**/*.test.ts',
    ],

    setupFilesAfterEnv: ['<rootDir>/src/__tests__/setupTests.ts'],

    transformIgnorePatterns: ['/node_modules/'],

    // Evita el duplicado de mocks en dist/
    modulePathIgnorePatterns: ['<rootDir>/dist/'],

    collectCoverageFrom: [
        'src/**/*.{ts,tsx}',
        '!src/**/index.ts',
        '!src/**/types.ts',
        '!src/__tests__/setupTests.ts',
    ],
};

export default config;
