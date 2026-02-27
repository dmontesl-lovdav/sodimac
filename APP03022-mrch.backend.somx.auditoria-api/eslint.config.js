// eslint.config.js
import js from '@eslint/js';
import tseslint from 'typescript-eslint';

export default tseslint.config(
    // ignores globales
    { ignores: ['dist/**', '**/*.test.*', '**/*.spec.*'] },

    // reglas base JS
    js.configs.recommended,

    // reglas TS sin type-check (rápidas)
    ...tseslint.configs.recommended,

    // aplica a TS/JS
    {
        files: ['**/*.{ts,js}'],
        languageOptions: {
            ecmaVersion: 'latest',
            sourceType: 'module'
        },
        rules: {
            // tus ajustes aquí si quieres
        }
    }
);
