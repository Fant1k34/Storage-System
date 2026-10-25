import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';
import { makeStore } from './app/store';
import { installMockBackend } from './test/mockBackend';

// Desktop breakpoint so the table (not the cards) renders
Object.defineProperty(window, 'innerWidth', { configurable: true, value: 1280 });

function renderApp() {
  const store = makeStore({ ui: { locale: 'ru', themeMode: 'light' } });
  render(
    <Provider store={store}>
      <App />
    </Provider>,
  );
  return store;
}

describe('App', { timeout: 30_000 }, () => {
  let backend: ReturnType<typeof installMockBackend>;
  beforeEach(() => {
    backend = installMockBackend();
  });
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('loads reference data, flags an unknown item, lets the user accept it and syncs the row', async () => {
    const user = userEvent.setup();
    const store = renderApp();

    // reference data requested up front
    await waitFor(() => {
      const paths = backend.calls.map((c) => c.path);
      expect(paths).toEqual(
        expect.arrayContaining(['/api/v1/get-all-items', '/api/v1/get-all-measures', '/api/v1/get-all-currencies', '/api/v1/get-all-packages']),
      );
    });
    expect(await screen.findByText('За этот день закупок пока нет')).toBeInTheDocument();

    // switch to edit mode, add a row
    await user.click(screen.getByText('Редактирование'));
    await user.click((await screen.findAllByRole('button', { name: /Добавить строку/ }))[0]);

    const date = store.getState().ui.selectedDate;
    await waitFor(() => expect(store.getState().drafts.byDate[date]).toHaveLength(1));
    const rowId = store.getState().drafts.byDate[date][0].id;

    // sync is disabled while required fields are empty? no: only conflicts block it, fields are validated on sync
    const syncButton = screen.getByRole('button', { name: /Синхронизировать/ });
    expect(syncButton).toBeEnabled();

    // type an unknown item -> conflict dot + disabled sync
    const itemInput = screen.getByRole('combobox', { name: 'Товар' });
    await user.type(itemInput, 'pear');
    await waitFor(() => expect(store.getState().drafts.byDate[date][0].item).toBe('pear'));
    expect(screen.getByText(/требует решения/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Синхронизировать/ })).toBeDisabled();

    // open the hint and accept creation
    await user.click(screen.getByRole('button', { name: 'Новое значение' }));
    const hint = await screen.findByText('Товара «pear» ещё нет в справочнике.');
    await user.click(within(hint.closest('.hint') as HTMLElement).getByRole('button', { name: 'Добавить' }));
    await waitFor(() => expect(store.getState().drafts.byDate[date][0].accepted.item).toBe('pear'));
    // conflict resolved, but the row is still incomplete, so it is not "ready" yet
    expect(screen.queryByText('Всё готово к отправке')).not.toBeInTheDocument();
    expect(screen.getByText('1 строка не заполнена')).toBeInTheDocument();

    // fill the remaining fields with existing values via the store (typing into every antd control is slow in jsdom)
    store.dispatch({
      type: 'drafts/updateRow',
      payload: {
        date,
        id: rowId,
        patch: { quantity: 2, quantityUnit: 'kg', packageName: 'box', packageWeight: 0.5, packageWeightUnit: 'kg', price: 100, currency: 'rub' },
      },
    });

    await user.click(screen.getByRole('button', { name: /Синхронизировать/ }));

    await waitFor(() => expect(store.getState().drafts.byDate[date]).toBeUndefined(), { timeout: 8000 });
    const posts = backend.calls.filter((c) => c.method === 'POST');
    expect(posts.map((c) => c.path)).toEqual(['/api/v1/add-item', '/api/v1/add-purchase-operation']);
    expect(posts[0].body).toEqual({ name: 'pear' });
    expect(posts[1].body).toMatchObject({ date, itemId: 2, quantity: 2, quantityUnitId: 1, packageId: 1, boughtPrice: 100, currency: 1 });

    // back in view mode with the synced row visible
    await waitFor(() => expect(store.getState().ui.mode).toBe('view'));
    expect(await screen.findByText('pear')).toBeInTheDocument();
  });

  it('keeps a row with a validation error and shows the reason', async () => {
    const user = userEvent.setup();
    const store = renderApp();
    await screen.findByText('За этот день закупок пока нет');

    await user.click(screen.getByText('Редактирование'));
    await user.click((await screen.findAllByRole('button', { name: /Добавить строку/ }))[0]);
    const date = store.getState().ui.selectedDate;
    await waitFor(() => expect(store.getState().drafts.byDate[date]).toHaveLength(1));

    await user.click(screen.getByRole('button', { name: /Синхронизировать/ }));
    await waitFor(() => expect(store.getState().drafts.byDate[date][0].status).toBe('error'));
    expect(store.getState().drafts.byDate[date][0].error).toContain('Укажите товар');
    // currency was pre-filled with the first one from the backend, so it is not among the problems
    expect(store.getState().drafts.byDate[date][0].currency).toBe('rub');
    expect(store.getState().drafts.byDate[date][0].error).not.toContain('Укажите валюту');
    expect(backend.calls.filter((c) => c.method === 'POST')).toHaveLength(0);
    expect(screen.getByText('1 с ошибкой')).toBeInTheDocument();
    expect(screen.queryByText('Всё готово к отправке')).not.toBeInTheDocument();

    // every required field is highlighted, not only the numeric ones
    const invalidCells = document.querySelectorAll('.cell--invalid');
    expect(invalidCells).toHaveLength(7);
    expect(screen.getByRole('combobox', { name: 'Товар' }).closest('.cell')).toHaveClass('cell--invalid');
    expect(screen.getByRole('combobox', { name: 'Валюта' }).closest('.cell')).not.toHaveClass('cell--invalid');
  });
});
