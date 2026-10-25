import { CheckCircleFilled, CloudUploadOutlined, ExclamationCircleFilled } from '@ant-design/icons';
import { Button, Card, Popconfirm, Space, Typography } from 'antd';
import { useT } from '@/i18n';

interface Props {
  rows: number;
  conflicts: number;
  errors: number; // rows rejected by the last sync
  incomplete: number; // rows that would fail validation right now
  syncing: boolean;
  onSync: () => void;
  onCancel: () => void;
}

// Sticky action island shown in edit mode. Sync is the single accent action and stays disabled
// while any new value is still waiting for a decision.
export function BottomBar({ rows, conflicts, errors, incomplete, syncing, onSync, onCancel }: Props) {
  const { t, tp } = useT();
  const ready = rows > 0 && conflicts === 0;

  return (
    <div className="bottom-bar">
      <Card className="island bottom-bar__inner" styles={{ body: { padding: 0, display: 'contents' } }}>
        <div className="bottom-bar__summary">
          <Typography.Text strong>{tp(rows, { one: 'bar.rowsOne', few: 'bar.rowsFew', many: 'bar.rows' })}</Typography.Text>
          {conflicts > 0 ? (
            <Typography.Text type="warning">
              <ExclamationCircleFilled /> {tp(conflicts, { one: 'bar.conflictsOne', few: 'bar.conflicts', many: 'bar.conflicts' })}
            </Typography.Text>
          ) : errors > 0 ? (
            <Typography.Text type="danger">
              <ExclamationCircleFilled /> {t('bar.errors', { count: errors })}
            </Typography.Text>
          ) : incomplete > 0 ? (
            <Typography.Text type="secondary">{tp(incomplete, { one: 'bar.incompleteOne', few: 'bar.incompleteFew', many: 'bar.incomplete' })}</Typography.Text>
          ) : rows > 0 ? (
            <Typography.Text type="success">
              <CheckCircleFilled /> {t('bar.ready')}
            </Typography.Text>
          ) : null}
        </div>
        <Space className="bottom-bar__actions" size={8}>
          {rows > 0 ? (
            <Popconfirm
              title={t('bar.cancelConfirm')}
              okText={t('bar.cancelYes')}
              cancelText={t('bar.cancelNo')}
              okButtonProps={{ danger: true }}
              onConfirm={onCancel}
              placement="topRight"
            >
              <Button size="large" disabled={syncing}>
                {t('bar.cancel')}
              </Button>
            </Popconfirm>
          ) : (
            <Button size="large" onClick={onCancel}>
              {t('bar.cancel')}
            </Button>
          )}
          <Button type="primary" size="large" icon={<CloudUploadOutlined />} disabled={!ready} loading={syncing} onClick={onSync}>
            {t('bar.sync')}
          </Button>
        </Space>
      </Card>
    </div>
  );
}
