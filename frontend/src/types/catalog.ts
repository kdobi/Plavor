export type Category = {
  id: number
  name: string
  slug: string
  displayOrder: number
}

export type ProductImage = {
  id: number
  imageUrl: string
  altText: string | null
  displayOrder: number
  thumbnail: boolean
}

export type ProductStatus = 'ACTIVE' | 'SOLD_OUT' | 'HIDDEN'

export type ProductSummary = {
  id: number
  categoryId: number
  categoryName: string
  categorySlug: string
  name: string
  price: number
  stockQuantity: number
  status: ProductStatus
  thumbnailImage: ProductImage | null
}

export type ProductDetail = {
  id: number
  category: Category
  name: string
  description: string | null
  price: number
  stockQuantity: number
  status: ProductStatus
  images: ProductImage[]
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
