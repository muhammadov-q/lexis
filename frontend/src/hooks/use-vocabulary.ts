'use client'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  addToVocabulary,
  getVocabularyWord,
  listVocabulary,
  removeFromVocabulary,
  updateVocabularyNote,
  type VocabularyWord,
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
    onSuccess: (word) => {
      queryClient.setQueryData(wordKey(word.id), word)
      queryClient.setQueryData<VocabularyWord[]>(
        VOCABULARY_KEY,
        (words) => words && [word, ...words]
      )
    },
  })
}

export function useUpdateNote() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, note }: { id: number; note: string }) => updateVocabularyNote(id, note),
    onSuccess: (word) => {
      queryClient.setQueryData(wordKey(word.id), word)
      queryClient.setQueryData<VocabularyWord[]>(VOCABULARY_KEY, (words) =>
        words?.map((entry) => (entry.id === word.id ? word : entry))
      )
    },
  })
}

export function useRemoveFromVocabulary(id: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => removeFromVocabulary(id),
    onSuccess: () =>
      queryClient.setQueryData<VocabularyWord[]>(VOCABULARY_KEY, (words) =>
        words?.filter((word) => word.id !== id)
      ),
  })
}
