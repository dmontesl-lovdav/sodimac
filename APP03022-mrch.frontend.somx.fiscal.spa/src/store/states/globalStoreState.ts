export interface AuthToken {
    token?: string;
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