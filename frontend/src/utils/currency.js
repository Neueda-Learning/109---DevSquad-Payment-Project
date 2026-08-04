// Formatting helpers for money values across currencies.

export const CURRENCIES = [
  {
    country: 'United States',
    currency: 'USD',
    symbol: '$',
  },
  {
    country: 'India',
    currency: 'INR',
    symbol: '₹',
  },
  {
    country: 'United Kingdom',
    currency: 'GBP',
    symbol: '£',
  },
  {
    country: 'European Union',
    currency: 'EUR',
    symbol: '€',
  },
  {
    country: 'Japan',
    currency: 'JPY',
    symbol: '¥',
  },
]

const CURRENCY_LOCALE_MAP = {
  USD: 'en-US',
  EUR: 'de-DE',
  GBP: 'en-GB',
  INR: 'en-IN',
  JPY: 'ja-JP',
  AUD: 'en-AU',
  CAD: 'en-CA',
  SGD: 'en-SG',
}

export function formatCurrency(amount, currencyCode = 'USD') {
  const locale = CURRENCY_LOCALE_MAP[currencyCode] || 'en-US'
  try {
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: currencyCode,
      maximumFractionDigits: currencyCode === 'JPY' ? 0 : 2,
    }).format(amount)
  } catch {
    return `${currencyCode} ${amount.toFixed(2)}`
  }
}

export function convertAmount(amount, fromCode, toCode, rates) {
  if (!rates || !rates[fromCode] || !rates[toCode]) return amount
  const amountInUsd = amount / rates[fromCode]
  return amountInUsd * rates[toCode]
}
