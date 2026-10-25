export type RefField = 'item' | 'quantityUnit' | 'packageWeightUnit' | 'currency' | 'package';

export type RowStatus = 'draft' | 'syncing' | 'error';

export interface DraftRow {
  id: string;
  item: string;
  quantity: number | null;
  quantityUnit: string;
  packageName: string;
  packageWeight: number | null;
  packageWeightUnit: string;
  price: number | null;
  currency: string;
  // For each reference field, the exact value the user agreed to create as a new record.
  // If the cell value changes afterwards, the agreement no longer applies.
  accepted: Partial<Record<RefField, string>>;
  status: RowStatus;
  error?: string;
}

export const NUMBER_FIELDS = ['quantity', 'packageWeight', 'price'] as const;
export type NumberField = (typeof NUMBER_FIELDS)[number];
