import { configureStore, createSlice } from '@reduxjs/toolkit';

const uiSlice = createSlice({
    name: 'ui',
    initialState: { greeting: 'Bienvenida al Centro de Ayuda' },
    reducers: {}
});

export const localHomeStore = configureStore({
    reducer: { ui: uiSlice.reducer }
});

export type RootState = ReturnType<typeof localHomeStore.getState>;
export type AppDispatch = typeof localHomeStore.dispatch;
