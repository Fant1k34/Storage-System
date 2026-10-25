import { EditOutlined, EyeOutlined, LeftOutlined, PlusOutlined, RightOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Grid, Segmented, Space, Switch, Tooltip, Typography } from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { PAGE_SIZE, errorMessage, useGetPurchasesQuery } from '@/api/skladApi';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { addRow, selectDraftsForDate } from '@/features/drafts/draftsSlice';
import { setAutoCreate, setMode, type Mode } from '@/features/ui/uiSlice';
import { useT } from '@/i18n';
import { EMPTY_REFS, type Refs } from '@/lib/matching';
import { PurchaseCards } from './PurchaseCards';
import { PurchasesTable } from './PurchasesTable';
import { buildRows } from './rowModel';

interface Props {
  refs: Refs | undefined;
  refsError?: unknown;
  onRetryRefs: () => void;
}

export function PurchasesIsland({ refs, refsError, onRetryRefs }: Props) {
  const dispatch = useAppDispatch();
  const { t, tp, locale } = useT();
  const screens = Grid.useBreakpoint();
  // Ten columns need roughly 900px; narrower screens (phones and tablets) get the card layout
  const isMobile = !screens.lg;
  const isPhone = !screens.md;

  const date = useAppSelector((s) => s.ui.selectedDate);
  const mode = useAppSelector((s) => s.ui.mode);
  const autoCreate = useAppSelector((s) => s.ui.autoCreate);
  const drafts = useAppSelector((s) => selectDraftsForDate(s, date));
  const editing = mode === 'edit';

  const [page, setPage] = useState(0);
  useEffect(() => setPage(0), [date]);

  const purchases = useGetPurchasesQuery({ date, page, pageSize: PAGE_SIZE });
  const synced = purchases.data ?? [];
  // The backend has no count endpoint: a full page means there may be a next one
  const hasNext = synced.length === PAGE_SIZE;

  const rows = useMemo(() => buildRows(synced, drafts, page * PAGE_SIZE), [synced, drafts, page]);
  // New rows start with the first currency the backend returned
  const addNewRow = () => dispatch(addRow({ date, template: { currency: refs?.currencies[0]?.name ?? '' } }));
  const effectiveRefs = refs ?? EMPTY_REFS;

  const title = dayjs(date).locale(locale).format(locale === 'ru' ? 'D MMMM, dddd' : 'dddd, MMMM D');
  const recordCount = synced.length + (page > 0 ? page * PAGE_SIZE : 0);

  return (
    <Card className="island" styles={{ body: { padding: isPhone ? 14 : 20 } }}>
      <div className="toolbar" style={{ marginBottom: 14 }}>
        <div>
          <Typography.Title level={4} style={{ margin: 0, textTransform: 'capitalize' }}>
            {title}
          </Typography.Title>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            {purchases.isFetching ? '…' : tp(recordCount, { one: 'island.recordsOne', few: 'island.recordsFew', many: 'island.records' })}
            {drafts.length > 0 && ` · ${tp(drafts.length, { one: 'bar.rowsOne', few: 'bar.rowsFew', many: 'bar.rows' })}`}
          </Typography.Text>
        </div>
        <div className="toolbar__spacer" />
        {/* Always rendered so the toolbar does not reflow; disabled while viewing */}
        <Tooltip title={t('autoCreate.hint')}>
          <Space size={6}>
            <Switch size="small" checked={autoCreate} disabled={!editing} onChange={(v) => dispatch(setAutoCreate(v))} />
            <Typography.Text type={editing ? undefined : 'secondary'}>{t('autoCreate.label')}</Typography.Text>
          </Space>
        </Tooltip>
        <Segmented<Mode>
          value={mode}
          onChange={(v) => dispatch(setMode(v))}
          options={[
            { value: 'view', label: isPhone ? undefined : t('mode.view'), icon: <EyeOutlined /> },
            { value: 'edit', label: isPhone ? undefined : t('mode.edit'), icon: <EditOutlined /> },
          ]}
        />
        <Button type="primary" ghost icon={<PlusOutlined />} onClick={addNewRow} disabled={!refs || !editing}>
          {isPhone ? null : t('row.add')}
        </Button>
      </div>

      {refsError !== undefined && (
        <Alert
          type="error"
          showIcon
          style={{ marginBottom: 12 }}
          message={t('island.refsError', { reason: errorMessage(refsError) })}
          action={
            <Button size="small" onClick={onRetryRefs}>
              {t('island.retry')}
            </Button>
          }
        />
      )}
      {purchases.isError && (
        <Alert
          type="error"
          showIcon
          style={{ marginBottom: 12 }}
          message={t('island.loadError', { reason: errorMessage(purchases.error) })}
          action={
            <Button size="small" onClick={() => purchases.refetch()}>
              {t('island.retry')}
            </Button>
          }
        />
      )}

      {rows.length === 0 && !purchases.isFetching && !purchases.isError ? (
        <div className="empty-state">
          <Typography.Title level={5} style={{ marginBottom: 4 }}>
            {t('island.empty')}
          </Typography.Title>
          {!editing && <Typography.Text type="secondary">{t('island.emptyHint')}</Typography.Text>}
          {editing && (
            <div style={{ marginTop: 12 }}>
              <Button type="primary" icon={<PlusOutlined />} onClick={addNewRow} disabled={!refs}>
                {t('row.add')}
              </Button>
            </div>
          )}
        </div>
      ) : isMobile ? (
        <PurchaseCards rows={rows} date={date} refs={effectiveRefs} editing={editing} autoCreate={autoCreate} />
      ) : (
        <PurchasesTable rows={rows} date={date} refs={effectiveRefs} editing={editing} autoCreate={autoCreate} loading={purchases.isFetching} />
      )}

      {(page > 0 || hasNext) && (
        <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 8, marginTop: 12 }}>
          <Typography.Text type="secondary">{t('pagination.page', { page: page + 1 })}</Typography.Text>
          <Button icon={<LeftOutlined />} disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            {isPhone ? null : t('pagination.prev')}
          </Button>
          <Button icon={<RightOutlined />} disabled={!hasNext} onClick={() => setPage((p) => p + 1)}>
            {isPhone ? null : t('pagination.next')}
          </Button>
        </div>
      )}
    </Card>
  );
}
