import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface Country {
    name?: string;
    [key: string]: any;
}

interface SelectedTenant {
    country?: Country;
    [key: string]: any;
}

interface Tenants {
    selectedTenant?: SelectedTenant;
    [key: string]: any;
}

export interface ConfigurationState {
    tenants?: Tenants;
    configuration?: any;
    [key: string]: any;
}

const initialState: ConfigurationState = {};

const configSlice = createSlice({
    name: "configuration",
    initialState,
    reducers: {
        configAction: (
            state,
            { payload }: PayloadAction<any>
        ) => {
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