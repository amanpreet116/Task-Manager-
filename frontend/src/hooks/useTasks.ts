import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as tasksApi from '../api/tasks'
import { errorMessage } from '../api/client'
import { useActivity } from '../features/activity/ActivityProvider'
import { useToast } from '../components/ui/ToastProvider'
import type { TaskRequestDto, TaskResponseDto } from '../types'

export const taskKeys = { all: ['tasks'] as const, detail: (id: string) => ['tasks', id] as const }
export function useTasks() { return useQuery({ queryKey: taskKeys.all, queryFn: tasksApi.listTasks }) }
export function useTask(id: string) { return useQuery({ queryKey: taskKeys.detail(id), queryFn: () => tasksApi.getTask(id), enabled: !!id }) }
export function useCreateTask() {
  const client = useQueryClient(), toast = useToast(), activity = useActivity()
  return useMutation({ mutationFn: tasksApi.createTask, onSuccess: (task) => { client.invalidateQueries({ queryKey: taskKeys.all }); client.setQueryData(taskKeys.detail(task.id), task); activity.record('created', task.title); toast('Task created') }, onError: (error) => toast(errorMessage(error), 'error') })
}
export function useUpdateTask(id: string) {
  const client = useQueryClient(), toast = useToast(), activity = useActivity()
  return useMutation({ mutationFn: (input: TaskRequestDto) => tasksApi.updateTask(id, input),
    onMutate: async (input) => {
      await client.cancelQueries({ queryKey: taskKeys.detail(id) })
      const previous = client.getQueryData<TaskResponseDto>(taskKeys.detail(id))
      if (previous) client.setQueryData(taskKeys.detail(id), { ...previous, ...input })
      return { previous }
    },
    onError: (error, _input, context) => { if (context?.previous) client.setQueryData(taskKeys.detail(id), context.previous); toast(errorMessage(error), 'error') },
    onSuccess: (task) => { client.setQueryData(taskKeys.detail(id), task); client.invalidateQueries({ queryKey: taskKeys.all }); activity.record('updated', task.title); toast('Task updated') },
  })
}
export function useDeleteTask() {
  const client = useQueryClient(), toast = useToast(), activity = useActivity()
  return useMutation({ mutationFn: ({ id }: { id: string; title: string }) => tasksApi.deleteTask(id), onSuccess: (_data, task) => { client.removeQueries({ queryKey: taskKeys.detail(task.id) }); client.invalidateQueries({ queryKey: taskKeys.all }); activity.record('deleted', task.title); toast('Task deleted') }, onError: (error) => toast(errorMessage(error), 'error') })
}
