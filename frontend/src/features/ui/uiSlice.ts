import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import dayjs from 'dayjs';

export type ThemeMode = 'system' | 'light' | 'dark';
export type Locale = 'ru' | 'en';
export type Mode = 'view' | 'edit';

export interface UiState {
  themeMode: ThemeMode;
  locale: Locale;
  selectedDate: string; // YYYY-MM-DD
  mode: Mode;
  autoCreate: boolean;
}

export const today = () => dayjs().format('YYYY-MM-DD');

const initialState: UiState = {
  themeMode: 'system',
  locale: 'ru',
  selectedDate: today(),
  mode: 'view',
  autoCreate: false,
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    setThemeMode(state, { payload }: PayloadAction<ThemeMode>) {
      state.themeMode = payload;
    },
    setLocale(state, { payload }: PayloadAction<Locale>) {
      state.locale = payload;
    },
    setSelectedDate(state, { payload }: PayloadAction<string>) {
      state.selectedDate = payload;
    },
    setMode(state, { payload }: PayloadAction<Mode>) {
      state.mode = payload;
    },
    setAutoCreate(state, { payload }: PayloadAction<boolean>) {
      state.autoCreate = payload;
    },
    hydrateUi(state, { payload }: PayloadAction<Partial<Pick<UiState, 'themeMode' | 'locale' | 'autoCreate'>>>) {
      Object.assign(state, payload);
    },
  },
});

export const { setThemeMode, setLocale, setSelectedDate, setMode, setAutoCreate, hydrateUi } = uiSlice.actions;
export default uiSlice.reducer;
