import type { Category, ProductDetail, ProductSummary } from '../types/catalog'

export const fallbackCategories: Category[] = [
  { id: 1, name: 'Tops', slug: 'tops', displayOrder: 1 },
  { id: 2, name: 'Outerwear', slug: 'outerwear', displayOrder: 2 },
  { id: 3, name: 'Bags', slug: 'bags', displayOrder: 3 },
]

export const fallbackProducts: ProductSummary[] = [
  {
    id: 3,
    categoryId: 2,
    categoryName: 'Outerwear',
    categorySlug: 'outerwear',
    name: 'Daily Denim Jacket',
    price: 89000,
    stockQuantity: 0,
    status: 'SOLD_OUT',
    thumbnailImage: {
      id: 3,
      imageUrl: 'https://images.unsplash.com/photo-1543076447-215ad9ba6923',
      altText: 'Daily Denim Jacket',
      displayOrder: 1,
      thumbnail: true,
    },
  },
  {
    id: 2,
    categoryId: 2,
    categoryName: 'Outerwear',
    categorySlug: 'outerwear',
    name: 'Relaxed Zip Hoodie',
    price: 69000,
    stockQuantity: 45,
    status: 'ACTIVE',
    thumbnailImage: {
      id: 2,
      imageUrl: 'https://images.unsplash.com/photo-1556821840-3a63f95609a7',
      altText: 'Relaxed Zip Hoodie',
      displayOrder: 1,
      thumbnail: true,
    },
  },
  {
    id: 1,
    categoryId: 1,
    categoryName: 'Tops',
    categorySlug: 'tops',
    name: 'Minimal Cotton T-Shirt',
    price: 29000,
    stockQuantity: 120,
    status: 'ACTIVE',
    thumbnailImage: {
      id: 1,
      imageUrl: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab',
      altText: 'Minimal Cotton T-Shirt',
      displayOrder: 1,
      thumbnail: true,
    },
  },
]

export function findFallbackProductDetail(productId: string) {
  const summary = fallbackProducts.find((product) => product.id === Number(productId))

  if (!summary) {
    return null
  }

  const category = fallbackCategories.find(
    (fallbackCategory) => fallbackCategory.id === summary.categoryId,
  )

  if (!category) {
    return null
  }

  return {
    id: summary.id,
    category,
    name: summary.name,
    description: getFallbackDescription(summary.id),
    price: summary.price,
    stockQuantity: summary.stockQuantity,
    status: summary.status,
    images: summary.thumbnailImage ? [summary.thumbnailImage] : [],
  } satisfies ProductDetail
}

export function filterFallbackProducts(categoryId: number | null, keyword: string) {
  const normalizedKeyword = keyword.trim().toLowerCase()

  return fallbackProducts.filter((product) => {
    const matchesCategory =
      categoryId === null || product.categoryId === categoryId
    const matchesKeyword =
      !normalizedKeyword ||
      product.name.toLowerCase().includes(normalizedKeyword)

    return matchesCategory && matchesKeyword
  })
}

function getFallbackDescription(productId: number) {
  switch (productId) {
    case 1:
      return '탄탄한 코튼 소재와 여유 있는 실루엣으로 매일 편하게 입기 좋은 티셔츠입니다.'
    case 2:
      return '간절기 레이어링에 알맞은 두께감의 집업 후디입니다. 과하지 않은 핏으로 다양한 하의와 잘 어울립니다.'
    case 3:
      return '부드럽게 워싱된 데님 질감과 정돈된 아웃핏이 특징인 데일리 재킷입니다.'
    default:
      return null
  }
}
