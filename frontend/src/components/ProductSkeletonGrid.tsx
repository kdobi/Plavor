export function ProductSkeletonGrid() {
  return (
    <div className="product-grid" aria-label="상품을 불러오는 중">
      {Array.from({ length: 6 }, (_, index) => (
        <div className="product-card skeleton-card" key={index}>
          <div className="skeleton-image" />
          <div className="skeleton-line wide" />
          <div className="skeleton-line" />
        </div>
      ))}
    </div>
  )
}
