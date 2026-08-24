export const currencyFormatter = new Intl.NumberFormat('ko-KR')

export function formatImageUrl(imageUrl: string, width = 720) {
  const params = `auto=format&fit=crop&w=${width}&q=86`
  return imageUrl.includes('?') ? `${imageUrl}&${params}` : `${imageUrl}?${params}`
}
