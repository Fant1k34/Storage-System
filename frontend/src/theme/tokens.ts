import { theme as antdTheme, type ThemeConfig } from 'antd';

// "Floating islands": a calm tinted canvas with white (or deep navy) cards lifted by a soft, wide shadow.
export const ISLAND_RADIUS = 20;

export function buildTheme(dark: boolean): ThemeConfig {
  return {
    algorithm: dark ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm,
    token: {
      colorPrimary: '#3D5AFE',
      colorInfo: '#3D5AFE',
      colorSuccess: '#1DB954',
      colorWarning: '#F5A524',
      colorError: '#E5484D',
      colorBgLayout: dark ? '#0E1320' : '#F2F4F8',
      colorBgContainer: dark ? '#161C2C' : '#FFFFFF',
      colorBgElevated: dark ? '#1B2234' : '#FFFFFF',
      colorBorderSecondary: dark ? '#242C40' : '#EDF0F5',
      colorText: dark ? '#E6E9F2' : '#121826',
      colorTextSecondary: dark ? '#9AA3B8' : '#5B6478',
      borderRadius: 12,
      borderRadiusLG: ISLAND_RADIUS,
      fontFamily:
        "-apple-system, BlinkMacSystemFont, 'Inter', 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
      fontSize: 14,
      boxShadowSecondary: dark ? '0 12px 40px rgba(0, 0, 0, 0.45)' : '0 12px 40px rgba(18, 24, 38, 0.08)',
      controlHeight: 36,
    },
    components: {
      Card: {
        paddingLG: 20,
      },
      Table: {
        headerBg: 'transparent',
        headerColor: dark ? '#9AA3B8' : '#5B6478',
        borderColor: dark ? '#242C40' : '#EDF0F5',
        rowHoverBg: dark ? '#1B2234' : '#F7F8FB',
        cellPaddingBlock: 10,
        cellPaddingInline: 8,
      },
      Segmented: {
        itemSelectedBg: dark ? '#1B2234' : '#FFFFFF',
        trackBg: dark ? '#0E1320' : '#E9ECF2',
      },
      Button: {
        fontWeight: 600,
      },
    },
  };
}
