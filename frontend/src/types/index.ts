import type { backend } from '../api/backend'

export type TaskStatus = (typeof backend.statuses)[number]
export type Role = 'USER' | 'ADMIN'
export interface SubtaskResponseDto { id: string; title: string; completed: boolean }
export interface TaskRequestDto { title: string; description: string; status: TaskStatus }
export interface TaskResponseDto extends TaskRequestDto {
  id: string
  ownerId: string
  createdAt: string
  updatedAt: string
  subtasks: SubtaskResponseDto[]
}
export interface User { email: string; role: Role; subject: string }
export interface ApiError { message: string; status: number; timestamp: string }
export interface LoginRequestDto { email: string; password: string }
export type RegisterRequestDto = LoginRequestDto
export interface JwtResponseDto { token: string }
