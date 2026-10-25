import { CopyOutlined, DeleteOutlined, LockOutlined } from '@ant-design/icons';
import { Button, Space, Table, Tag, Tooltip, type TableColumnsType } from 'antd';
import { useAppDispatch } from '@/app/hooks';
import { duplicateRow, removeRow } from '@/features/drafts/draftsSlice';
import type { DraftRow } from '@/features/drafts/types';
import { useT } from '@/i18n';
import type { Refs } from '@/lib/matching';
import { useDraftCells } from './DraftRowEditor';
import { formatNumber, type TableRow } from './rowModel';

interface Props {
  rows: TableRow[];
  date: string;
  refs: Refs;
  editing: boolean;
  autoCreate: boolean;
  loading: boolean;
}

export function PurchasesTable({ rows, date, refs, editing, autoCreate, loading }: Props) {
  const dispatch = useAppDispatch();
  const { t, locale } = useT();
  const cells = useDraftCells({ date, refs, autoCreate, disabled: !editing });

  // Text cells (saved rows and drafts in view mode) share the box of an input: same height, same text inset.
  // Saved and draft rows therefore line up with each other, and nothing moves when the mode changes.
  const draftOrText = (render: (row: DraftRow) => React.ReactNode, text: (r: TableRow) => React.ReactNode) => (_: unknown, r: TableRow) =>
    r.kind === 'draft' && editing ? render(r.data) : <div className="cell-text">{text(r)}</div>;

  const draftText = (pick: (d: DraftRow) => string | number | null) => (r: TableRow) =>
    r.kind === 'draft' ? (pick(r.data) ?? '') : '';

  const columns: TableColumnsType<TableRow> = [
    { title: t('col.index'), dataIndex: 'index', width: 40, render: (_, r) => <span style={{ opacity: 0.6 }}>{r.index}</span> },
    {
      title: t('col.item'),
      render: draftOrText(cells.item, (r) => (r.kind === 'synced' ? r.data.itemName : draftText((d) => d.item)(r))),
    },
    {
      title: t('col.quantity'),
      width: 92,
      render: draftOrText(cells.quantity, (r) =>
        r.kind === 'synced' ? formatNumber(r.data.quantity, locale) : r.data.quantity !== null ? formatNumber(r.data.quantity, locale) : '',
      ),
    },
    {
      title: t('col.quantityUnit'),
      width: 66,
      render: draftOrText(cells.quantityUnit, (r) => (r.kind === 'synced' ? r.data.quantityUnitName : draftText((d) => d.quantityUnit)(r))),
    },
    {
      title: t('col.packageName'),
      render: draftOrText(cells.packageName, (r) => (r.kind === 'synced' ? r.data.packageName : draftText((d) => d.packageName)(r))),
    },
    {
      title: t('col.packageWeight'),
      width: 110,
      render: draftOrText(cells.packageWeight, (r) =>
        r.kind === 'synced'
          ? formatNumber(r.data.packageWeight, locale)
          : r.data.packageWeight !== null
            ? formatNumber(r.data.packageWeight, locale)
            : '',
      ),
    },
    {
      title: t('col.packageWeightUnit'),
      width: 96,
      render: draftOrText(cells.packageWeightUnit, (r) =>
        r.kind === 'synced' ? r.data.packageWeightUnitName : draftText((d) => d.packageWeightUnit)(r),
      ),
    },
    {
      title: t('col.price'),
      width: 76,
      render: draftOrText(cells.price, (r) =>
        r.kind === 'synced' ? formatNumber(r.data.boughtPrice, locale) : r.data.price !== null ? formatNumber(r.data.price, locale) : '',
      ),
    },
    {
      title: t('col.currency'),
      width: 90,
      render: draftOrText(cells.currency, (r) => (r.kind === 'synced' ? r.data.currencyName : draftText((d) => d.currency)(r))),
    },
    {
      title: t('col.status'),
      width: 140,
      render: (_, r) => <RowStatus row={r} editing={editing} date={date} />,
    },
  ];

  function RowStatus({ row, editing, date }: { row: TableRow; editing: boolean; date: string }) {
    if (row.kind === 'synced') {
      return (
        <Space size={6}>
          <Tag color="success">{t('row.saved')}</Tag>
          {editing && (
            <Tooltip title={t('row.locked')}>
              <LockOutlined style={{ opacity: 0.45 }} />
            </Tooltip>
          )}
        </Space>
      );
    }
    const d = row.data;
    const tag =
      d.status === 'syncing' ? (
        <Tag color="processing">{t('row.syncing')}</Tag>
      ) : d.status === 'error' ? (
        <Tooltip title={d.error}>
          <Tag color="error">{t('row.error')}</Tag>
        </Tooltip>
      ) : (
        <Tag color="warning">{t('row.draft')}</Tag>
      );
    if (!editing) return tag;
    return (
      <Space size={0}>
        {tag}
        <Tooltip title={t('row.duplicate')}>
          <Button type="text" size="small" icon={<CopyOutlined />} onClick={() => dispatch(duplicateRow({ date, id: d.id }))} disabled={d.status === 'syncing'} />
        </Tooltip>
        <Tooltip title={t('row.remove')}>
          <Button type="text" size="small" danger icon={<DeleteOutlined />} onClick={() => dispatch(removeRow({ date, id: d.id }))} disabled={d.status === 'syncing'} />
        </Tooltip>
      </Space>
    );
  }

  return (
    <Table<TableRow>
      className="purchases-table"
      size="middle"
      rowKey="key"
      columns={columns}
      dataSource={rows}
      loading={loading}
      pagination={false}
      tableLayout="fixed"
      scroll={{ x: 900 }}
      rowClassName={(r) =>
        r.kind === 'synced'
          ? editing
            ? 'row--locked'
            : ''
          : r.data.status === 'error'
            ? 'row--error'
            : r.data.status === 'syncing'
              ? 'row--syncing'
              : ''
      }
    />
  );
}
