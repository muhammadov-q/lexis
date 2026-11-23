import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, renderHook, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { makeVocabularyWord } from '@/test/vocabulary'
import {
  useRemoveFromVocabulary,
  useUpdateNote,
  useVocabulary,
  useVocabularyWord,
} from './use-vocabulary'

afterEach(() => {
  vi.unstubAllGlobals()
})

function renderWithClient<T>(hook: () => T) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  return renderHook(hook, {
    wrapper: ({ children }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    ),
  })
}

function stubBackend() {
  let words = [makeVocabularyWord({ id: 1 }), makeVocabularyWord({ id: 2, word: 'καὶ' })]
  const fetch = vi.fn(async (url: string, init: RequestInit = {}) => {
    if (url === '/api/vocabulary') return Response.json(words)
    const id = Number(url.split('/').pop())
    if (init.method === 'DELETE') {
      words = words.filter((word) => word.id !== id)
      return new Response(null, { status: 204 })
    }
    if (init.method === 'PATCH') {
      const { note } = JSON.parse(init.body as string)
      words = words.map((word) => (word.id === id ? { ...word, note } : word))
    }
    return Response.json(words.find((word) => word.id === id))
  })
  vi.stubGlobal('fetch', fetch)
}

describe('vocabulary hooks', () => {
  it('drops a removed word from the list without loading it again', async () => {
    stubBackend()
    const { result } = renderWithClient(() => ({
      list: useVocabulary(),
      remove: useRemoveFromVocabulary(1),
    }))
    await waitFor(() => expect(result.current.list.data).toHaveLength(2))

    await act(() => result.current.remove.mutateAsync())

    await waitFor(() => expect(result.current.list.data?.map((word) => word.id)).toEqual([2]))
    const lists = vi.mocked(fetch).mock.calls.filter(([url]) => url === '/api/vocabulary')
    expect(lists).toHaveLength(1)
  })

  it('shows a saved note on the word and in the list', async () => {
    stubBackend()
    const { result } = renderWithClient(() => ({
      list: useVocabulary(),
      word: useVocabularyWord(2),
      update: useUpdateNote(),
    }))
    await waitFor(() => expect(result.current.word.data?.note).toBe(''))

    await act(() => result.current.update.mutateAsync({ id: 2, note: 'and' }))

    await waitFor(() => expect(result.current.word.data?.note).toBe('and'))
    expect(result.current.list.data?.find((word) => word.id === 2)?.note).toBe('and')
  })
})
