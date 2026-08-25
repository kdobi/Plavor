let memoryAccessToken: string | null = null

export function getStoredAccessToken() {
  return memoryAccessToken
}

export function storeAccessToken(accessToken: string) {
  memoryAccessToken = accessToken
}

export function clearStoredAccessToken() {
  memoryAccessToken = null
}
