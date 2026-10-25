// Mirrors the backend DTOs (see /v3/api-docs on the Spring side)

export interface NamedRef {
  id: number;
  name: string;
}

export interface PackageRef {
  id: number;
  name: string;
  weight: number;
  weightUnitId: number;
  weightUnitName: string;
}

export interface PurchaseOperation {
  id: number;
  date: string;
  itemId: number;
  itemName: string;
  quantity: number;
  quantityUnitId: number;
  quantityUnitName: string;
  packageId: number;
  packageName: string;
  packageWeight: number;
  packageWeightUnitName: string;
  boughtPrice: number;
  currencyId: number;
  currencyName: string;
  operationTimestamp: number;
}

export interface PurchaseRequest {
  date: string;
  itemId: number;
  quantity: number;
  quantityUnitId: number;
  packageId: number;
  boughtPrice: number;
  currency: number;
}

export interface PurchasesQuery {
  date: string;
  page: number; // zero-based, as in the backend
  pageSize: number;
}

export interface BackendError {
  status?: number;
  error?: string;
  message?: string;
}
