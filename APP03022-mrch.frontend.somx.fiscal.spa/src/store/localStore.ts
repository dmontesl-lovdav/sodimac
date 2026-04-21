/*
Repositorio para almacenar el store local de fiscal, para evitar mezclarlo con el store global de la aplicación.
*/
import { configureStore, createSlice } from '@reduxjs/toolkit';

const uiSlice = createSlice({
    name: 'ui',
    initialState: { greeting: 'Bienvenido al módulo Fiscal', isLoading: false, error: null },
    reducers: {}
});

export const localHomeStore = configureStore({
    reducer: { ui: uiSlice.reducer, configuration: (state = {}) => state, authentication: (state = { }) => state },
});

export type RootState = ReturnType<typeof localHomeStore.getState>;
export type AppDispatch = typeof localHomeStore.dispatch;
