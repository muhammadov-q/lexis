'use client'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { User } from '@/lib/api/auth'
import { ApiError } from '@/lib/api/client'
import { predictPage, type Prediction } from '@/lib/api/predict'
import { getRecognition, listRecognitions, recognizePage } from '@/lib/api/recognitions'
import { CURRENT_USER_KEY } from './use-auth'

export const RECOGNITIONS_KEY = ['recognitions'] as const

export interface RecognizeResult {
  recognition: Prediction
  durationMs: number
  savedId?: number
}

export function useRecognize() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationKey: ['recognize'],
    mutationFn: async ({
      file,
      model,
      signal,
    }: {
      file: File
      model?: string
      signal?: AbortSignal
    }): Promise<RecognizeResult> => {
      const startedAt = performance.now()

      if (queryClient.getQueryData<User | null>(CURRENT_USER_KEY) !== null) {
        try {
          const recognition = await recognizePage(file, { model, signal })
          return { recognition, savedId: recognition.id, durationMs: performance.now() - startedAt }
        } catch (error) {
          if (!(error instanceof ApiError) || error.status !== 401) throw error
          await queryClient.cancelQueries({ queryKey: CURRENT_USER_KEY })
          queryClient.setQueryData(CURRENT_USER_KEY, null)
        }
      }

      const recognition = await predictPage(file, { model, signal })
      return { recognition, durationMs: performance.now() - startedAt }
    },
    onSuccess: (result) => {
      if (result.savedId !== undefined) {
        void queryClient.invalidateQueries({ queryKey: RECOGNITIONS_KEY })
      }
    },
  })
}

export function useRecognitions() {
  return useQuery({
    queryKey: RECOGNITIONS_KEY,
    queryFn: ({ signal }) => listRecognitions(signal),
    retry: false,
  })
}

export function useRecognition(id: number) {
  return useQuery({
    queryKey: [...RECOGNITIONS_KEY, id],
    queryFn: ({ signal }) => getRecognition(id, signal),
    retry: false,
  })
}
