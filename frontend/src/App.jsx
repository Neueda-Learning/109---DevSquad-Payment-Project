import { Routes, Route } from 'react-router-dom'
import Navbar from './components/layout/Navbar'
import HomePage from './pages/HomePage'
import PaymentsHistoryPage from './pages/PaymentsHistoryPage'
import PaymentDetailsPage from './pages/PaymentDetailsPage'
import NewPaymentPage from './pages/NewPaymentPage'
import ScheduledPaymentsPage from './pages/ScheduledPaymentsPage'
import NotFoundPage from './pages/NotFoundPage'
import './styles/layout.css'
import './styles/buttons.css'

function App() {
  return (
    <>
      <Navbar />
      <main className="app-main">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/payments" element={<PaymentsHistoryPage />} />
          <Route path="/payments/new" element={<NewPaymentPage defaultTiming="now" />} />
          <Route path="/payments/:id" element={<PaymentDetailsPage />} />
          <Route path="/scheduled" element={<ScheduledPaymentsPage />} />
          <Route path="/scheduled/new" element={<NewPaymentPage defaultTiming="schedule" />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </>
  )
}

export default App
