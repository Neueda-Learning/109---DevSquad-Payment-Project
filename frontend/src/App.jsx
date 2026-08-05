import { useEffect, useState } from 'react'
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
import './styles/app.css'

function App() {
  const [users, setUsers] = useState([])
  const [selectedUser, setSelectedUser] = useState(null)

  const apiUrl = import.meta.env.VITE_API_BASE_URL

  useEffect(() => {
    fetch(`${apiUrl}/api/1.0/users/all`)
      .then((res) => res.json())
      .then((data) => {
        setUsers(data)

        const defaultUser =
          data.find((u) => u.userId === 1) || data[0]

        setSelectedUser(defaultUser)
      })
      .catch((err) => {
        console.error('Failed to load users:', err)
      })
  }, [])

  return (
    <>
      <Navbar
        users={users}
        selectedUser={selectedUser}
        setSelectedUser={setSelectedUser}
      />

      <main className="app-main">
        <Routes>
          <Route
            path="/"
            element={
              <HomePage
                selectedUser={selectedUser}
              />
            }
          />

          <Route
            path="/payments"
            element={
              <PaymentsHistoryPage
                selectedUser={selectedUser}
              />
            }
          />

          <Route
            path="/payments/new"
            element={
              <NewPaymentPage defaultTiming="now" selectedUser={selectedUser} />
            }
          />

          <Route
            path="/payments/:id"
            element={<PaymentDetailsPage />}
          />

          <Route
            path="/scheduled"
            element={<ScheduledPaymentsPage />}
          />

          <Route
            path="/scheduled/new"
            element={
              <NewPaymentPage defaultTiming="schedule" selectedUser={selectedUser} />
            }
          />

          <Route
            path="*"
            element={<NotFoundPage />}
          />
        </Routes>
      </main>
    </>
  )
}

export default App