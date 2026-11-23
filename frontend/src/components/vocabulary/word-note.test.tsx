import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '@/test/render'
import { makeVocabularyWord } from '@/test/vocabulary'
import { WordNote } from './word-note'

afterEach(() => {
  vi.unstubAllGlobals()
})

function stubBackend(list: () => Response, save?: (init: RequestInit) => Response) {
  const fetch = vi.fn(async (_url: string, init: RequestInit = {}) =>
    init.method !== 'GET' && save ? save(init) : list()
  )
  vi.stubGlobal('fetch', fetch)
  return fetch
}

function bodyOf(fetch: ReturnType<typeof stubBackend>, method: string) {
  const call = fetch.mock.calls.find(([, init]) => init?.method === method)
  return JSON.parse(call?.[1]?.body as string)
}

describe('WordNote', () => {
  it('saves the word with its note from its place on the page', async () => {
    const fetch = stubBackend(
      () => Response.json([]),
      () => Response.json(makeVocabularyWord({ id: 5, word: 'ἐστί', note: 'is' }), { status: 201 })
    )
    renderWithProviders(<WordNote recognitionId={7} wordIndex={1} text="ἐστί," />)

    await userEvent.type(await screen.findByLabelText('Note'), 'is')
    await userEvent.click(screen.getByRole('button', { name: 'Save word' }))

    expect(await screen.findByText('Saved')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'In vocabulary' })).toHaveAttribute(
      'href',
      '/dashboard/vocabulary/5'
    )
    expect(screen.getByRole('button', { name: 'Save note' })).toBeDisabled()
    expect(bodyOf(fetch, 'POST')).toEqual({ recognition_id: 7, word_index: 1, note: 'is' })
    expect(fetch.mock.calls.filter(([, init]) => init?.method === 'POST')).toHaveLength(1)
  })

  it('saves a typed note when you move on, so it is not lost', async () => {
    const fetch = stubBackend(
      () => Response.json([]),
      () => Response.json(makeVocabularyWord({ id: 5, word: 'ἐστί', note: 'is' }), { status: 201 })
    )
    renderWithProviders(<WordNote recognitionId={7} wordIndex={1} text="ἐστί," />)

    await userEvent.type(await screen.findByLabelText('Note'), 'is')
    await userEvent.click(document.body)

    expect(await screen.findByText('Saved')).toBeInTheDocument()
    expect(bodyOf(fetch, 'POST')).toEqual({ recognition_id: 7, word_index: 1, note: 'is' })
  })

  it('edits the note of a word you saved, even with other punctuation around it', async () => {
    const saved = makeVocabularyWord({ id: 3, word: 'ἐστί', note: 'is' })
    const fetch = stubBackend(
      () => Response.json([saved]),
      () => Response.json({ ...saved, note: 'is, from εἰμί' })
    )
    renderWithProviders(<WordNote recognitionId={7} wordIndex={4} text="ἐστί·" />)

    const note = await screen.findByLabelText('Note')
    expect(note).toHaveValue('is')
    await userEvent.type(note, ', from εἰμί')
    await userEvent.click(screen.getByRole('button', { name: 'Save note' }))

    expect(await screen.findByText('Saved')).toBeInTheDocument()
    expect(fetch.mock.calls.some(([url]) => url === '/api/vocabulary/3')).toBe(true)
    expect(bodyOf(fetch, 'PATCH')).toEqual({ note: 'is, from εἰμί' })
  })

  it('says what went wrong when the word cannot be saved', async () => {
    stubBackend(
      () => Response.json([]),
      () => Response.json({ detail: 'This word is already in your vocabulary.' }, { status: 409 })
    )
    renderWithProviders(<WordNote recognitionId={7} wordIndex={0} text="Φύσις" />)

    await userEvent.click(await screen.findByRole('button', { name: 'Save word' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'This word is already in your vocabulary.'
    )
  })

  it('offers nothing for punctuation', async () => {
    const fetch = stubBackend(() => Response.json([]))
    renderWithProviders(<WordNote recognitionId={7} wordIndex={2} text="·" />)

    await vi.waitFor(() => expect(fetch).toHaveBeenCalled())
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument()
  })

  it('offers nothing when you are signed out', async () => {
    const fetch = stubBackend(() => Response.json({ detail: 'Sign in' }, { status: 401 }))
    renderWithProviders(<WordNote recognitionId={7} wordIndex={0} text="Φύσις" />)

    await vi.waitFor(() => expect(fetch).toHaveBeenCalled())
    await vi.waitFor(() => expect(screen.queryByRole('textbox')).not.toBeInTheDocument())
  })
})
