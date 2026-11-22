import { apiRequest } from './client'
import type { paths } from './schema'

type AddOperation = paths['/vocabulary']['post']
type NoteOperation = paths['/vocabulary/{word_id}']['patch']

export type VocabularyWord = AddOperation['responses'][201]['content']['application/json']
export type NewVocabularyWord = AddOperation['requestBody']['content']['application/json']
type NoteBody = NoteOperation['requestBody']['content']['application/json']

export function listVocabulary(signal?: AbortSignal): Promise<VocabularyWord[]> {
  return apiRequest<VocabularyWord[]>('/vocabulary', {
    signal,
    fallback: 'Could not load your vocabulary.',
  })
}

export function getVocabularyWord(id: number, signal?: AbortSignal): Promise<VocabularyWord> {
  return apiRequest<VocabularyWord>(`/vocabulary/${id}`, {
    signal,
    fallback: 'Could not load this word.',
  })
}

export function addToVocabulary(word: NewVocabularyWord): Promise<VocabularyWord> {
  return apiRequest<VocabularyWord>('/vocabulary', {
    method: 'POST',
    body: word,
    fallback: 'Could not add this word.',
  })
}

export function updateVocabularyNote(id: number, note: string): Promise<VocabularyWord> {
  const body: NoteBody = { note }
  return apiRequest<VocabularyWord>(`/vocabulary/${id}`, {
    method: 'PATCH',
    body,
    fallback: 'Could not save your note.',
  })
}

export function removeFromVocabulary(id: number): Promise<void> {
  return apiRequest<void>(`/vocabulary/${id}`, {
    method: 'DELETE',
    fallback: 'Could not remove this word.',
  })
}
