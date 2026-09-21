import { api } from './client'
import { backend } from './backend'
import type { JwtResponseDto, LoginRequestDto, RegisterRequestDto } from '../types'

export async function login(input: LoginRequestDto): Promise<JwtResponseDto> {
  const { data } = await api.post<JwtResponseDto>(backend.paths.login, input)
  return data
}
export async function register(input: RegisterRequestDto): Promise<void> {
  await api.post(backend.paths.register, input)
}
