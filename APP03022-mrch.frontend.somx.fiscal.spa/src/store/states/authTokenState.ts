export interface AuthToken {
    idToken?: string;
    refreshToken?: string;
    token?: string;
    isLogged?: boolean;
    tokenDecoded?: any;
    [key: string]: any;
}

export const AuthTokenStateDefault: AuthToken = {
    idToken: "",
    refreshToken: "",
    token: "",
    isLogged: false,
    tokenDecoded: {},
};