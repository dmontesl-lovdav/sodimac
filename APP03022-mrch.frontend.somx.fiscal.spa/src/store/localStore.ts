import { combineReducers, configureStore, Store } from "@reduxjs/toolkit";
import authSlice from "./slices/authSlice";
import configSlice from "./slices/configSlice";

export const rootReducers = combineReducers({
    authentication: authSlice,
    configuration: configSlice,
});

export type RootState = ReturnType<typeof rootReducers>;

export const localHomeStore: Store<RootState> = configureStore({
    reducer: rootReducers,
});

export type AppDispatch = typeof localHomeStore.dispatch;