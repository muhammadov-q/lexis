import { afterEach, describe, expect, it, vi } from 'vitest'
import { makeRecognition } from '@/test/recognitions'
import {
  getRecognition,
  listRecognitions,
  recognitionImageUrl,
  recognizePage,
} from './recognitions'

afterEach(() => {
  vi.unstubAllGlobals()
})

function stubFetch(body: unknown) {
  const fetch = vi.fn().mockResolvedValue(Response.json(body))
  vi.stubGlobal('fetch', fetch)
  return fetch
}

const call = (fetch: ReturnType<typeof vi.fn>) => fetch.mock.calls[0] as [string, RequestInit]

describe('recognitions API', () => {
  it('posts the page as the multipart field `image`', async () => {
    const recognition = makeRecognition()
    const fetch = stubFetch(recognition)
    const page = new File(['bytes'], 'folio-1r.png', { type: 'image/png' })
    const controller = new AbortController()

    await expect(recognizePage(page, { signal: controller.signal })).resolves.toEqual(recognition)

    const [url, init] = call(fetch)
    expect(url).toBe('/api/recognitions')
    expect(init.method).toBe('POST')
    expect((init.body as FormData).get('image')).toBe(page)
    expect((init.body as FormData).has('model')).toBe(false)
    expect(init.signal).toBe(controller.signal)
  })

  it('names the chosen model in the field `model`', async () => {
    const fetch = stubFetch(makeRecognition())

    await recognizePage(new File(['bytes'], 'folio-1r.png', { type: 'image/png' }), {
      model: 'kraken',
    })

    expect((call(fetch)[1].body as FormData).get('model')).toBe('kraken')
  })

  it('lists the history', async () => {
    const fetch = stubFetch([makeRecognition()])

    await expect(listRecognitions()).resolves.toHaveLength(1)
    expect(call(fetch)[0]).toBe('/api/recognitions')
    expect(call(fetch)[1].method).toBe('GET')
  })

  it('gets one result by id', async () => {
    const fetch = stubFetch(makeRecognition({ id: 7 }))

    await expect(getRecognition(7)).resolves.toMatchObject({ id: 7 })
    expect(call(fetch)[0]).toBe('/api/recognitions/7')
  })

  it('points at the saved image through the proxy', () => {
    expect(recognitionImageUrl(7)).toBe('/api/recognitions/7/image')
  })
})
