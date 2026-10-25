import { CopyOutlined, DeleteOutlined, LockOutlined } from '@ant-design/icons';
import { Button, Card, Space, Tag, Tooltip, Typography } from 'antd';
import { useAppDispatch } from '@/app/hooks';
import { duplicateRow, removeRow } from '@/features/drafts/draftsSlice';
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
}

// Mobile layout: every row becomes its own small island with the fields laid out in two columns
export function PurchaseCards({ rows, date, refs, editing, autoCreate }: Props) {
  const dispatch = useAppDispatch();
  const { t, locale } = useT();
  const cells = useDraftCells({ date, refs, autoCreate, disabled: !editing });

  const Field = ({ label, children, full }: { label: string; children: React.ReactNode; full?: boolean }) => (
    <div className={full ? 'card-grid__full' : undefined}>
      <span className="field-label">{label}</span>
      {children}
    </div>
  );

  return (
    <div className="cards">
      {rows.map((row) => {
        if (row.kind === 'synced') {
          const d = row.data;
          return (
            <Card key={row.key} className="island" size="small" styles={{ body: { padding: 14 } }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', gap: 8 }}>
                <Typography.Text strong style={{ fontSize: 15 }}>
                  {d.itemName}
                </Typography.Text>
                <Space size={6}>
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    #{row.index}
                  </Typography.Text>
                  <Tag color="success">{t('row.saved')}</Tag>
                  {editing && <LockOutlined style={{ opacity: 0.45 }} />}
                </Space>
              </div>
              <Typography.Paragraph style={{ margin: '6px 0 0' }} type="secondary">
                {formatNumber(d.quantity, locale)} {d.quantityUnitName} · {d.packageName} {formatNumber(d.packageWeight, locale)}{' '}
                {d.packageWeightUnitName}
              </Typography.Paragraph>
              <Typography.Text strong>
                {formatNumber(d.boughtPrice, locale)} {d.currencyName}
              </Typography.Text>
            </Card>
          );
        }

        const d = row.data;
        const tag =
          d.status === 'syncing' ? (
            <Tag color="processing">{t('row.syncing')}</Tag>
          ) : d.status === 'error' ? (
            <Tag color="error">{t('row.error')}</Tag>
          ) : (
            <Tag color="warning">{t('row.draft')}</Tag>
          );

        return (
          <Card
            key={row.key}
            className="island"
            size="small"
            styles={{ body: { padding: 14, background: d.status === 'error' ? 'var(--error-soft)' : undefined } }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
              <Space size={6}>
                <Typography.Text type="secondary">#{row.index}</Typography.Text>
                {tag}
              </Space>
              {editing && (
                <Space size={0}>
                  <Tooltip title={t('row.duplicate')}>
                    <Button type="text" size="small" icon={<CopyOutlined />} onClick={() => dispatch(duplicateRow({ date, id: d.id }))} />
                  </Tooltip>
                  <Tooltip title={t('row.remove')}>
                    <Button type="text" size="small" danger icon={<DeleteOutlined />} onClick={() => dispatch(removeRow({ date, id: d.id }))} />
                  </Tooltip>
                </Space>
              )}
            </div>
            {d.error && (
              <Typography.Text type="danger" style={{ display: 'block', marginBottom: 8, fontSize: 12 }}>
                {d.error}
              </Typography.Text>
            )}
            <div className="card-grid">
              <Field label={t('col.item')} full>
                {cells.item(d)}
              </Field>
              <Field label={t('col.quantity')}>{cells.quantity(d)}</Field>
              <Field label={t('col.quantityUnit')}>{cells.quantityUnit(d)}</Field>
              <Field label={t('col.packageName')} full>
                {cells.packageName(d)}
              </Field>
              <Field label={t('col.packageWeight')}>{cells.packageWeight(d)}</Field>
              <Field label={t('col.packageWeightUnit')}>{cells.packageWeightUnit(d)}</Field>
              <Field label={t('col.price')}>{cells.price(d)}</Field>
              <Field label={t('col.currency')} full>
                {cells.currency(d)}
              </Field>
            </div>
          </Card>
        );
      })}
    </div>
  );
}
