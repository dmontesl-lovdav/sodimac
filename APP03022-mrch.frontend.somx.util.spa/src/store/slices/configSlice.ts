import { createSlice, PayloadAction } from "@reduxjs/toolkit";

const configSlice = createSlice({
    name: "configuration",
    initialState: {},
    reducers: {
        configAction: (state, { payload }: PayloadAction<any>) => {
            return {
                ...state,
                ...payload,
                configuration: payload,
            };
        },
    },
});

export const { configAction } = configSlice.actions;
export default configSlice.reducer;