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
    },

    testMatch: [
        '<rootDir>/src/exceptions/__tests__/*.test.ts',
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

    collectCoverageFrom: [
        'src/exceptions/HttpException.ts',
        'src/exceptions/GenericException.ts',
        'src/exceptions/ConflictException.ts',
    ],

    clearMocks: true,
    restoreMocks: true,
};
