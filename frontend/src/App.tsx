import { Navigate, Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import CataloguePage from "./pages/CataloguePage";
import ItemPage from "./pages/ItemPage";
import CreateItemPage from "./pages/CreateItemPage";
import PaymentPage from "./pages/PaymentPage";
import ReceiptPage from "./pages/ReceiptPage";
import AdminPage from "./pages/AdminPage";

export default function App() {
  return (
    <>
      <Navbar />
      <div className="container">
        <Routes>
          <Route path="/" element={<Navigate to="/catalogue" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />

          <Route
            path="/catalogue"
            element={
              <ProtectedRoute>
                <CataloguePage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/items/:itemId"
            element={
              <ProtectedRoute>
                <ItemPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/create-item"
            element={
              <ProtectedRoute>
                <CreateItemPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/payments/:itemId"
            element={
              <ProtectedRoute>
                <PaymentPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/receipts/:receiptId"
            element={
              <ProtectedRoute>
                <ReceiptPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin"
            element={
              <ProtectedRoute requireAdmin>
                <AdminPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </div>
    </>
  );
}