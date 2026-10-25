import { useAppDispatch } from '@/app/hooks';
import { acceptCreate, updateRow } from '@/features/drafts/draftsSlice';
import type { DraftRow, RefField } from '@/features/drafts/types';
import { useT } from '@/i18n';
import { fieldState, packageAutofill, validateRow, type Refs, type ValidationKey } from '@/lib/matching';
import { NumberCell } from './cells/NumberCell';
import { ReferenceCell } from './cells/ReferenceCell';

interface Ctx {
  date: string;
  refs: Refs;
  autoCreate: boolean;
  disabled: boolean;
}

// Builds the editable cell for every column of a draft row. Shared by the desktop table and mobile cards.
export function useDraftCells(ctx: Ctx) {
  const dispatch = useAppDispatch();
  const { t } = useT();
  const { date, refs, autoCreate, disabled } = ctx;

  const names = {
    items: refs.items.map((r) => r.name),
    units: refs.units.map((r) => r.name),
    currencies: refs.currencies.map((r) => r.name),
    packages: Array.from(new Set(refs.packages.map((p) => p.name))),
  };

  const patch = (row: DraftRow, values: Parameters<typeof updateRow>[0]['patch']) =>
    dispatch(updateRow({ date, id: row.id, patch: values }));
  const accept = (row: DraftRow, field: RefField, value: string) => dispatch(acceptCreate({ date, id: row.id, field, value }));

  const isDisabled = (row: DraftRow) => disabled || row.status === 'syncing';

  // After a failed sync every field that validateRow complains about is marked, with the same wording as the row error
  const problems = (row: DraftRow): Set<ValidationKey> => (row.status === 'error' ? new Set(validateRow(row)) : new Set());
  const invalidProps = (row: DraftRow, key: ValidationKey) => {
    const invalid = problems(row).has(key);
    return { invalid, invalidHint: invalid ? t(key) : undefined };
  };

  return {
    item: (row: DraftRow) => (
      <ReferenceCell
        value={row.item}
        options={names.items}
        state={fieldState(row, 'item', refs, autoCreate)}
        hintKey="hint.item"
        placeholder={t('col.item')}
        disabled={isDisabled(row)}
        {...invalidProps(row, 'validation.item')}
        onChange={(v) => patch(row, { item: v })}
        onAccept={() => accept(row, 'item', row.item)}
        onPick={(v) => patch(row, { item: v })}
      />
    ),
    quantity: (row: DraftRow) => (
      <NumberCell value={row.quantity} onChange={(v) => patch(row, { quantity: v })} placeholder={t('col.quantity')} label={t('col.quantity')} disabled={isDisabled(row)} {...invalidProps(row, 'validation.quantity')} />
    ),
    quantityUnit: (row: DraftRow) => (
      <ReferenceCell
        value={row.quantityUnit}
        options={names.units}
        state={fieldState(row, 'quantityUnit', refs, autoCreate)}
        hintKey="hint.unit"
        placeholder={t('col.quantityUnit')}
        disabled={isDisabled(row)}
        {...invalidProps(row, 'validation.quantityUnit')}
        onChange={(v) => patch(row, { quantityUnit: v })}
        onAccept={() => accept(row, 'quantityUnit', row.quantityUnit)}
        onPick={(v) => patch(row, { quantityUnit: v })}
      />
    ),
    packageName: (row: DraftRow) => (
      <ReferenceCell
        value={row.packageName}
        options={names.packages}
        state={fieldState(row, 'package', refs, autoCreate)}
        hintKey="hint.package"
        hintExtra={t('hint.packageParts')}
        placeholder={t('col.packageName')}
        disabled={isDisabled(row)}
        {...invalidProps(row, 'validation.packageName')}
        onChange={(v) => patch(row, { packageName: v })}
        onSelect={(v) => patch(row, packageAutofill(refs.packages, v))}
        onAccept={() => accept(row, 'package', `${row.packageName.trim().toLowerCase().replace(/\s+/g, ' ')}|${row.packageWeight ?? ''}|${row.packageWeightUnit.trim().toLowerCase().replace(/\s+/g, ' ')}`)}
        onPick={(v) => patch(row, packageAutofill(refs.packages, v))}
      />
    ),
    packageWeight: (row: DraftRow) => (
      <NumberCell value={row.packageWeight} onChange={(v) => patch(row, { packageWeight: v })} placeholder={t('col.packageWeight')} label={t('col.packageWeight')} disabled={isDisabled(row)} {...invalidProps(row, 'validation.packageWeight')} />
    ),
    packageWeightUnit: (row: DraftRow) => (
      <ReferenceCell
        value={row.packageWeightUnit}
        options={names.units}
        state={fieldState(row, 'packageWeightUnit', refs, autoCreate)}
        hintKey="hint.unit"
        placeholder={t('col.packageWeightUnit')}
        disabled={isDisabled(row)}
        {...invalidProps(row, 'validation.packageWeightUnit')}
        onChange={(v) => patch(row, { packageWeightUnit: v })}
        onAccept={() => accept(row, 'packageWeightUnit', row.packageWeightUnit)}
        onPick={(v) => patch(row, { packageWeightUnit: v })}
      />
    ),
    price: (row: DraftRow) => (
      <NumberCell value={row.price} onChange={(v) => patch(row, { price: v })} placeholder={t('col.price')} label={t('col.price')} disabled={isDisabled(row)} {...invalidProps(row, 'validation.price')} />
    ),
    currency: (row: DraftRow) => (
      <ReferenceCell
        value={row.currency}
        options={names.currencies}
        state={fieldState(row, 'currency', refs, autoCreate)}
        hintKey="hint.currency"
        placeholder={t('col.currency')}
        disabled={isDisabled(row)}
        {...invalidProps(row, 'validation.currency')}
        onChange={(v) => patch(row, { currency: v })}
        onAccept={() => accept(row, 'currency', row.currency)}
        onPick={(v) => patch(row, { currency: v })}
      />
    ),
  };
}
