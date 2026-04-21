// src/configuration/ConfigurationBuilder.ts

import { createApiClient } from '@/services/ApiClient';
import { createAuthenticator } from '@/security/Authenticator';
import { localHomeStore } from '@/store/localStore';

/* ========================================= */
/*               FLAGS                       */
/* ========================================= */

const localDeploymentEnv = process.env.LOCAL_DEPLOYMENT ?? process.env.REACT_APP_LOCAL_DEPLOYMENT;

export const localDeployment =
        localDeploymentEnv !== undefined
                ? localDeploymentEnv.toLowerCase() === 'true'
                : (process.env.AUTH_DEFAULT_TOKEN ?? '') !== '';

/* ========================================= */
/*             AUTHENTICATOR                 */
/* ========================================= */

export const authenticator = createAuthenticator({
        adminGroup:
                process.env.AUTH_GROUP_ADMIN ??
                '/Omnichannel-Retail/Merchandise/PPSOMX/ppsomx-admin',
        proveedorGroup:
                process.env.AUTH_GROUP_PROVEEDOR ??
                '/Omnichannel-Retail/Merchandise/PPSOMX/ppsomx-proveedores',
        defaultToken: process.env.AUTH_DEFAULT_TOKEN ?? '',
});

/* ========================================= */
/*               API CLIENT                  */
/* ========================================= */

export const client = createApiClient({
        baseUrl: process.env.API_BASE_URL ?? '',
        tokenProvider: () => {
                const envToken = process.env.AUTH_DEFAULT_TOKEN;

                if (envToken?.trim()) return envToken;

                return (
                        (localHomeStore.getState() as any)?.authentication?.token ??
                        null
                );
        },
        timeoutMs: 15000,
});

/* ========================================= */
/*         DEFAULT EXPORT COMPATIBLE         */
/* ========================================= */

const ConfigurationBuilder = {
        localDeployment,
        authenticator,
        client,
} as const;

export default ConfigurationBuilder;