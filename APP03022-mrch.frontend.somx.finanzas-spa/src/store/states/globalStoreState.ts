export interface AuthToken {
    idToken?: string;
    refreshToken?: string;
    token?: string;
    isLogged?: boolean;
    tokenDecoded?: any;
    [key: string]: any;
}

export interface Configuration {
    [key: string]: any;
}

export interface GlobalStoreState {
    authentication: AuthToken;
    configuration: Configuration;
}