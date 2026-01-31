/**
 * Format ISO 8601 timestamp to locale string
 * @param isoString - ISO 8601 timestamp string (e.g., "2024-01-31T09:09:15.475Z")
 * @returns Formatted date string (e.g., "01/31 09:09")
 */
export function formatDateTime(isoString: string): string {
  try {
    const date = new Date(isoString);
    return date.toLocaleString('zh-TW', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
  } catch {
    return '';
  }
}

/**
 * Format ISO 8601 timestamp to relative time (e.g., "2 hours ago")
 * @param isoString - ISO 8601 timestamp string
 * @returns Relative time string
 */
export function formatRelativeTime(isoString: string): string {
  try {
    const date = new Date(isoString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return '剛剛';
    if (diffMins < 60) return `${diffMins} 分鐘前`;
    if (diffHours < 24) return `${diffHours} 小時前`;
    if (diffDays < 7) return `${diffDays} 天前`;

    return formatDateTime(isoString);
  } catch {
    return '';
  }
}
