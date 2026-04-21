import { configureStore, createSlice } from '@reduxjs/toolkit';

const catalogosSlice = createSlice({
  name: 'catalogos',
  initialState: {
    loading: false,
  },
  reducers: {
    setLoading: (state, action) => {
      state.loading = action.payload;
    },
  },
});

export const { setLoading } = catalogosSlice.actions;

export const catalogosStore = configureStore({
  reducer: {
    catalogos: catalogosSlice.reducer,
  },
});

export type RootState = ReturnType<typeof catalogosStore.getState>;
export type AppDispatch = typeof catalogosStore.dispatch;








