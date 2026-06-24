export type TokenParsed = {
    [key: string]: any;
};

export type AuthToken = {
    idToken?: string;
    refreshToken?: string;
    token?: string;
    isLogged: boolean;
    tokenDecoded: TokenParsed;
};

export const AuthTokenStateDefault: AuthToken = {
    idToken: undefined,
    refreshToken: undefined,
    token: undefined,
    isLogged: false,
    tokenDecoded: {},
};
