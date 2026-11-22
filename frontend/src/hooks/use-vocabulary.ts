'use client'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  addToVocabulary,
  getVocabularyWord,
  listVocabulary,
  removeFromVocabulary,
  updateVocabularyNote,
} from '@/lib/api/vocabulary'

export const VOCABULARY_KEY = ['vocabulary'] as const

const wordKey = (id: number) => [...VOCABULARY_KEY, id] as const

export function useVocabulary() {
  return useQuery({
    queryKey: VOCABULARY_KEY,
    queryFn: ({ signal }) => listVocabulary(signal),
    retry: false,
  })
}

export function useVocabularyWord(id: number) {
  return useQuery({
    queryKey: wordKey(id),
    queryFn: ({ signal }) => getVocabularyWord(id, signal),
    retry: false,
  })
}

export function useAddToVocabulary() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: addToVocabulary,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: VOCABULARY_KEY, exact: true }),
  })
}

export function useUpdateNote(id: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (note: string) => updateVocabularyNote(id, note),
    onSuccess: (word) => {
      queryClient.setQueryData(wordKey(id), word)
      return queryClient.invalidateQueries({ queryKey: VOCABULARY_KEY, exact: true })
    },
  })
}

export function useRemoveFromVocabulary(id: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => removeFromVocabulary(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: VOCABULARY_KEY, exact: true }),
  })
}
