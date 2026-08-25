import type {
  Category,
  PageResponse,
  ProductImage,
  ProductStatus,
} from './catalog'

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
