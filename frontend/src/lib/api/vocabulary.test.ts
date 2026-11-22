import { afterEach, describe, expect, it, vi } from 'vitest'
import { makeVocabularyWord } from '@/test/vocabulary'
import {
  addToVocabulary,
  getVocabularyWord,
  listVocabulary,
  removeFromVocabulary,
  updateVocabularyNote,
} from './vocabulary'

afterEach(() => {
  vi.unstubAllGlobals()
})

function stubFetch(response: Response) {
  const fetch = vi.fn().mockResolvedValue(response)
  vi.stubGlobal('fetch', fetch)
  return fetch
}

const call = (fetch: ReturnType<typeof vi.fn>) => fetch.mock.calls[0] as [string, RequestInit]

describe('vocabulary API', () => {
  it('lists your words and gets one', async () => {
    const word = makeVocabularyWord()
    const controller = new AbortController()

    let fetch = stubFetch(Response.json([word]))
    await expect(listVocabulary(controller.signal)).resolves.toEqual([word])
    expect(call(fetch)[0]).toBe('/api/vocabulary')
    expect(call(fetch)[1].signal).toBe(controller.signal)

    fetch = stubFetch(Response.json(word))
    await expect(getVocabularyWord(1)).resolves.toEqual(word)
    expect(call(fetch)[0]).toBe('/api/vocabulary/1')
  })

  it('adds a word by its place on a saved page', async () => {
    const fetch = stubFetch(Response.json(makeVocabularyWord(), { status: 201 }))

    await addToVocabulary({ recognition_id: 7, word_index: 3 })

    const [url, init] = call(fetch)
    expect(url).toBe('/api/vocabulary')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body as string)).toEqual({ recognition_id: 7, word_index: 3 })
  })

  it('saves a note and removes a word', async () => {
    let fetch = stubFetch(Response.json(makeVocabularyWord({ note: 'as in physics' })))
    await updateVocabularyNote(1, 'as in physics')
    expect(call(fetch)[0]).toBe('/api/vocabulary/1')
    expect(call(fetch)[1].method).toBe('PATCH')
    expect(JSON.parse(call(fetch)[1].body as string)).toEqual({ note: 'as in physics' })

    fetch = stubFetch(new Response(null, { status: 204 }))
    await expect(removeFromVocabulary(1)).resolves.toBeUndefined()
    expect(call(fetch)[1].method).toBe('DELETE')
  })

  it('passes on what the backend says when a word is already saved', async () => {
    stubFetch(
      Response.json({ detail: 'This word is already in your vocabulary.' }, { status: 409 })
    )

    await expect(addToVocabulary({ recognition_id: 7, word_index: 0 })).rejects.toMatchObject({
      status: 409,
      message: 'This word is already in your vocabulary.',
    })
  })
})
