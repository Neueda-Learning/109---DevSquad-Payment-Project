import './Spinner.css'

/** Wallet-style loading animation used while demo API calls resolve. */
function Spinner() {
  const text = 'Loading'

  return (
    <div className="spinner-wrap">
      <div className="wallet-loader">
        <div className="wallet-back" />
        <div className="bill bill-1" />
        <div className="bill bill-2" />
        <div className="bill bill-3" />
        <div className="wallet-front">
          <div className="text">
            {text}
            <span className="dot">.</span>
            <span className="dot">.</span>
            <span className="dot">.</span>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Spinner
