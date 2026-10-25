import { DesktopOutlined, MoonOutlined, SunOutlined } from '@ant-design/icons';
import { Card, Segmented, Space, Tooltip, Typography } from 'antd';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { setLocale, setThemeMode, type Locale, type ThemeMode } from '@/features/ui/uiSlice';
import { useT } from '@/i18n';

export function AppHeader() {
  const dispatch = useAppDispatch();
  const { t } = useT();
  const themeMode = useAppSelector((s) => s.ui.themeMode);
  const locale = useAppSelector((s) => s.ui.locale);

  return (
    <Card className="island" styles={{ body: { padding: '14px 20px' } }}>
      <div className="header-island">
        <div className="brand">
          <div className="brand__logo" aria-hidden>
            С
          </div>
          <div>
            <Typography.Title level={4} style={{ margin: 0, lineHeight: 1.1 }}>
              {t('app.title')}
            </Typography.Title>
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              {t('app.subtitle')}
            </Typography.Text>
          </div>
        </div>
        <Space size={8} wrap>
          <Segmented<Locale>
            value={locale}
            onChange={(value) => dispatch(setLocale(value))}
            options={[
              { label: 'RU', value: 'ru' },
              { label: 'EN', value: 'en' },
            ]}
          />
          <Segmented<ThemeMode>
            value={themeMode}
            onChange={(value) => dispatch(setThemeMode(value))}
            options={[
              { value: 'system', icon: <Tooltip title={t('theme.system')}><DesktopOutlined /></Tooltip> },
              { value: 'light', icon: <Tooltip title={t('theme.light')}><SunOutlined /></Tooltip> },
              { value: 'dark', icon: <Tooltip title={t('theme.dark')}><MoonOutlined /></Tooltip> },
            ]}
          />
        </Space>
      </div>
    </Card>
  );
}
