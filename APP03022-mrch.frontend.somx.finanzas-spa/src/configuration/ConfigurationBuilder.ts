// src/configuration/ConfigurationBuilder.ts
import { createAuthenticator } from '@/security/Authenticator';

// ✅ flags/env
export const localDeployment =
        (process.env.AUTH_DEFAULT_TOKEN ?? '') !== '';

// ✅ autenticador funcional
export const authenticator = createAuthenticator({
        adminGroup:
                process.env.AUTH_GROUP_ADMIN ??
                '/Omnichannel-Retail/Merchandise/PPSOMX/ppsomx-admin',
        proveedorGroup:
                process.env.AUTH_GROUP_PROVEEDOR ??
                '/Omnichannel-Retail/Merchandise/PPSOMX/ppsomx-proveedores',
        defaultToken: process.env.AUTH_DEFAULT_TOKEN ?? '',
});

// 🔄 default export para mantener compatibilidad:
//   import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
//   ConfigurationBuilder.client ...
const ConfigurationBuilder = {
        localDeployment,
        authenticator,
} as const;

export default ConfigurationBuilder;
