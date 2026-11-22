import { screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '@/test/render'
import { makeVocabularyWord } from '@/test/vocabulary'
import { VocabularyList } from './vocabulary-list'

afterEach(() => {
  vi.unstubAllGlobals()
})

function stubFetch(response: Response) {
  const fetch = vi.fn().mockResolvedValue(response)
  vi.stubGlobal('fetch', fetch)
  return fetch
}

describe('VocabularyList', () => {
  it('lists your words with their translation and page', async () => {
    stubFetch(
      Response.json([
        makeVocabularyWord({ id: 2 }),
        makeVocabularyWord({
          id: 1,
          word: 'καὶ',
          gloss: null,
          source: { recognition_id: 7, filename: '', line: 4, word_index: 9 },
        }),
      ])
    )
    renderWithProviders(<VocabularyList />)

    const list = await screen.findByRole('list', { name: 'Saved words' })
    const [first, second] = within(list).getAllByRole('link')
    expect(first).toHaveAttribute('href', '/dashboard/vocabulary/2')
    expect(first).toHaveTextContent('Φύσις')
    expect(first).toHaveTextContent('folio-1r.png, line 1')
    expect(first).toHaveTextContent('nature')
    expect(second).toHaveTextContent('Untitled page, line 5')
    expect(second).toHaveTextContent('No translation')
    expect(screen.getByText('2 words')).toBeInTheDocument()
    expect(screen.getByText('Machine translations')).toBeInTheDocument()
  })

  it('points to the history when there are no words yet', async () => {
    stubFetch(Response.json([]))
    renderWithProviders(<VocabularyList />)

    expect(await screen.findByText('No words yet')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Open history' })).toHaveAttribute(
      'href',
      '/dashboard/history'
    )
  })

  it('asks a guest to sign in', async () => {
    stubFetch(Response.json({ detail: 'Sign in to continue.' }, { status: 401 }))
    renderWithProviders(<VocabularyList />)

    expect(await screen.findByText('Sign in to keep a vocabulary')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Sign in' })).toHaveAttribute('href', '/sign-in')
  })

  it('says why the list could not load', async () => {
    stubFetch(Response.json({ detail: 'Database is locked' }, { status: 500 }))
    renderWithProviders(<VocabularyList />)

    const alert = await screen.findByRole('alert')
    expect(alert).toHaveTextContent('Could not load your vocabulary')
    expect(alert).toHaveTextContent('Database is locked')
  })
})
