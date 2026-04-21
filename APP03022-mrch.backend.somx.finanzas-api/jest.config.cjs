/** @type {import('jest').Config} */
module.exports = {
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
        '^@/(.*)\\.js$': '<rootDir>/src/$1',
        '^@/(.*)$': '<rootDir>/src/$1',
        '^(\\.{1,2}/.*)\\.js$': '$1',
        '^jose$': '<rootDir>/src/__tests__/mocks/jose.ts',
    },

    testMatch: [
        '<rootDir>/src/__tests__/**/*.test.ts',
        '<rootDir>/src/tests/**/*.test.ts',
    ],

    transformIgnorePatterns: ['/node_modules/'],
    modulePathIgnorePatterns: ['<rootDir>/dist/'],

    collectCoverageFrom: [
        'src/**/*.{ts,tsx}',
        '!src/**/index.ts',
        '!src/**/types.ts',
    ],
};
