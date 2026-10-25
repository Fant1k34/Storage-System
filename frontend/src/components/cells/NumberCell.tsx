import { InputNumber, Tooltip } from 'antd';

interface Props {
  value: number | null;
  onChange: (value: number | null) => void;
  integer?: boolean;
  placeholder?: string;
  label?: string;
  disabled?: boolean;
  invalid?: boolean;
  invalidHint?: string;
}

export function NumberCell({ value, onChange, integer, placeholder, label, disabled, invalid, invalidHint }: Props) {
  const cell = (
    <div className={`cell${invalid ? ' cell--invalid' : ''}`}>
      <InputNumber
        value={value}
        onChange={(next) => onChange(next === null || next === undefined ? null : Number(next))}
        min={0}
        precision={integer ? 0 : undefined}
        step={integer ? 1 : 0.1}
        placeholder={placeholder}
        aria-label={label}
        disabled={disabled}
        status={invalid ? 'error' : undefined}
        controls={false}
        inputMode="decimal"
        style={{ width: '100%' }}
      />
    </div>
  );

  return invalid && invalidHint ? <Tooltip title={invalidHint}>{cell}</Tooltip> : cell;
}
