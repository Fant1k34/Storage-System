import { AutoComplete, Button, Popover, Select, Tooltip, Typography } from 'antd';
import { useMemo, useState } from 'react';
import type { MessageKey } from '@/i18n';
import { useT } from '@/i18n';
import type { FieldState } from '@/lib/matching';

interface Props {
  value: string;
  options: string[];
  state: FieldState;
  hintKey: MessageKey; // sentence explaining which catalogue misses the value
  hintExtra?: string; // optional second line (used for packages)
  placeholder?: string;
  disabled?: boolean;
  invalid?: boolean; // required value is missing (shown after a failed sync)
  invalidHint?: string;
  onChange: (value: string) => void;
  onSelect?: (value: string) => void; // an option was chosen from the dropdown (not typed)
  onAccept: () => void;
  onPick: (value: string) => void;
}

// Text input with suggestions from the backend catalogue. When the value is unknown the cell turns amber
// and a popover offers two ways out: create the value (primary) or replace it with an existing one.
export function ReferenceCell({ value, options, state, hintKey, hintExtra, placeholder, disabled, invalid, invalidHint, onChange, onSelect, onAccept, onPick }: Props) {
  const { t } = useT();
  const [picking, setPicking] = useState(false);
  const [open, setOpen] = useState(false);

  const acOptions = useMemo(() => options.map((name) => ({ value: name })), [options]);
  const selectOptions = useMemo(() => options.map((name) => ({ value: name, label: name })), [options]);

  const isConflict = state === 'new-conflict' && !disabled;
  const isAccepted = state === 'new-accepted';

  const hint = (
    <div className="hint">
      <div>
        <Typography.Text strong>{t('hint.title')}</Typography.Text>
        <br />
        <Typography.Text>{t(hintKey, { value: value.trim() })}</Typography.Text>
        {hintExtra && (
          <>
            <br />
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              {hintExtra}
            </Typography.Text>
          </>
        )}
      </div>
      {picking ? (
        <Select
          autoFocus
          showSearch
          defaultOpen
          placeholder={t('hint.pickPlaceholder')}
          options={selectOptions}
          style={{ width: '100%' }}
          optionFilterProp="label"
          onChange={(picked: string) => {
            onPick(picked);
            setPicking(false);
            setOpen(false);
          }}
        />
      ) : (
        <div className="hint__actions">
          <Button
            type="primary"
            size="small"
            onClick={() => {
              onAccept();
              setOpen(false);
            }}
          >
            {t('hint.add')}
          </Button>
          <Button size="small" onClick={() => setPicking(true)} disabled={options.length === 0}>
            {t('hint.pick')}
          </Button>
        </div>
      )}
    </div>
  );

  const input = (
    <AutoComplete
      value={value}
      options={acOptions}
      onChange={(next) => onChange(String(next ?? ''))}
      onSelect={(next) => onSelect?.(String(next))}
      filterOption={(text, option) => (option?.value ?? '').toLowerCase().includes(text.toLowerCase())}
      placeholder={placeholder}
      aria-label={placeholder}
      disabled={disabled}
      status={invalid ? 'error' : isConflict ? 'warning' : undefined}
      allowClear
    />
  );

  const cell = (
    <div className={`cell${invalid ? ' cell--invalid' : isConflict ? ' cell--conflict' : ''}${isAccepted ? ' cell--accepted' : ''}`}>
      {/* The Popover always wraps the input: swapping wrappers would remount the input and drop focus mid-typing */}
      <Popover
        content={hint}
        trigger="click"
        open={isConflict && open}
        onOpenChange={(next) => {
          if (!isConflict) return;
          setOpen(next);
          if (!next) setPicking(false);
        }}
        placement="bottomLeft"
        destroyTooltipOnHide
      >
        {input}
      </Popover>
      {isConflict && (
        <Tooltip title={t('hint.title')}>
          <button
            type="button"
            className="cell__dot cell__dot--conflict"
            aria-label={t('hint.title')}
            onClick={() => setOpen(true)}
            style={{ border: 0, padding: 0, cursor: 'pointer' }}
          />
        </Tooltip>
      )}
      {isAccepted && (
        <Tooltip title={t('hint.willCreate')}>
          <span className="cell__dot cell__dot--accepted" />
        </Tooltip>
      )}
    </div>
  );

  return invalid && invalidHint ? <Tooltip title={invalidHint}>{cell}</Tooltip> : cell;
}
