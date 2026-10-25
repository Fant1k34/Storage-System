import { configureStore } from '@reduxjs/toolkit';
import { skladApi } from '@/api/skladApi';
import draftsReducer, { hydrate, type DraftsState } from '@/features/drafts/draftsSlice';
import uiReducer, { hydrateUi, type UiState } from '@/features/ui/uiSlice';
import { loadJson, saveJson } from './persist';

const DRAFTS_KEY = 'sklad.drafts.v1';
const UI_KEY = 'sklad.ui.v1';

export function makeStore(preloaded?: { drafts?: DraftsState; ui?: Partial<UiState> }) {
  const store = configureStore({
    reducer: {
      [skladApi.reducerPath]: skladApi.reducer,
      drafts: draftsReducer,
      ui: uiReducer,
    },
    middleware: (getDefault) => getDefault().concat(skladApi.middleware),
  });
  if (preloaded?.drafts) store.dispatch(hydrate(preloaded.drafts));
  if (preloaded?.ui) store.dispatch(hydrateUi(preloaded.ui));
  return store;
}

export function makePersistedStore() {
  const store = makeStore({
    drafts: loadJson<DraftsState>(DRAFTS_KEY),
    ui: loadJson<Partial<UiState>>(UI_KEY),
  });
  let lastDrafts = store.getState().drafts;
  let lastUi = store.getState().ui;
  store.subscribe(() => {
    const { drafts, ui } = store.getState();
    if (drafts !== lastDrafts) {
      lastDrafts = drafts;
      saveJson(DRAFTS_KEY, drafts);
    }
    if (ui !== lastUi) {
      lastUi = ui;
      saveJson(UI_KEY, { themeMode: ui.themeMode, locale: ui.locale, autoCreate: ui.autoCreate });
    }
  });
  return store;
}

export type AppStore = ReturnType<typeof makeStore>;
export type RootState = ReturnType<AppStore['getState']>;
export type AppDispatch = AppStore['dispatch'];
