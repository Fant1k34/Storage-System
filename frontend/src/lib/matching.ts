import type { NamedRef, PackageRef } from '@/api/types';
import type { DraftRow, RefField } from '@/features/drafts/types';

export interface Refs {
  items: NamedRef[];
  units: NamedRef[];
  currencies: NamedRef[];
  packages: PackageRef[];
}

export const EMPTY_REFS: Refs = { items: [], units: [], currencies: [], packages: [] };

// Names are compared case-insensitively with collapsed whitespace: "Apple " and "apple" are the same record
export const normalize = (value: string): string => value.trim().toLowerCase().replace(/\s+/g, ' ');

export function findByName<T extends { name: string }>(list: T[], name: string): T | undefined {
  const key = normalize(name);
  if (!key) return undefined;
  return list.find((entry) => normalize(entry.name) === key);
}

export function findPackage(
  packages: PackageRef[],
  name: string,
  weight: number | null,
  unitName: string,
): PackageRef | undefined {
  if (!name.trim() || weight === null || !unitName.trim()) return undefined;
  const nameKey = normalize(name);
  const unitKey = normalize(unitName);
  return packages.find(
    (p) => normalize(p.name) === nameKey && Number(p.weight) === Number(weight) && normalize(p.weightUnitName) === unitKey,
  );
}

// Picking a package by name fills weight and unit only when the name is unambiguous:
// exactly one package with that name exists in the catalogue. Otherwise only the name is set.
export function packageAutofill(
  packages: PackageRef[],
  name: string,
): { packageName: string; packageWeight?: number; packageWeightUnit?: string } {
  const key = normalize(name);
  const matches = packages.filter((p) => normalize(p.name) === key);
  if (matches.length === 1) {
    const [p] = matches;
    return { packageName: p.name, packageWeight: Number(p.weight), packageWeightUnit: p.weightUnitName };
  }
  return { packageName: name };
}

// Stable key for "the same package" across rows, used to deduplicate creation and to track acceptance
export const packageKey = (name: string, weight: number | null, unitName: string): string =>
  `${normalize(name)}|${weight ?? ''}|${normalize(unitName)}`;

export type FieldState =
  | 'empty' // nothing entered yet (or, for package, not all three parts entered)
  | 'existing' // matches a record from the backend
  | 'new-accepted' // not in the backend, the user agreed to create it (or auto-create is on)
  | 'new-conflict'; // not in the backend, decision pending

export function refValue(row: DraftRow, field: RefField): string {
  switch (field) {
    case 'item':
      return row.item;
    case 'quantityUnit':
      return row.quantityUnit;
    case 'packageWeightUnit':
      return row.packageWeightUnit;
    case 'currency':
      return row.currency;
    case 'package':
      return packageKey(row.packageName, row.packageWeight, row.packageWeightUnit);
  }
}

export function fieldState(row: DraftRow, field: RefField, refs: Refs, autoCreate: boolean): FieldState {
  let exists: boolean;
  if (field === 'package') {
    if (!row.packageName.trim() || row.packageWeight === null || !row.packageWeightUnit.trim()) return 'empty';
    exists = findPackage(refs.packages, row.packageName, row.packageWeight, row.packageWeightUnit) !== undefined;
  } else {
    const value = refValue(row, field);
    if (!value.trim()) return 'empty';
    const list = field === 'item' ? refs.items : field === 'currency' ? refs.currencies : refs.units;
    exists = findByName(list, value) !== undefined;
  }
  if (exists) return 'existing';
  if (autoCreate) return 'new-accepted';
  return row.accepted[field] === refValue(row, field) ? 'new-accepted' : 'new-conflict';
}

export const REF_FIELDS: RefField[] = ['item', 'quantityUnit', 'packageWeightUnit', 'currency', 'package'];

export function rowConflicts(row: DraftRow, refs: Refs, autoCreate: boolean): RefField[] {
  return REF_FIELDS.filter((field) => fieldState(row, field, refs, autoCreate) === 'new-conflict');
}

export function countConflicts(rows: DraftRow[], refs: Refs, autoCreate: boolean): number {
  return rows.reduce((sum, row) => sum + rowConflicts(row, refs, autoCreate).length, 0);
}

// Field-level completeness checks; returns i18n keys so the UI can render them in the active language
export type ValidationKey =
  | 'validation.item'
  | 'validation.quantity'
  | 'validation.quantityUnit'
  | 'validation.packageName'
  | 'validation.packageWeight'
  | 'validation.packageWeightUnit'
  | 'validation.price'
  | 'validation.currency';

const positive = (value: number | null): boolean => value !== null && Number.isFinite(value) && value > 0;

export function validateRow(row: DraftRow): ValidationKey[] {
  const errors: ValidationKey[] = [];
  if (!row.item.trim()) errors.push('validation.item');
  if (!positive(row.quantity)) errors.push('validation.quantity');
  if (!row.quantityUnit.trim()) errors.push('validation.quantityUnit');
  if (!row.packageName.trim()) errors.push('validation.packageName');
  if (!positive(row.packageWeight)) errors.push('validation.packageWeight');
  if (!row.packageWeightUnit.trim()) errors.push('validation.packageWeightUnit');
  if (!positive(row.price)) errors.push('validation.price');
  if (!row.currency.trim()) errors.push('validation.currency');
  return errors;
}
