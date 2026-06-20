/**
 * Format a date to the given format string.
 * Supports: YYYY, MM, DD, HH, mm, ss
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * Get the start of the week (Monday) for a given date.
 */
export function getWeekStart(date = new Date()) {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  d.setDate(diff)
  d.setHours(0, 0, 0, 0)
  return d
}

/**
 * Get the end of the week (Sunday) for a given date.
 */
export function getWeekEnd(date = new Date()) {
  const d = getWeekStart(date)
  d.setDate(d.getDate() + 6)
  d.setHours(23, 59, 59, 999)
  return d
}

/**
 * Get the start of the month for a given date.
 */
export function getMonthStart(date = new Date()) {
  return new Date(date.getFullYear(), date.getMonth(), 1)
}

/**
 * Get the end of the month for a given date.
 */
export function getMonthEnd(date = new Date()) {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59, 999)
}

/**
 * Get a date string formatted as YYYY-MM-DD.
 */
export function toDateString(date) {
  return formatDate(date, 'YYYY-MM-DD')
}

/**
 * Get a datetime string formatted as YYYY-MM-DD HH:mm:ss.
 */
export function toDateTimeString(date) {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}
