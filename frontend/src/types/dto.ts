export interface DeviceDto {
    id: string
    name: string
    location?: LocationDto
}

export interface LocationDto {
    id: string
    name: string
    zoneId: number | null
    x: number
    y: number
}

export interface FirmDto {
    id: string
    firmName: string
    users: UserDto[]
    locations: LocationDto[]
    devices: DeviceDto[]
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