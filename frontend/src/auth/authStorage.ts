const ACCESS_TOKEN_KEY = 'plavor.accessToken'
const KAKAO_OAUTH_STATE_KEY = 'plavor.kakaoOAuthState'

export function getStoredAccessToken() {
  return window.localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function storeAccessToken(accessToken: string) {
  window.localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
}

export function clearStoredAccessToken() {
  window.localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export function getStoredKakaoOAuthState() {
  return window.localStorage.getItem(KAKAO_OAUTH_STATE_KEY)
}

export function storeKakaoOAuthState(state: string) {
  window.localStorage.setItem(KAKAO_OAUTH_STATE_KEY, state)
}

export function clearStoredKakaoOAuthState() {
  window.localStorage.removeItem(KAKAO_OAUTH_STATE_KEY)
}
