// ConfigurationBuilder.ts
import Authenticator from '@/security/Authenticator';
import { createApiClient } from '@/services/ApiClient';

export default class ConfigurationBuilder {
        public static readonly localDeployment =
                (process.env.AUTH_DEFAULT_TOKEN ?? '') !== '';

        public static readonly authenticator: Authenticator = new Authenticator(
                process.env.AUTH_GROUP_ADMIN ?? '/Omnichannel-Retail/Merchandise/PPSOMX/ppsomx-admin',
                process.env.AUTH_GROUP_PROVEEDOR ?? '/Omnichannel-Retail/Merchandise/PPSOMX/ppsomx-proveedores',
                process.env.AUTH_DEFAULT_TOKEN ?? ''
        );

        public static readonly client = createApiClient({
                baseUrl: process.env.API_BASE_URL ? process.env.API_BASE_URL : '',
                tokenProvider: () =>
                        (process.env.AUTH_DEFAULT_TOKEN && process.env.AUTH_DEFAULT_TOKEN.trim() !== '')
                                ? process.env.AUTH_DEFAULT_TOKEN
                                : (require('../store/localStore').localHomeStore.getState().authentication.token ?? null),
                timeoutMs: 15000,
        });
}
