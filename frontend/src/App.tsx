import { App as AntApp, ConfigProvider, Layout, theme as antdTheme } from 'antd';
import enUS from 'antd/locale/en_US';
import ruRU from 'antd/locale/ru_RU';
import dayjs from 'dayjs';
import 'dayjs/locale/ru';
import 'dayjs/locale/en';
import { useEffect, useMemo } from 'react';
import { useGetCurrenciesQuery, useGetItemsQuery, useGetPackagesQuery, useGetUnitsQuery } from '@/api/skladApi';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { clearDate, selectDraftsForDate } from '@/features/drafts/draftsSlice';
import { syncDate } from '@/features/drafts/syncThunk';
import { setMode } from '@/features/ui/uiSlice';
import { useT } from '@/i18n';
import { countConflicts, validateRow, type Refs } from '@/lib/matching';
import { buildTheme } from '@/theme/tokens';
import { AppHeader } from './components/AppHeader';
import { BottomBar } from './components/BottomBar';
import { DateStrip } from './components/DateStrip';
import { PurchasesIsland } from './components/PurchasesIsland';

function useSystemDark(): boolean {
  const query = useMemo(() => window.matchMedia?.('(prefers-color-scheme: dark)'), []);
  const [dark, setDark] = useStateSafe(query?.matches ?? false);
  useEffect(() => {
    if (!query) return;
    const handler = (e: MediaQueryListEvent) => setDark(e.matches);
    query.addEventListener('change', handler);
    return () => query.removeEventListener('change', handler);
  }, [query, setDark]);
  return dark;
}

// Small wrapper so the hook above reads naturally
import { useState as useStateSafe } from 'react';

function Shell() {
  const dispatch = useAppDispatch();
  const { t, locale } = useT();
  const { message } = AntApp.useApp();
  const date = useAppSelector((s) => s.ui.selectedDate);
  const mode = useAppSelector((s) => s.ui.mode);
  const autoCreate = useAppSelector((s) => s.ui.autoCreate);
  const drafts = useAppSelector((s) => selectDraftsForDate(s, date));

  const items = useGetItemsQuery();
  const units = useGetUnitsQuery();
  const currencies = useGetCurrenciesQuery();
  const packages = useGetPackagesQuery();

  const refs: Refs | undefined =
    items.data && units.data && currencies.data && packages.data
      ? { items: items.data, units: units.data, currencies: currencies.data, packages: packages.data }
      : undefined;
  const refsError = items.error ?? units.error ?? currencies.error ?? packages.error;
  const retryRefs = () => {
    items.refetch();
    units.refetch();
    currencies.refetch();
    packages.refetch();
  };

  const conflicts = refs ? countConflicts(drafts, refs, autoCreate) : drafts.length;
  const errors = drafts.filter((d) => d.status === 'error').length;
  const incomplete = drafts.filter((d) => validateRow(d).length > 0).length;
  const syncing = drafts.some((d) => d.status === 'syncing');
  const editing = mode === 'edit';

  useEffect(() => {
    dayjs.locale(locale);
  }, [locale]);

  const onSync = async () => {
    const result = await dispatch(syncDate({ date })).unwrap();
    if (result.saved === 0 && result.failed === 0) message.info(t('sync.nothing'));
    else if (result.failed === 0) message.success(t('sync.done', { saved: result.saved }));
    else message.warning(t('sync.partial', { saved: result.saved, failed: result.failed }));
    if (result.failed === 0) dispatch(setMode('view'));
  };

  const onCancel = () => {
    dispatch(clearDate({ date }));
    dispatch(setMode('view'));
  };

  return (
    <Layout style={{ minHeight: '100vh', background: 'transparent' }}>
      <div className={`page${editing ? ' page--with-bar' : ''}`}>
        <AppHeader />
        <DateStrip />
        <PurchasesIsland refs={refs} refsError={refsError} onRetryRefs={retryRefs} />
      </div>
      {editing && <BottomBar rows={drafts.length} conflicts={conflicts} errors={errors} incomplete={incomplete} syncing={syncing} onSync={onSync} onCancel={onCancel} />}
    </Layout>
  );
}

export default function App() {
  const themeMode = useAppSelector((s) => s.ui.themeMode);
  const locale = useAppSelector((s) => s.ui.locale);
  const systemDark = useSystemDark();
  const dark = themeMode === 'dark' || (themeMode === 'system' && systemDark);

  useEffect(() => {
    document.documentElement.dataset.theme = dark ? 'dark' : 'light';
    document.documentElement.lang = locale;
  }, [dark, locale]);

  const themeConfig = useMemo(() => buildTheme(dark), [dark]);

  return (
    <ConfigProvider theme={themeConfig} locale={locale === 'ru' ? ruRU : enUS}>
      <ThemeVars />
      <AntApp>
        <Shell />
      </AntApp>
    </ConfigProvider>
  );
}

// Exposes a few antd tokens as CSS variables for the hand-written styles (chips, dots)
function ThemeVars() {
  const { token } = antdTheme.useToken();
  useEffect(() => {
    const root = document.documentElement.style;
    root.setProperty('--ant-color-primary', token.colorPrimary);
    root.setProperty('--chip-hover', token.colorFillSecondary);
    document.body.style.background = token.colorBgLayout;
    document.body.style.color = token.colorText;
  }, [token]);
  return null;
}
