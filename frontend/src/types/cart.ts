export type CartItem = {
  id: number
  productId: number
  productName: string
  thumbnailImageUrl: string | null
  unitPrice: number
  quantity: number
  stockQuantity: number
  totalPrice: number
}

export type Cart = {
  id: number
  items: CartItem[]
  totalQuantity: number
  totalAmount: number
}

export type CartItemAddRequest = {
  productId: number
  quantity: number
}

export type CartItemUpdateRequest = {
  quantity: number
}
