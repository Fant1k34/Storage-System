import { vi } from 'vitest';
import type { NamedRef, PackageRef, PurchaseOperation, PurchaseRequest } from '@/api/types';

// In-memory stand-in for the Spring backend, wired into global fetch
export function installMockBackend() {
  const state = {
    items: [{ id: 1, name: 'apple' }] as NamedRef[],
    units: [
      { id: 1, name: 'kg' },
      { id: 2, name: 'g' },
    ] as NamedRef[],
    currencies: [{ id: 1, name: 'rub' }] as NamedRef[],
    packages: [{ id: 1, name: 'box', weight: 0.5, weightUnitId: 1, weightUnitName: 'kg' }] as PackageRef[],
    purchases: [] as PurchaseOperation[],
    calls: [] as { method: string; path: string; body?: unknown }[],
  };

  const json = (data: unknown, status = 200) =>
    new Response(JSON.stringify(data), { status, headers: { 'Content-Type': 'application/json' } });

  const upsert = (list: NamedRef[], name: string): NamedRef => {
    const found = list.find((e) => e.name === name);
    if (found) return found;
    const created = { id: list.length + 1, name };
    list.push(created);
    return created;
  };

  const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    // RTK Query hands over a prepared Request object, so method and body live on it, not on init
    const request = input instanceof Request ? input : null;
    const url = new URL(typeof input === 'string' ? input : input instanceof URL ? input.href : input.url, 'http://test');
    const method = (request?.method ?? init?.method ?? 'GET').toUpperCase();
    const rawBody = request ? await request.text() : init?.body ? String(init.body) : '';
    const body = rawBody ? JSON.parse(rawBody) : undefined;
    state.calls.push({ method, path: url.pathname, body });
    const path = url.pathname.replace(/^\/api/, '');

    switch (`${method} ${path}`) {
      case 'GET /v1/get-all-items':
        return json(state.items);
      case 'GET /v1/get-all-measures':
        return json(state.units);
      case 'GET /v1/get-all-currencies':
        return json(state.currencies);
      case 'GET /v1/get-all-packages':
        return json(state.packages);
      case 'GET /v1/get-all-purchases': {
        const date = url.searchParams.get('date');
        return json(state.purchases.filter((p) => p.date === date));
      }
      case 'POST /v1/add-item':
        return json(upsert(state.items, body.name));
      case 'POST /v1/add-measure':
        return json(upsert(state.units, body.name));
      case 'POST /v1/add-currency':
        return json(upsert(state.currencies, body.name));
      case 'POST /v1/add-package': {
        const unit = state.units.find((u) => u.id === body.weightUnitId);
        if (!unit) return json({ status: 400, message: 'Measure unit does not exist' }, 400);
        const found = state.packages.find((p) => p.name === body.name && p.weight === body.weight && p.weightUnitId === body.weightUnitId);
        if (found) return json(found);
        const created = { id: state.packages.length + 1, name: body.name, weight: body.weight, weightUnitId: unit.id, weightUnitName: unit.name };
        state.packages.push(created);
        return json(created);
      }
      case 'POST /v1/add-purchase-operation': {
        const req = body as PurchaseRequest;
        if (!(req.quantity > 0)) return json({ status: 400, message: 'Quantity must be positive' }, 400);
        const item = state.items.find((i) => i.id === req.itemId)!;
        const unit = state.units.find((u) => u.id === req.quantityUnitId)!;
        const pack = state.packages.find((p) => p.id === req.packageId)!;
        const cur = state.currencies.find((c) => c.id === req.currency)!;
        state.purchases.push({
          id: state.purchases.length + 1,
          date: req.date,
          itemId: item.id,
          itemName: item.name,
          quantity: req.quantity,
          quantityUnitId: unit.id,
          quantityUnitName: unit.name,
          packageId: pack.id,
          packageName: pack.name,
          packageWeight: pack.weight,
          packageWeightUnitName: pack.weightUnitName,
          boughtPrice: req.boughtPrice,
          currencyId: cur.id,
          currencyName: cur.name,
          operationTimestamp: Date.now(),
        });
        return json(true);
      }
      default:
        return json({ status: 404, message: `No route ${method} ${path}` }, 404);
    }
  });

  vi.stubGlobal('fetch', fetchMock);
  return state;
}
