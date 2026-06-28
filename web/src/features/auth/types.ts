export interface AuthUserInfo {
  id: string
  email: string
  username: string
  roles: string[]
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  userInfo: AuthUserInfo
}

export interface LoginRequest {
  identifier: string
  password: string
  deviceFingerprint?: string
}

export interface UserResponse {
  id: string
  firstname?: string
  lastname?: string
  username: string
  email: string
  phoneNumber?: string
  status?: string
  avatarUrl?: string
}
