import { CalendarOutlined, LeftOutlined, RightOutlined } from '@ant-design/icons';
import { Button, Card, DatePicker, Grid, Tooltip } from 'antd';
import dayjs, { type Dayjs } from 'dayjs';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { selectDraftCounts } from '@/features/drafts/draftsSlice';
import { setSelectedDate, today } from '@/features/ui/uiSlice';
import { useT } from '@/i18n';

const DAYS_BACK = 60;
const DAYS_FORWARD = 14;

// Horizontal ribbon of days, like an airline date picker. Today is marked with a dot,
// days that have unsynced drafts carry an amber badge with their count.
export function DateStrip() {
  const dispatch = useAppDispatch();
  const { t, locale } = useT();
  const selected = useAppSelector((s) => s.ui.selectedDate);
  const draftCounts = useAppSelector(selectDraftCounts);
  const isMobile = !Grid.useBreakpoint().md;

  const [range, setRange] = useState(() => ({
    start: dayjs().subtract(DAYS_BACK, 'day'),
    end: dayjs().add(DAYS_FORWARD, 'day'),
  }));
  const scrollerRef = useRef<HTMLDivElement>(null);

  // Keep the selected day inside the rendered window (jumping via the calendar can leave it)
  useEffect(() => {
    const day = dayjs(selected);
    setRange((r) => {
      if (day.isBefore(r.start)) return { start: day.subtract(7, 'day'), end: r.end };
      if (day.isAfter(r.end)) return { start: r.start, end: day.add(7, 'day') };
      return r;
    });
  }, [selected]);

  const days = useMemo(() => {
    const list: Dayjs[] = [];
    for (let d = range.start; !d.isAfter(range.end); d = d.add(1, 'day')) list.push(d);
    return list;
  }, [range]);

  useEffect(() => {
    const node = scrollerRef.current?.querySelector<HTMLElement>(`[data-date="${selected}"]`);
    node?.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });
  }, [selected, days.length]);

  const scrollBy = (direction: -1 | 1) => {
    const el = scrollerRef.current;
    if (el) el.scrollBy({ left: direction * el.clientWidth * 0.7, behavior: 'smooth' });
  };

  const todayKey = today();

  const scroller = (
    <div className="date-strip__scroller" ref={scrollerRef} role="listbox" aria-label={t('date.pick')}>
      {days.map((day) => {
        const key = day.format('YYYY-MM-DD');
        const isSelected = key === selected;
        const isToday = key === todayKey;
        const count = draftCounts[key];
        const showMonth = day.date() === 1 || isSelected;
        return (
          <button
            type="button"
            key={key}
            data-date={key}
            role="option"
            aria-selected={isSelected}
            className={`day-chip${isSelected ? ' day-chip--selected' : ''}`}
            onClick={() => dispatch(setSelectedDate(key))}
            title={day.locale(locale).format('D MMMM YYYY, dddd')}
          >
            <span className="day-chip__weekday">{day.locale(locale).format('dd')}</span>
            <span className="day-chip__day">{day.format('D')}</span>
            <span className="day-chip__month">{showMonth ? day.locale(locale).format('MMM') : '\u00a0'}</span>
            {isToday && !isSelected && <span className="day-chip__today-dot" />}
            {count ? (
              <span className="day-chip__badge" title={t('date.drafts', { count })}>
                {count}
              </span>
            ) : null}
          </button>
        );
      })}
    </div>
  );

  const todayButton = (
    <Button onClick={() => dispatch(setSelectedDate(todayKey))} disabled={selected === todayKey}>
      {t('date.today')}
    </Button>
  );
  const picker = (
    <DatePicker
      value={dayjs(selected)}
      onChange={(value) => value && dispatch(setSelectedDate(value.format('YYYY-MM-DD')))}
      allowClear={false}
      suffixIcon={<CalendarOutlined />}
      format="DD.MM.YYYY"
      style={{ width: 132 }}
      aria-label={t('date.pick')}
    />
  );

  if (isMobile) {
    // Touch devices scroll the ribbon with a swipe; arrows would only steal width
    return (
      <Card className="island" styles={{ body: { padding: '10px 12px' } }}>
        <div className="date-strip date-strip--mobile">
          {scroller}
          <div className="date-strip__controls">
            {todayButton}
            {picker}
          </div>
        </div>
      </Card>
    );
  }

  return (
    <Card className="island" styles={{ body: { padding: '10px 12px' } }}>
      <div className="date-strip">
        <Tooltip title={t('date.prev')}>
          <Button shape="circle" icon={<LeftOutlined />} onClick={() => scrollBy(-1)} aria-label={t('date.prev')} />
        </Tooltip>
        {scroller}
        <Tooltip title={t('date.next')}>
          <Button shape="circle" icon={<RightOutlined />} onClick={() => scrollBy(1)} aria-label={t('date.next')} />
        </Tooltip>
        {todayButton}
        {picker}
      </div>
    </Card>
  );
}
