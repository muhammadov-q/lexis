import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, renderHook, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { makeModelList } from '@/test/models'
import { useModelChoice } from './use-models'

afterEach(() => {
  vi.unstubAllGlobals()
})

function renderChoice(response: Response) {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response))
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return renderHook(useModelChoice, {
    wrapper: ({ children }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    ),
  })
}

describe('useModelChoice', () => {
  it("selects the backend's default until another model is chosen", async () => {
    const { result } = renderChoice(Response.json(makeModelList({ default_model: 'kraken-base' })))

    expect(result.current.selected).toBeUndefined()
    await waitFor(() => expect(result.current.selected).toBe('kraken-base'))

    act(() => result.current.choose('dummy'))
    expect(result.current.selected).toBe('dummy')
  })

  it('keeps the default when the chosen model is not on the list', async () => {
    const { result } = renderChoice(Response.json(makeModelList()))
    await waitFor(() => expect(result.current.models).toHaveLength(3))

    act(() => result.current.choose('knn'))
    expect(result.current.selected).toBe('kraken')
  })

  it('selects nothing when the list cannot be loaded, so the backend uses its default', async () => {
    const { result } = renderChoice(Response.json({ detail: 'Not reachable' }, { status: 502 }))

    await waitFor(() => expect(result.current.query.isError).toBe(true))
    expect(result.current.selected).toBeUndefined()
    expect(result.current.models).toEqual([])
  })
})
