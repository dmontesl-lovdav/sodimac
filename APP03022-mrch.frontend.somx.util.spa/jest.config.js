module.exports = {
  testEnvironment: 'jsdom',

  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
    '^@shared/(.*)$': '<rootDir>/src/shared/$1',
    '^@assets/(.*)$': '<rootDir>/src/assets/$1',
    '\\.(css|scss)$': '<rootDir>/jest.mock.css.js'
  },

  transform: {
    '^.+\\.[jt]sx?$': 'babel-jest'
  },

  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],

  collectCoverage: true,
  coverageDirectory: 'coverage',
  coverageReporters: ['lcov', 'text-summary', 'cobertura'],
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/**/index.ts',
    '!src/main.tsx',
    '!src/assets/**'
  ],
};
