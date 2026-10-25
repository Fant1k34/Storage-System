import { createApi, fetchBaseQuery, type FetchBaseQueryError } from '@reduxjs/toolkit/query/react';
import type { BackendError, NamedRef, PackageRef, PurchaseOperation, PurchaseRequest, PurchasesQuery } from './types';

export const PAGE_SIZE = 50;

// All requests go to /api on the page's own origin and are proxied to Spring (vite dev server or server/index.mjs).
// The origin is spelled out because the Request constructor needs an absolute URL outside the browser (tests).
const API_BASE = typeof window !== 'undefined' ? `${window.location.origin}/api` : '/api';

export const skladApi = createApi({
  reducerPath: 'skladApi',
  baseQuery: fetchBaseQuery({ baseUrl: API_BASE }),
  tagTypes: ['Items', 'Units', 'Currencies', 'Packages', 'Purchases'],
  endpoints: (build) => ({
    getItems: build.query<NamedRef[], void>({
      query: () => '/v1/get-all-items',
      providesTags: ['Items'],
    }),
    getUnits: build.query<NamedRef[], void>({
      query: () => '/v1/get-all-measures',
      providesTags: ['Units'],
    }),
    getCurrencies: build.query<NamedRef[], void>({
      query: () => '/v1/get-all-currencies',
      providesTags: ['Currencies'],
    }),
    getPackages: build.query<PackageRef[], void>({
      query: () => '/v1/get-all-packages',
      providesTags: ['Packages'],
    }),
    getPurchases: build.query<PurchaseOperation[], PurchasesQuery>({
      query: ({ date, page, pageSize }) => ({
        url: '/v1/get-all-purchases',
        params: { date, page, pageSize },
      }),
      providesTags: (_result, _error, { date }) => [{ type: 'Purchases', id: date }],
    }),

    // Mutations do not invalidate tags on their own: the sync thunk creates many records in a row
    // and invalidates everything once at the end.
    addItem: build.mutation<NamedRef, { name: string }>({
      query: (body) => ({ url: '/v1/add-item', method: 'POST', body }),
    }),
    addUnit: build.mutation<NamedRef, { name: string }>({
      query: (body) => ({ url: '/v1/add-measure', method: 'POST', body }),
    }),
    addCurrency: build.mutation<NamedRef, { name: string }>({
      query: (body) => ({ url: '/v1/add-currency', method: 'POST', body }),
    }),
    addPackage: build.mutation<PackageRef, { name: string; weight: number; weightUnitId: number }>({
      query: (body) => ({ url: '/v1/add-package', method: 'POST', body }),
    }),
    addPurchase: build.mutation<boolean, PurchaseRequest>({
      query: (body) => ({ url: '/v1/add-purchase-operation', method: 'POST', body }),
    }),
  }),
});

export const {
  useGetItemsQuery,
  useGetUnitsQuery,
  useGetCurrenciesQuery,
  useGetPackagesQuery,
  useGetPurchasesQuery,
} = skladApi;

// Turns an RTK Query error into a human readable string, preferring the backend's `message`
export function errorMessage(err: unknown): string {
  const e = err as FetchBaseQueryError | { message?: string } | undefined;
  if (!e) return 'Unknown error';
  if ('status' in e) {
    const data = e.data as BackendError | string | undefined;
    if (typeof data === 'string' && data) return data;
    if (data && typeof data === 'object' && data.message) return data.message;
    if (e.status === 'FETCH_ERROR') return 'Network error';
    return `HTTP ${e.status}`;
  }
  return e.message ?? 'Unknown error';
}
