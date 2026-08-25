import { BrowserRouter, Route, Routes } from 'react-router-dom'
import './App.css'
import { AuthProvider } from './auth/AuthContext'
import { AdminProductFormPage } from './pages/AdminProductFormPage'
import { AdminProductsPage } from './pages/AdminProductsPage'
import { CartPage } from './pages/CartPage'
import { HomePage } from './pages/HomePage'
import { KakaoCallbackPage } from './pages/KakaoCallbackPage'
import { LoginPage } from './pages/LoginPage'
import { OrderCompletePage } from './pages/OrderCompletePage'
import { OrderDetailPage } from './pages/OrderDetailPage'
import { OrdersPage } from './pages/OrdersPage'
import { ProductDetailPage } from './pages/ProductDetailPage'
import { SignupPage } from './pages/SignupPage'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/auth/kakao/callback" element={<KakaoCallbackPage />} />
          <Route path="/products/:productId" element={<ProductDetailPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/orders/complete/:orderId" element={<OrderCompletePage />} />
          <Route path="/orders/:orderId" element={<OrderDetailPage />} />
          <Route path="/admin/products" element={<AdminProductsPage />} />
          <Route path="/admin/products/new" element={<AdminProductFormPage />} />
          <Route
            path="/admin/products/:productId/edit"
            element={<AdminProductFormPage />}
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
