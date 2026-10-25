import type { PurchaseOperation } from '@/api/types';
import type { DraftRow } from '@/features/drafts/types';

// Synced purchases and drafts are rendered in one list; this discriminated union keeps them apart
export type TableRow =
  | { kind: 'synced'; key: string; index: number; data: PurchaseOperation }
  | { kind: 'draft'; key: string; index: number; data: DraftRow };

export function buildRows(synced: PurchaseOperation[], drafts: DraftRow[], offset: number): TableRow[] {
  const syncedRows: TableRow[] = synced.map((data, i) => ({ kind: 'synced', key: `s-${data.id}`, index: offset + i + 1, data }));
  const draftRows: TableRow[] = drafts.map((data, i) => ({
    kind: 'draft',
    key: `d-${data.id}`,
    index: offset + synced.length + i + 1,
    data,
  }));
  return [...syncedRows, ...draftRows];
}

export const formatNumber = (value: number, locale: string): string =>
  new Intl.NumberFormat(locale === 'ru' ? 'ru-RU' : 'en-US', { maximumFractionDigits: 3 }).format(value);
