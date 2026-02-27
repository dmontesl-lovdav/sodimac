module.exports = {
    root: true,
    parser: '@typescript-eslint/parser',
    plugins: ['@typescript-eslint', 'functional'],
    extends: [
        'eslint:recommended',
        'plugin:@typescript-eslint/recommended',
        'plugin:functional/external-typescript-recommended',
        'plugin:functional/recommended',
    ],
    parserOptions: { ecmaVersion: 'latest', sourceType: 'module' },
    rules: {
        'functional/no-classes': 'error',
    },
};
