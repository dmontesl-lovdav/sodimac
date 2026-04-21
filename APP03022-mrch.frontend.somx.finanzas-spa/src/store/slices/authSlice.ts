import { AuthTokenStateDefault } from "../states/authTokenState";
import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { jwtDecode } from "jwt-decode";

type AuthToken = {
    token?: string;
    tokenDecoded?: any;
    isLogged?: boolean;
    [key: string]: any;
};

const authSlice = createSlice({
    name: "authentication",
    initialState: AuthTokenStateDefault,
    reducers: {
        logInAction: (state, { payload }: PayloadAction<AuthToken>) => {
            const hasToken = Boolean(payload.token?.trim());

            console.log("🪪 LOGIN PAYLOAD:", payload);
            console.log("🪪 HAS TOKEN:", hasToken);

            return {
                ...state,
                ...payload,
                isLogged: hasToken,
                tokenDecoded: hasToken ? jwtDecode(payload.token as string) : {},
            };
        },
    },
});

export const { logInAction } = authSlice.actions;
export default authSlice.reducer;