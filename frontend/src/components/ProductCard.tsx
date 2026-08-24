import { Link } from 'react-router-dom'
import type { ProductSummary } from '../types/catalog'
import { currencyFormatter, formatImageUrl } from '../utils/catalog'

export function ProductCard({ product }: { product: ProductSummary }) {
  return (
    <article className="product-card">
      <Link className="product-link" to={`/products/${product.id}`}>
        <div className="image-wrap">
          {product.thumbnailImage ? (
            <img
              src={formatImageUrl(product.thumbnailImage.imageUrl)}
              alt={product.thumbnailImage.altText ?? product.name}
              loading="lazy"
            />
          ) : (
            <div className="image-fallback">{product.categoryName}</div>
          )}
          {product.status === 'SOLD_OUT' && (
            <span className="status-badge">Sold out</span>
          )}
        </div>
        <div className="product-info">
          <div>
            <p className="category-name">{product.categoryName}</p>
            <h3>{product.name}</h3>
          </div>
          <div className="price-row">
            <strong>{currencyFormatter.format(product.price)}원</strong>
            <span>{product.stockQuantity > 0 ? '구매 가능' : '재입고 대기'}</span>
          </div>
        </div>
      </Link>
    </article>
  )
}
