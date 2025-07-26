export interface JwtPayload {
  roles?: string[]
  [key: string]: any
}
  
export function parseJwt(token: string): JwtPayload | null {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = atob(base64)
    return JSON.parse(jsonPayload) as JwtPayload
  } catch (e) {
    return null
  }
}

export function getUserRoles(): string[] | null {
  const token = localStorage.getItem('token')
  if (!token) return null
  const payload = parseJwt(token)
  return payload?.roles ?? null
}
