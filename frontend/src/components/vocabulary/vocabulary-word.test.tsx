import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { VocabularyWord } from '@/lib/api/vocabulary'
import { renderWithProviders } from '@/test/render'
import { makeVocabularyWord } from '@/test/vocabulary'
import { VocabularyWordView } from './vocabulary-word'

const router = vi.hoisted(() => ({ replace: vi.fn() }))
vi.mock('next/navigation', () => ({ useRouter: () => router }))

beforeEach(() => {
  router.replace.mockClear()
})

afterEach(() => {
  vi.unstubAllGlobals()
})

function stubBackend(word: VocabularyWord) {
  let current = word
  const fetch = vi.fn(async (url: string, init: RequestInit = {}) => {
    if (url === '/api/vocabulary') return Response.json([current])
    if (init.method === 'PATCH') {
      current = { ...current, note: JSON.parse(init.body as string).note.trim() }
      return Response.json(current)
    }
    if (init.method === 'DELETE') return new Response(null, { status: 204 })
    return Response.json(current)
  })
  vi.stubGlobal('fetch', fetch)
  return fetch
}

describe('VocabularyWordView', () => {
  it('shows the word, its machine translation and where it came from', async () => {
    stubBackend(
      makeVocabularyWord({
        id: 4,
        source: { recognition_id: 7, filename: 'folio-1r.png', line: 2, word_index: 12 },
      })
    )
    renderWithProviders(<VocabularyWordView id={4} />)

    expect(await screen.findByRole('heading', { level: 1, name: 'Φύσις' })).toBeVisible()
    expect(screen.getByText('Machine output')).toBeInTheDocument()
    expect(screen.getByText('nature')).toBeInTheDocument()
    expect(screen.getByText('noun: nature')).toBeInTheDocument()
    expect(screen.getByText('From folio-1r.png, line 3')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Show on the page' })).toHaveAttribute(
      'href',
      '/dashboard/history/7?word=12'
    )
    expect(screen.getByRole('link', { name: 'Vocabulary' })).toHaveAttribute(
      'href',
      '/dashboard/vocabulary'
    )
  })

  it('says so when the model gave no translation', async () => {
    stubBackend(makeVocabularyWord({ gloss: null }))
    renderWithProviders(<VocabularyWordView id={1} />)

    expect(await screen.findByText('No translation')).toBeInTheDocument()
  })

  it('saves your note', async () => {
    const fetch = stubBackend(makeVocabularyWord({ id: 4, note: '' }))
    renderWithProviders(<VocabularyWordView id={4} />)

    const note = await screen.findByLabelText('Your note')
    const save = screen.getByRole('button', { name: 'Save note' })
    expect(save).toBeDisabled()

    await userEvent.type(note, 'as in physics ')
    await userEvent.click(save)

    expect(await screen.findByText('Note saved')).toBeInTheDocument()
    expect(save).toBeDisabled()
    const patch = fetch.mock.calls.find(([, init]) => init?.method === 'PATCH')
    expect(patch?.[0]).toBe('/api/vocabulary/4')
    expect(JSON.parse(patch?.[1]?.body as string)).toEqual({ note: 'as in physics ' })

    await userEvent.type(note, 'again')
    expect(screen.queryByText('Note saved')).not.toBeInTheDocument()
    expect(save).toBeEnabled()
  })

  it('removes the word after you confirm, then goes back to the list', async () => {
    const fetch = stubBackend(makeVocabularyWord({ id: 4, note: 'as in physics' }))
    renderWithProviders(<VocabularyWordView id={4} />)

    await userEvent.click(await screen.findByRole('button', { name: 'Remove from vocabulary' }))
    const dialog = screen.getByRole('dialog', { name: 'Remove Φύσις?' })
    expect(dialog).toHaveTextContent('Your note on this word is deleted too.')
    expect(fetch).not.toHaveBeenCalledWith(
      '/api/vocabulary/4',
      expect.objectContaining({ method: 'DELETE' })
    )

    await userEvent.click(screen.getByRole('button', { name: 'Remove' }))

    await vi.waitFor(() => expect(router.replace).toHaveBeenCalledWith('/dashboard/vocabulary'))
    expect(fetch).toHaveBeenCalledWith(
      '/api/vocabulary/4',
      expect.objectContaining({ method: 'DELETE' })
    )
  })

  it('says so when the word is not yours or is gone', async () => {
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValue(
          Response.json({ detail: 'This word is not in your vocabulary.' }, { status: 404 })
        )
    )
    renderWithProviders(<VocabularyWordView id={99} />)

    const alert = await screen.findByRole('alert')
    expect(alert).toHaveTextContent('Could not open this word')
    expect(alert).toHaveTextContent('This word is not in your vocabulary.')
  })
})
