import { describe, expect, it } from 'vitest';
import reducer, { acceptCreate, addRow, clearDate, duplicateRow, removeRow, setRowStatus, updateRow } from './draftsSlice';

const DATE = '2026-10-25';

describe('draftsSlice', () => {
  it('keeps rows per date and removes empty dates', () => {
    let state = reducer(undefined, addRow({ date: DATE }));
    state = reducer(state, addRow({ date: '2026-10-26' }));
    expect(Object.keys(state.byDate)).toEqual([DATE, '2026-10-26']);

    const id = state.byDate[DATE][0].id;
    state = reducer(state, removeRow({ date: DATE, id }));
    expect(state.byDate[DATE]).toBeUndefined();
    expect(state.byDate['2026-10-26']).toHaveLength(1);
  });

  it('editing a failed row resets its error', () => {
    let state = reducer(undefined, addRow({ date: DATE }));
    const id = state.byDate[DATE][0].id;
    state = reducer(state, setRowStatus({ date: DATE, id, status: 'error', error: 'boom' }));
    expect(state.byDate[DATE][0].status).toBe('error');
    state = reducer(state, updateRow({ date: DATE, id, patch: { item: 'apple' } }));
    expect(state.byDate[DATE][0]).toMatchObject({ item: 'apple', status: 'draft', error: undefined });
  });

  it('duplicates a row right after the source with a fresh id', () => {
    let state = reducer(undefined, addRow({ date: DATE, template: { item: 'apple' } }));
    state = reducer(state, addRow({ date: DATE, template: { item: 'pear' } }));
    const first = state.byDate[DATE][0];
    state = reducer(state, duplicateRow({ date: DATE, id: first.id }));
    const [a, copy, b] = state.byDate[DATE];
    expect(copy.item).toBe('apple');
    expect(copy.id).not.toBe(a.id);
    expect(b.item).toBe('pear');
  });

  it('records accepted values and clears a whole day', () => {
    let state = reducer(undefined, addRow({ date: DATE }));
    const id = state.byDate[DATE][0].id;
    state = reducer(state, acceptCreate({ date: DATE, id, field: 'item', value: 'Pear' }));
    expect(state.byDate[DATE][0].accepted.item).toBe('Pear');
    state = reducer(state, clearDate({ date: DATE }));
    expect(state.byDate).toEqual({});
  });
});
