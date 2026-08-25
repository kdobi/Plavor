export type OrderStatus =
  | 'CREATED'
  | 'PAID'
  | 'PREPARING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELED'

export type OrderItem = {
  id: number
  productId: number
  productName: string
  thumbnailImageUrl: string | null
  unitPrice: number
  quantity: number
  totalPrice: number
}

export type Order = {
  id: number
  orderNumber: string
  status: OrderStatus
  totalAmount: number
  receiverName: string
  receiverPhone: string
  postalCode: string
  address: string
  addressDetail: string | null
  deliveryMessage: string | null
  orderedAt: string
  items: OrderItem[]
}

export type OrderCreateRequest = {
  cartItemIds: number[]
  receiverName: string
  receiverPhone: string
  postalCode: string
  address: string
  addressDetail?: string
  deliveryMessage?: string
}
