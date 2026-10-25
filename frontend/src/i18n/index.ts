import { useCallback } from 'react';
import { useAppSelector } from '@/app/hooks';
import type { Locale } from '@/features/ui/uiSlice';
import { en, ru, type MessageKey } from './messages';

const dictionaries: Record<Locale, Record<MessageKey, string>> = { ru, en };

export type { MessageKey };

export function translate(locale: Locale, key: MessageKey, vars?: Record<string, string | number>): string {
  const template = dictionaries[locale][key] ?? dictionaries.ru[key] ?? key;
  if (!vars) return template;
  return template.replace(/\{(\w+)\}/g, (_match, name: string) => String(vars[name] ?? `{${name}}`));
}

// Russian has three plural forms; English two. Picks the right key suffix for a count.
export function plural(locale: Locale, count: number, keys: { one: MessageKey; few: MessageKey; many: MessageKey }): MessageKey {
  if (locale === 'en') return count === 1 ? keys.one : keys.many;
  const mod10 = count % 10;
  const mod100 = count % 100;
  if (mod10 === 1 && mod100 !== 11) return keys.one;
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return keys.few;
  return keys.many;
}

export function useT() {
  const locale = useAppSelector((s) => s.ui.locale);
  const t = useCallback(
    (key: MessageKey, vars?: Record<string, string | number>) => translate(locale, key, vars),
    [locale],
  );
  const tp = useCallback(
    (count: number, keys: { one: MessageKey; few: MessageKey; many: MessageKey }) =>
      translate(locale, plural(locale, count, keys), { count }),
    [locale],
  );
  return { t, tp, locale };
}
