/**
 * Converts a human-entered Brazilian decimal to the API's exact decimal-string
 * contract. Financial values never pass through JavaScript's Number type.
 */
export function toApiDecimal(value: string): string {
  const compact = value.trim().replaceAll(/\s/g, '');
  if (!compact) throw new Error('Informe um valor.');

  const lastComma = compact.lastIndexOf(',');
  const lastDot = compact.lastIndexOf('.');
  const separatorIndex = Math.max(lastComma, lastDot);
  const integerPart = (separatorIndex === -1 ? compact : compact.slice(0, separatorIndex))
    .replaceAll(/[.,]/g, '');
  const decimalPart = separatorIndex === -1 ? '' : compact.slice(separatorIndex + 1);

  if (!/^\d+$/.test(integerPart) || (decimalPart && !/^\d+$/.test(decimalPart))) {
    throw new Error('Use apenas algarismos, ponto ou vírgula.');
  }

  const normalized = `${integerPart || '0'}${decimalPart ? `.${decimalPart}` : ''}`;
  if (!/^\d{1,18}(?:\.\d{1,8})?$/.test(normalized)) {
    throw new Error('Use até 18 dígitos inteiros e 8 casas decimais.');
  }
  return normalized;
}

/** Converts a percentage typed as 12,5 into the API fraction 0.125 without floats. */
export function percentageToApiFraction(value: string): string {
  const decimal = toApiDecimal(value);
  const [whole, fraction = ''] = decimal.split('.');
  const digits = `${whole}${fraction}`.replace(/^0+(?=\d)/, '') || '0';
  const scale = fraction.length + 2;
  const shifted = digits.length <= scale
    ? `0.${digits.padStart(scale, '0')}`
    : `${digits.slice(0, digits.length - scale)}.${digits.slice(digits.length - scale)}`;
  const normalized = shifted.replace(/0+$/, '').replace(/\.$/, '') || '0';
  if (!/^(?:0|0\.\d{1,8}|1(?:\.0{1,8})?)$/.test(normalized)) {
    throw new Error('A alíquota deve estar entre 0% e 100%.');
  }
  return normalized;
}

export function formatCurrency(value: string): string {
  return `R$ ${formatDecimal(value, 2)}`;
}

export function formatNumber(value: string): string {
  return formatDecimal(value, 4);
}

export function formatPercentage(fraction: string): string {
  const [integer = '0', decimal = ''] = fraction.split('.');
  const digits = `${integer}${decimal}`.replace(/^0+(?=\d)/, '') || '0';
  const scale = decimal.length - 2;
  const percentage = scale <= 0
    ? `${digits}${'0'.repeat(-scale)}`
    : digits.length <= scale
      ? `0.${digits.padStart(scale, '0')}`
      : `${digits.slice(0, digits.length - scale)}.${digits.slice(digits.length - scale)}`;
  return `${formatDecimal(percentage, 2)}%`;
}

function formatDecimal(value: string, maximumFractionDigits: number): string {
  const [integer = '0', fraction = ''] = value.split('.');
  const sign = integer.startsWith('-') ? '-' : '';
  const absoluteInteger = (sign ? integer.slice(1) : integer).replace(/^0+(?=\d)/, '') || '0';
  const grouped = absoluteInteger.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  const visibleFraction = fraction.slice(0, maximumFractionDigits).replace(/0+$/, '');
  return `${sign}${grouped}${visibleFraction ? `,${visibleFraction}` : ''}`;
}
