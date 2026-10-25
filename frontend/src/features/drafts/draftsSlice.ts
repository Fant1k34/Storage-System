import { createSlice, nanoid, type PayloadAction } from '@reduxjs/toolkit';
import type { DraftRow, RefField, RowStatus } from './types';

export interface DraftsState {
  byDate: Record<string, DraftRow[]>;
}

const initialState: DraftsState = { byDate: {} };

export function emptyRow(): DraftRow {
  return {
    id: nanoid(),
    item: '',
    quantity: null,
    quantityUnit: '',
    packageName: '',
    packageWeight: null,
    packageWeightUnit: '',
    price: null,
    currency: '',
    accepted: {},
    status: 'draft',
  };
}

type RowPatch = Partial<Omit<DraftRow, 'id' | 'accepted' | 'status' | 'error'>>;

const draftsSlice = createSlice({
  name: 'drafts',
  initialState,
  reducers: {
    addRow(state, { payload }: PayloadAction<{ date: string; template?: Partial<DraftRow> }>) {
      const rows = (state.byDate[payload.date] ??= []);
      rows.push({ ...emptyRow(), ...payload.template });
    },
    duplicateRow(state, { payload }: PayloadAction<{ date: string; id: string }>) {
      const rows = state.byDate[payload.date] ?? [];
      const index = rows.findIndex((r) => r.id === payload.id);
      if (index < 0) return;
      const source = rows[index];
      rows.splice(index + 1, 0, { ...source, id: nanoid(), status: 'draft', error: undefined });
    },
    updateRow(state, { payload }: PayloadAction<{ date: string; id: string; patch: RowPatch }>) {
      const row = state.byDate[payload.date]?.find((r) => r.id === payload.id);
      if (!row) return;
      Object.assign(row, payload.patch);
      // Editing a failed row gives it another chance
      if (row.status === 'error') {
        row.status = 'draft';
        row.error = undefined;
      }
    },
    removeRow(state, { payload }: PayloadAction<{ date: string; id: string }>) {
      const rows = state.byDate[payload.date];
      if (!rows) return;
      state.byDate[payload.date] = rows.filter((r) => r.id !== payload.id);
      if (state.byDate[payload.date].length === 0) delete state.byDate[payload.date];
    },
    removeRows(state, { payload }: PayloadAction<{ date: string; ids: string[] }>) {
      const rows = state.byDate[payload.date];
      if (!rows) return;
      const ids = new Set(payload.ids);
      state.byDate[payload.date] = rows.filter((r) => !ids.has(r.id));
      if (state.byDate[payload.date].length === 0) delete state.byDate[payload.date];
    },
    acceptCreate(state, { payload }: PayloadAction<{ date: string; id: string; field: RefField; value: string }>) {
      const row = state.byDate[payload.date]?.find((r) => r.id === payload.id);
      if (row) row.accepted[payload.field] = payload.value;
    },
    setRowStatus(state, { payload }: PayloadAction<{ date: string; id: string; status: RowStatus; error?: string }>) {
      const row = state.byDate[payload.date]?.find((r) => r.id === payload.id);
      if (!row) return;
      row.status = payload.status;
      row.error = payload.error;
    },
    clearDate(state, { payload }: PayloadAction<{ date: string }>) {
      delete state.byDate[payload.date];
    },
    hydrate(_state, { payload }: PayloadAction<DraftsState>) {
      return payload;
    },
  },
});

export const { addRow, duplicateRow, updateRow, removeRow, removeRows, acceptCreate, setRowStatus, clearDate, hydrate } =
  draftsSlice.actions;
export default draftsSlice.reducer;

export const selectDraftsForDate = (state: { drafts: DraftsState }, date: string): DraftRow[] =>
  state.drafts.byDate[date] ?? [];
export const selectDraftCounts = (state: { drafts: DraftsState }): Record<string, number> =>
  Object.fromEntries(Object.entries(state.drafts.byDate).map(([date, rows]) => [date, rows.length]));
