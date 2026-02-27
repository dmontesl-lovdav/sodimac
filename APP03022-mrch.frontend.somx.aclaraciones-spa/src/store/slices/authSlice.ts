import { AuthToken } from "@rtl/mrch.frontend.cross.common-interfaces";
import { AuthTokenStateDefault } from "../states/authTokenState";
import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import jwtDecode from "jwt-decode";

const authSlice = createSlice({
  name: "authentication",
  initialState: AuthTokenStateDefault,
  reducers: {
    logInAction: (state, { payload }: PayloadAction<AuthToken>) => {
      return {
        ...state,
        ...payload,
        tokenDecoded: jwtDecode(payload.token || ""),
      };
    },
  },
});

export const { logInAction } = authSlice.actions;
export default authSlice.reducer;
