import { combineReducers, configureStore, Store } from '@reduxjs/toolkit';
import authSlice from './slices/authSlice';
import configSlice from './slices/configSlice';

export type RootState = ReturnType<typeof rootReducers>;
export const rootReducers = combineReducers({ authentication: authSlice , configuration: configSlice});
export const localHomeStore: Store<RootState> = configureStore({
  reducer: rootReducers,
});
