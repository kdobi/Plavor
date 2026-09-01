export const currencyFormatter = new Intl.NumberFormat('ko-KR')

export function formatImageUrl(imageUrl: string, width = 720) {
  if (
    imageUrl.startsWith('/')
    || imageUrl.startsWith('blob:')
    || imageUrl.startsWith('data:')
  ) {
    return imageUrl
  }

  try {
    const url = new URL(imageUrl)
    if (!url.hostname.includes('images.unsplash.com')) {
      return imageUrl
    }
  } catch {
    return imageUrl
  }

  const params = `auto=format&fit=crop&w=${width}&q=86`
  return imageUrl.includes('?') ? `${imageUrl}&${params}` : `${imageUrl}?${params}`
}
