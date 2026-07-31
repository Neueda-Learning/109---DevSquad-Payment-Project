import { useState } from 'react'
import { downloadReceipt } from '../../api/paymentApi'
import './ReceiptDownloadButton.css'

/**
 * Button that requests the invoice/receipt for a completed payment.
 * Currently calls the demo API stub — will return a real file blob once the
 * backend route (GET /api/payments/:id/receipt) is connected.
 */
function ReceiptDownloadButton({ paymentId, disabled }) {
  const [isDownloading, setIsDownloading] = useState(false)
  const [notice, setNotice] = useState('')

  const handleDownload = async () => {
    setIsDownloading(true)
    setNotice('')
    try {
      const result = await downloadReceipt(paymentId)
      setNotice(result.message)
    } finally {
      setIsDownloading(false)
    }
  }

  return (
    <div className="receipt-download">
      <button
        type="button"
        className="btn btn-secondary"
        onClick={handleDownload}
        disabled={disabled || isDownloading}
      >
        {isDownloading ? 'Preparing…' : 'Download invoice / receipt'}
      </button>
      {notice && <p className="receipt-download-notice">{notice}</p>}
    </div>
  )
}

export default ReceiptDownloadButton
