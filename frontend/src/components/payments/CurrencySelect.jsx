import { demoCurrencies } from '../../data/demoCurrencies'

/** Currency dropdown used on payment creation/scheduling forms. */
function CurrencySelect({ value, onChange, id = 'currency' }) {
  return (
    <select id={id} className="input" value={value} onChange={(e) => onChange(e.target.value)}>
      {demoCurrencies.map((c) => (
        <option key={c.code} value={c.code}>
          {c.code} — {c.name} ({c.symbol})
        </option>
      ))}
    </select>
  )
}

export default CurrencySelect
