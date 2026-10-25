import { describe, expect, it } from 'vitest';
import { plural, translate } from './index';

describe('i18n', () => {
  it('substitutes variables and falls back to the key', () => {
    expect(translate('ru', 'sync.done', { saved: 3 })).toBe('Сохранено записей: 3');
    expect(translate('en', 'sync.done', { saved: 3 })).toBe('Saved records: 3');
  });

  it('picks russian plural forms', () => {
    const keys = { one: 'bar.rowsOne', few: 'bar.rowsFew', many: 'bar.rows' } as const;
    expect(plural('ru', 1, keys)).toBe('bar.rowsOne');
    expect(plural('ru', 3, keys)).toBe('bar.rowsFew');
    expect(plural('ru', 5, keys)).toBe('bar.rows');
    expect(plural('ru', 11, keys)).toBe('bar.rows');
    expect(plural('ru', 21, keys)).toBe('bar.rowsOne');
    expect(plural('en', 1, keys)).toBe('bar.rowsOne');
    expect(plural('en', 2, keys)).toBe('bar.rows');
  });
});
