import { api } from './client'
import { backend } from './backend'
import type { TaskRequestDto, TaskResponseDto } from '../types'

export async function listTasks(): Promise<TaskResponseDto[]> {
  const { data } = await api.get<TaskResponseDto[]>(backend.paths.myTasks)
  return data
}
export async function getTask(id: string): Promise<TaskResponseDto> {
  const { data } = await api.get<TaskResponseDto>(backend.paths.task(id))
  return data
}
export async function createTask(input: TaskRequestDto): Promise<TaskResponseDto> {
  const { data } = await api.post<TaskResponseDto>(backend.paths.tasks, input)
  return data
}
export async function updateTask(id: string, input: TaskRequestDto): Promise<TaskResponseDto> {
  const { data } = await api.put<TaskResponseDto>(backend.paths.task(id), input)
  return data
}
export async function deleteTask(id: string): Promise<void> {
  await api.delete(backend.paths.task(id))
}
