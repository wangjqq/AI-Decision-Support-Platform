import dayjs from 'dayjs';

/** 格式化日期时间（默认 yyyy-MM-dd HH:mm:ss） */
export const formatDateTime = (value?: string | number | Date | null, pattern = 'YYYY-MM-DD HH:mm:ss'): string => {
  if (value === null || value === undefined || value === '') return '-';
  const d = dayjs(value);
  return d.isValid() ? d.format(pattern) : '-';
};

/** 格式化日期（默认 yyyy-MM-dd） */
export const formatDate = (value?: string | number | Date | null, pattern = 'YYYY-MM-DD'): string => {
  if (value === null || value === undefined || value === '') return '-';
  const d = dayjs(value);
  return d.isValid() ? d.format(pattern) : '-';
};

/** 千分位数字格式化 */
export const formatNumber = (value?: number | string | null): string => {
  if (value === null || value === undefined || value === '') return '-';
  const n = Number(value);
  if (Number.isNaN(n)) return '-';
  return n.toLocaleString('zh-CN');
};

/** 百分比格式化：0.1234 -> "12.34%" */
export const formatPercent = (value?: number | string | null, fractionDigits = 2): string => {
  if (value === null || value === undefined || value === '') return '-';
  const n = Number(value);
  if (Number.isNaN(n)) return '-';
  return `${(n * 100).toFixed(fractionDigits)}%`;
};
