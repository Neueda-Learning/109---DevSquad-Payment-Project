// DEMO DATA — remove this file once the backend currency/rates API is connected.
// Backend should expose GET /api/currencies which returns a similar shape.

export const demoCurrencies = [
  { code: 'USD', symbol: '$', name: 'US Dollar', rateToUSD: 1 },
  { code: 'EUR', symbol: '€', name: 'Euro', rateToUSD: 0.92 },
  { code: 'GBP', symbol: '£', name: 'British Pound', rateToUSD: 0.78 },
  { code: 'INR', symbol: '₹', name: 'Indian Rupee', rateToUSD: 83.1 },
  { code: 'JPY', symbol: '¥', name: 'Japanese Yen', rateToUSD: 151.4 },
  { code: 'AUD', symbol: 'A$', name: 'Australian Dollar', rateToUSD: 1.51 },
  { code: 'CAD', symbol: 'C$', name: 'Canadian Dollar', rateToUSD: 1.36 },
  { code: 'SGD', symbol: 'S$', name: 'Singapore Dollar', rateToUSD: 1.34 },
]

export default demoCurrencies
