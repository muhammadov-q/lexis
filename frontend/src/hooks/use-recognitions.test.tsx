import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, renderHook, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { makeRecognition } from '@/test/recognitions'
import { CURRENT_USER_KEY } from './use-auth'
import { useRecognitions, useRecognize } from './use-recognitions'

afterEach(() => {
  vi.unstubAllGlobals()
})

function renderWithClient<T>(hook: () => T, guest = false) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  if (guest) {
    queryClient.setQueryData(CURRENT_USER_KEY, null)
  }
  return renderHook(hook, {
    wrapper: ({ children }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    ),
  })
}

describe('useRecognize', () => {
  it('refreshes the history after reading a page', async () => {
    const first = makeRecognition({ id: 1 })
    const second = makeRecognition({ id: 2 })
    const history = [first]
    const fetch = vi.fn(async (_url: string, init?: RequestInit) => {
      if (init?.method !== 'POST') return Response.json(history)
      history.unshift(second)
      return Response.json(second)
    })
    vi.stubGlobal('fetch', fetch)

    const { result } = renderWithClient(() => ({
      history: useRecognitions(),
      recognize: useRecognize(),
    }))
    await waitFor(() => expect(result.current.history.data).toEqual([first]))

    await act(() =>
      result.current.recognize.mutateAsync({
        file: new File(['x'], 'folio-2r.png', { type: 'image/png' }),
      })
    )

    await waitFor(() => expect(result.current.history.data).toEqual([second, first]))
    expect(result.current.recognize.data?.recognition).toEqual(second)
  })
})

describe('guest recognition', () => {
  it('uploads only once to public prediction for a known guest', async () => {
    const saved = makeRecognition()
    const prediction = {
      predictions: saved.predictions,
      model_version: saved.model_version,
    }

    const fetch = vi.fn().mockResolvedValue(Response.json(prediction))
    vi.stubGlobal('fetch', fetch)

    const { result } = renderWithClient(useRecognize, true)
    const file = new File(['bytes'], 'digits.png', { type: 'image/png' })

    await act(() => result.current.mutateAsync({ file }))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))

    expect(fetch).toHaveBeenCalledTimes(1)
    expect(fetch.mock.calls[0][0]).toBe('/api/predict')
    expect(result.current.data?.recognition).toEqual(prediction)
    expect(result.current.data?.savedId).toBeUndefined()

    const init = fetch.mock.calls[0][1] as RequestInit
    expect((init.body as FormData).get('image')).toBe(file)
  })

  it('uses public prediction when authentication is required', async () => {
    const saved = makeRecognition()
    const prediction = {
      predictions: saved.predictions,
      model_version: saved.model_version,
    }

    const fetch = vi
      .fn()
      .mockResolvedValueOnce(Response.json({ detail: 'Sign in to continue.' }, { status: 401 }))
      .mockResolvedValueOnce(Response.json(prediction))

    vi.stubGlobal('fetch', fetch)

    const { result } = renderWithClient(useRecognize)
    const file = new File(['bytes'], 'digits.png', { type: 'image/png' })

    await act(() => result.current.mutateAsync({ file, model: 'kraken' }))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))

    expect(fetch.mock.calls.map(([url]) => url)).toEqual(['/api/recognitions', '/api/predict'])
    expect(result.current.data?.recognition).toEqual(prediction)
    expect(result.current.data?.savedId).toBeUndefined()

    const forms = fetch.mock.calls.map(([, init]) => (init as RequestInit).body as FormData)
    expect(forms.map((form) => form.get('model'))).toEqual(['kraken', 'kraken'])
    expect(forms[1].get('image')).toBe(file)
  })

  it('does not fall back to public prediction for server errors', async () => {
    const fetch = vi
      .fn()
      .mockResolvedValue(Response.json({ detail: 'Server unavailable' }, { status: 500 }))

    vi.stubGlobal('fetch', fetch)

    const { result } = renderWithClient(useRecognize)

    await act(async () => {
      await result.current
        .mutateAsync({
          file: new File(['bytes'], 'digits.png', { type: 'image/png' }),
        })
        .catch(() => {})
    })

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(fetch).toHaveBeenCalledTimes(1)
  })
})
