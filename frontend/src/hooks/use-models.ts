'use client'

import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { listModels } from '@/lib/api/models'

const MODELS_KEY = ['models'] as const

export type ModelChoice = ReturnType<typeof useModelChoice>

export function useModelChoice() {
  const query = useQuery({
    queryKey: MODELS_KEY,
    queryFn: ({ signal }) => listModels(signal),
    staleTime: 5 * 60_000,
  })
  const [chosen, setChosen] = useState<string | null>(null)
  const models = query.data?.models ?? []
  const selected =
    chosen !== null && models.some((model) => model.id === chosen)
      ? chosen
      : query.data?.default_model

  return { query, models, selected, choose: setChosen }
}
