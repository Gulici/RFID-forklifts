export interface DeviceDto {
    id: string
    name: string
    location?: LocationDto
    alive: boolean
    timestamp: string
}

export interface LocationDto {
    id: string
    name: string
    zoneId: number
    x: number
    y: number
}

export interface LocationHistoryDto {
  deviceDto: DeviceDto,
  locationDto: LocationDto,
  timestamp: string
}

export interface FirmDto {
    id: string
    firmName: string
    users: UserDto[]
    locations: LocationDto[]
    devices: DeviceDto[]
}

export interface FirmRegisterDto {
    firmName: string;
    adminName: string;
    adminEmail: string;
    password: string;
}

export interface UserDto {
    id: string
    username: string
    email: string
    firmName: string
    roles: Role[];
}

export type Role = 'ROLE_USER' | 'ROLE_ADMIN' | 'ROLE_ROOT';

export interface LoginRequest {
    username: string
    password: string
}

export interface LoginResponse {
    jwt: string
}

export interface JwtPayload {
    sub: string;
    roles: Role[];
    [key: string]: any;
}