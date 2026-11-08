import { afterEach, describe, expect, it, vi } from 'vitest'
import { makeModelList } from '@/test/models'
import { listModels } from './models'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('models API', () => {
  it('lists the models this backend can run', async () => {
    const models = makeModelList()
    const fetch = vi.fn().mockResolvedValue(Response.json(models))
    vi.stubGlobal('fetch', fetch)

    await expect(listModels()).resolves.toEqual(models)
    expect(fetch.mock.calls[0][0]).toBe('/api/models')
  })

  it("passes on the backend's reason when it can't list them", async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(Response.json({ detail: 'Not reachable' }, { status: 502 }))
    )

    await expect(listModels()).rejects.toMatchObject({ status: 502, message: 'Not reachable' })
  })
})
