// DEMO DATA — remove this file once the backend scheduled-payments API is connected.
// Backend should expose GET /api/payments/scheduled returning a list shaped like this.

export const RECURRENCE_OPTIONS = [
  { value: 'none', label: 'Does not repeat' },
  { value: 'weekly', label: 'Weekly' },
  { value: 'monthly', label: 'Monthly' },
  { value: 'quarterly', label: 'Quarterly' },
]

export const demoScheduledPayments = [
  {
    id: 'sch_2001',
    vendor: 'Horizon Capital Bank',
    tags: ['Loan'],
    description: 'Monthly loan installment',
    amount: 3200,
    currency: 'USD',
    scheduledFor: '2026-08-24T09:00:00Z',
    recurrence: 'monthly',
    status: 'scheduled',
  },
  {
    id: 'sch_2002',
    vendor: 'CityPower Utilities',
    tags: ['Utility'],
    description: 'Electricity bill - August',
    amount: 198.4,
    currency: 'GBP',
    scheduledFor: '2026-08-05T11:00:00Z',
    recurrence: 'monthly',
    status: 'scheduled',
  },
  {
    id: 'sch_2003',
    vendor: 'NimbusCloud Hosting',
    tags: ['Subscription', 'Vendor'],
    description: 'Cloud infrastructure - August',
    amount: 640.2,
    currency: 'SGD',
    scheduledFor: '2026-08-10T04:15:00Z',
    recurrence: 'monthly',
    status: 'paused',
  },
]

export default demoScheduledPayments
