import './ScheduledPaymentsTabs.css'

function ScheduledPaymentsTabs({ activeTab, onChange }) {
  return (
    <div className="scheduled-tabs" role="tablist" aria-label="Scheduled payment categories">
      <button
        type="button"
        role="tab"
        aria-selected={activeTab === 'normal'}
        className={`scheduled-tab ${activeTab === 'normal' ? 'scheduled-tab-active' : ''}`}
        onClick={() => onChange('normal')}
      >
        Normal Scheduled Payments
      </button>
      <button
        type="button"
        role="tab"
        aria-selected={activeTab === 'batch'}
        className={`scheduled-tab ${activeTab === 'batch' ? 'scheduled-tab-active' : ''}`}
        onClick={() => onChange('batch')}
      >
        Batch Scheduled Payments
      </button>
    </div>
  )
}

export default ScheduledPaymentsTabs

