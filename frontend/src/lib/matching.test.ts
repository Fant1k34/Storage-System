import { describe, expect, it } from 'vitest';
import { emptyRow } from '@/features/drafts/draftsSlice';
import { countConflicts, fieldState, findByName, findPackage, normalize, packageAutofill, validateRow, type Refs } from './matching';

const refs: Refs = {
  items: [{ id: 1, name: 'Apple' }],
  units: [
    { id: 1, name: 'kg' },
    { id: 2, name: 'g' },
  ],
  currencies: [{ id: 1, name: 'rub' }],
  packages: [{ id: 1, name: 'box', weight: 0.5, weightUnitId: 1, weightUnitName: 'kg' }],
};

describe('normalize / findByName', () => {
  it('ignores case and surrounding whitespace', () => {
    expect(normalize('  Apple  Pie ')).toBe('apple pie');
    expect(findByName(refs.items, ' apple ')?.id).toBe(1);
    expect(findByName(refs.items, 'pear')).toBeUndefined();
    expect(findByName(refs.items, '   ')).toBeUndefined();
  });
});

describe('findPackage', () => {
  it('matches only when name, weight and unit all agree', () => {
    expect(findPackage(refs.packages, 'Box', 0.5, 'KG')?.id).toBe(1);
    expect(findPackage(refs.packages, 'box', 1, 'kg')).toBeUndefined();
    expect(findPackage(refs.packages, 'box', 0.5, 'g')).toBeUndefined();
    expect(findPackage(refs.packages, 'box', null, 'kg')).toBeUndefined();
  });
});

describe('fieldState', () => {
  it('is empty for blank values and existing for known ones', () => {
    const row = { ...emptyRow(), item: '' };
    expect(fieldState(row, 'item', refs, false)).toBe('empty');
    expect(fieldState({ ...row, item: 'apple' }, 'item', refs, false)).toBe('existing');
  });

  it('flags unknown values as conflicts until accepted', () => {
    const row = { ...emptyRow(), item: 'Pear' };
    expect(fieldState(row, 'item', refs, false)).toBe('new-conflict');
    expect(fieldState({ ...row, accepted: { item: 'Pear' } }, 'item', refs, false)).toBe('new-accepted');
    // changing the value after acceptance reopens the question
    expect(fieldState({ ...row, item: 'Pears', accepted: { item: 'Pear' } }, 'item', refs, false)).toBe('new-conflict');
  });

  it('auto-create turns every unknown value into accepted', () => {
    const row = { ...emptyRow(), item: 'Pear', currency: 'usd' };
    expect(fieldState(row, 'item', refs, true)).toBe('new-accepted');
    expect(fieldState(row, 'currency', refs, true)).toBe('new-accepted');
  });

  it('treats a package as a triple', () => {
    const row = { ...emptyRow(), packageName: 'box', packageWeight: 0.5, packageWeightUnit: 'kg' };
    expect(fieldState(row, 'package', refs, false)).toBe('existing');
    expect(fieldState({ ...row, packageWeight: 1 }, 'package', refs, false)).toBe('new-conflict');
    expect(fieldState({ ...row, packageWeight: null }, 'package', refs, false)).toBe('empty');
  });
});

describe('packageAutofill', () => {
  const packages = [
    { id: 1, name: 'box', weight: 0.5, weightUnitId: 1, weightUnitName: 'kg' },
    { id: 2, name: 'bag', weight: 1, weightUnitId: 1, weightUnitName: 'kg' },
    { id: 3, name: 'bag', weight: 5, weightUnitId: 1, weightUnitName: 'kg' },
  ];

  it('fills weight and unit when exactly one package has that name', () => {
    expect(packageAutofill(packages, 'Box')).toEqual({ packageName: 'box', packageWeight: 0.5, packageWeightUnit: 'kg' });
  });

  it('sets only the name when the name is ambiguous or unknown', () => {
    expect(packageAutofill(packages, 'bag')).toEqual({ packageName: 'bag' });
    expect(packageAutofill(packages, 'crate')).toEqual({ packageName: 'crate' });
  });
});

describe('countConflicts', () => {
  it('sums pending decisions across rows and fields', () => {
    const a = { ...emptyRow(), item: 'Pear', quantityUnit: 'kg', currency: 'usd' };
    const b = { ...emptyRow(), item: 'apple', quantityUnit: 'pcs' };
    expect(countConflicts([a, b], refs, false)).toBe(3);
    expect(countConflicts([a, b], refs, true)).toBe(0);
  });
});

describe('validateRow', () => {
  it('reports every missing or non-positive field', () => {
    expect(validateRow(emptyRow())).toEqual([
      'validation.item',
      'validation.quantity',
      'validation.quantityUnit',
      'validation.packageName',
      'validation.packageWeight',
      'validation.packageWeightUnit',
      'validation.price',
      'validation.currency',
    ]);
  });

  it('passes a complete row', () => {
    const full = {
      ...emptyRow(),
      item: 'apple',
      quantity: 2,
      quantityUnit: 'kg',
      packageName: 'box',
      packageWeight: 0.5,
      packageWeightUnit: 'kg',
      price: 100,
      currency: 'rub',
    };
    expect(validateRow(full)).toEqual([]);
  });
});
