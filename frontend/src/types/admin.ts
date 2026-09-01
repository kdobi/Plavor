import type {
  Category,
  PageResponse,
  ProductImage,
  ProductStatus,
} from './catalog'
import type { OrderItem, OrderStatus } from './order'

export type AdminProduct = {
  id: number
  category: Category
  name: string
  description: string | null
  price: number
  stockQuantity: number
  status: ProductStatus
  images: ProductImage[]
  createdAt: string
  updatedAt: string
}

export type AdminProductImageRequest = {
  imageUrl: string
  altText?: string | null
  displayOrder: number
  thumbnail: boolean
}

export type AdminProductRequest = {
  categoryId: number
  name: string
  description?: string | null
  price: number
  stockQuantity: number
  status: ProductStatus
  images: AdminProductImageRequest[]
}

export type AdminProductStatusUpdateRequest = {
  status: ProductStatus
}

export type AdminProductPage = PageResponse<AdminProduct>

export type AdminOrder = {
  id: number
  orderNumber: string
  status: OrderStatus
  totalAmount: number
  memberId: number
  memberEmail: string
  memberName: string
  receiverName: string
  receiverPhone: string
  postalCode: string
  address: string
  addressDetail: string | null
  deliveryMessage: string | null
  orderedAt: string
  updatedAt: string
  availableNextStatuses: OrderStatus[]
  items: OrderItem[]
}

export type AdminOrderStatusUpdateRequest = {
  status: OrderStatus
}

export type AdminOrderPage = PageResponse<AdminOrder>
