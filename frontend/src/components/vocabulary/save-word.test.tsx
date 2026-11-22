import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '@/test/render'
import { makeVocabularyWord } from '@/test/vocabulary'
import { SaveWord } from './save-word'

afterEach(() => {
  vi.unstubAllGlobals()
})

function stubBackend(list: () => Response, add?: () => Response) {
  const fetch = vi.fn(async (_url: string, init: RequestInit = {}) =>
    init.method === 'POST' && add ? add() : list()
  )
  vi.stubGlobal('fetch', fetch)
  return fetch
}

describe('SaveWord', () => {
  it('adds the word from its place on the page, then links to it', async () => {
    const saved = makeVocabularyWord({ id: 5, word: 'ἐστί' })
    const words: (typeof saved)[] = []
    const fetch = stubBackend(
      () => Response.json(words),
      () => {
        words.push(saved)
        return Response.json(saved, { status: 201 })
      }
    )
    renderWithProviders(<SaveWord recognitionId={7} wordIndex={1} text="ἐστί," />)

    await userEvent.click(await screen.findByRole('button', { name: 'Add to vocabulary' }))

    expect(await screen.findByRole('link', { name: 'Open it' })).toHaveAttribute(
      'href',
      '/dashboard/vocabulary/5'
    )
    expect(screen.getByText(/In your vocabulary/)).toBeInTheDocument()
    const post = fetch.mock.calls.find(([, init]) => init?.method === 'POST')
    expect(JSON.parse(post?.[1]?.body as string)).toEqual({ recognition_id: 7, word_index: 1 })
  })

  it('knows the word is saved even with other punctuation around it', async () => {
    stubBackend(() => Response.json([makeVocabularyWord({ id: 3, word: 'ἐστί' })]))
    renderWithProviders(<SaveWord recognitionId={7} wordIndex={4} text="ἐστί·" />)

    expect(await screen.findByRole('link', { name: 'Open it' })).toHaveAttribute(
      'href',
      '/dashboard/vocabulary/3'
    )
    expect(screen.queryByRole('button', { name: 'Add to vocabulary' })).not.toBeInTheDocument()
  })

  it('says what went wrong when the word cannot be added', async () => {
    stubBackend(
      () => Response.json([]),
      () => Response.json({ detail: 'This word is already in your vocabulary.' }, { status: 409 })
    )
    renderWithProviders(<SaveWord recognitionId={7} wordIndex={0} text="Φύσις" />)

    await userEvent.click(await screen.findByRole('button', { name: 'Add to vocabulary' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'This word is already in your vocabulary.'
    )
  })

  it('offers nothing for punctuation', async () => {
    const fetch = stubBackend(() => Response.json([]))
    renderWithProviders(<SaveWord recognitionId={7} wordIndex={2} text="·" />)

    await vi.waitFor(() => expect(fetch).toHaveBeenCalled())
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })

  it('offers nothing when you are signed out', async () => {
    const fetch = stubBackend(() => Response.json({ detail: 'Sign in' }, { status: 401 }))
    renderWithProviders(<SaveWord recognitionId={7} wordIndex={0} text="Φύσις" />)

    await vi.waitFor(() => expect(fetch).toHaveBeenCalled())
    await vi.waitFor(() => expect(screen.queryByRole('button')).not.toBeInTheDocument())
    expect(screen.queryByRole('link')).not.toBeInTheDocument()
  })
})
