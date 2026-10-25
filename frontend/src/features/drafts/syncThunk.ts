import { createAsyncThunk } from '@reduxjs/toolkit';
import { errorMessage, skladApi } from '@/api/skladApi';
import type { NamedRef, PackageRef } from '@/api/types';
import type { AppDispatch, RootState } from '@/app/store';
import { translate } from '@/i18n';
import { findByName, findPackage, normalize, packageKey, rowConflicts, validateRow, type Refs } from '@/lib/matching';
import { removeRows, setRowStatus } from './draftsSlice';
import type { DraftRow } from './types';

export interface SyncResult {
  saved: number;
  failed: number;
}

// Sends every draft of the given day to the backend:
//  1. reloads reference data so decisions are made against the current state,
//  2. creates missing units / items / currencies / packages (each distinct value once),
//  3. posts purchases one by one; saved rows leave the drafts, failed rows stay with the error text.
export const syncDate = createAsyncThunk<SyncResult, { date: string }, { state: RootState; dispatch: AppDispatch }>(
  'drafts/syncDate',
  async ({ date }, { dispatch, getState }) => {
    const { locale, autoCreate } = getState().ui;
    const t = (key: Parameters<typeof translate>[1], vars?: Record<string, string | number>) => translate(locale, key, vars);
    const rows = (getState().drafts.byDate[date] ?? []).filter((row) => row.status !== 'syncing');
    if (rows.length === 0) return { saved: 0, failed: 0 };

    const fetchAll = async (): Promise<Refs> => {
      const [items, units, currencies, packages] = await Promise.all([
        dispatch(skladApi.endpoints.getItems.initiate(undefined, { forceRefetch: true })).unwrap(),
        dispatch(skladApi.endpoints.getUnits.initiate(undefined, { forceRefetch: true })).unwrap(),
        dispatch(skladApi.endpoints.getCurrencies.initiate(undefined, { forceRefetch: true })).unwrap(),
        dispatch(skladApi.endpoints.getPackages.initiate(undefined, { forceRefetch: true })).unwrap(),
      ]);
      return { items, units, currencies, packages };
    };

    let refs: Refs;
    try {
      refs = await fetchAll();
    } catch (err) {
      const message = t('sync.refsFailed', { reason: errorMessage(err) });
      rows.forEach((row) => dispatch(setRowStatus({ date, id: row.id, status: 'error', error: message })));
      return { saved: 0, failed: rows.length };
    }

    // Each new value is created once even when several rows use it
    const created = {
      units: new Map<string, NamedRef>(),
      items: new Map<string, NamedRef>(),
      currencies: new Map<string, NamedRef>(),
      packages: new Map<string, PackageRef>(),
    };

    const resolveNamed = async (
      kind: 'units' | 'items' | 'currencies',
      value: string,
    ): Promise<NamedRef> => {
      const existing = findByName(refs[kind], value) ?? created[kind].get(normalize(value));
      if (existing) return existing;
      const body = { name: value.trim() };
      const endpoint =
        kind === 'units'
          ? skladApi.endpoints.addUnit
          : kind === 'items'
            ? skladApi.endpoints.addItem
            : skladApi.endpoints.addCurrency;
      const result = await dispatch(endpoint.initiate(body)).unwrap();
      created[kind].set(normalize(value), result);
      return result;
    };

    const resolvePackage = async (row: DraftRow, unit: NamedRef): Promise<PackageRef> => {
      const key = packageKey(row.packageName, row.packageWeight, row.packageWeightUnit);
      const existing = findPackage(refs.packages, row.packageName, row.packageWeight, row.packageWeightUnit) ?? created.packages.get(key);
      if (existing) return existing;
      const result = await dispatch(
        skladApi.endpoints.addPackage.initiate({
          name: row.packageName.trim(),
          weight: row.packageWeight as number,
          weightUnitId: unit.id,
        }),
      ).unwrap();
      created.packages.set(key, result);
      return result;
    };

    let saved = 0;
    let failed = 0;

    for (const row of rows) {
      const validation = validateRow(row);
      if (validation.length > 0) {
        failed += 1;
        dispatch(setRowStatus({ date, id: row.id, status: 'error', error: validation.map((key) => t(key)).join('; ') }));
        continue;
      }
      if (rowConflicts(row, refs, autoCreate).length > 0) {
        failed += 1;
        dispatch(setRowStatus({ date, id: row.id, status: 'error', error: t('sync.unresolved') }));
        continue;
      }

      dispatch(setRowStatus({ date, id: row.id, status: 'syncing' }));
      try {
        const quantityUnit = await resolveNamed('units', row.quantityUnit);
        const packageUnit = await resolveNamed('units', row.packageWeightUnit);
        const item = await resolveNamed('items', row.item);
        const currency = await resolveNamed('currencies', row.currency);
        const pack = await resolvePackage(row, packageUnit);

        const ok = await dispatch(
          skladApi.endpoints.addPurchase.initiate({
            date,
            itemId: item.id,
            quantity: row.quantity as number,
            quantityUnitId: quantityUnit.id,
            packageId: pack.id,
            boughtPrice: row.price as number,
            currency: currency.id,
          }),
        ).unwrap();

        if (ok) {
          saved += 1;
          dispatch(removeRows({ date, ids: [row.id] }));
        } else {
          failed += 1;
          dispatch(setRowStatus({ date, id: row.id, status: 'error', error: t('sync.rejected') }));
        }
      } catch (err) {
        failed += 1;
        dispatch(setRowStatus({ date, id: row.id, status: 'error', error: errorMessage(err) }));
      }
    }

    dispatch(skladApi.util.invalidateTags(['Items', 'Units', 'Currencies', 'Packages', { type: 'Purchases', id: date }]));
    return { saved, failed };
  },
);
