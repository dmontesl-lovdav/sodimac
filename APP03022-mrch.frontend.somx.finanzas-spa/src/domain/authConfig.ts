import { APP_NAME, AUTH_CONFIG_CLIENT } from "@shared/constant/environment";

export interface AuthConfig {
    client: string;
    appName: string;
}

export const AuthConfigDefault: AuthConfig = {
    client: AUTH_CONFIG_CLIENT,
    appName: APP_NAME,
};
