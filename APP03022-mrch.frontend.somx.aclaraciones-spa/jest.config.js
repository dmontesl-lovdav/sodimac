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
};
