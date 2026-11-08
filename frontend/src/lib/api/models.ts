import { apiRequest } from './client'
import type { paths } from './schema'

export type ModelList = paths['/models']['get']['responses'][200]['content']['application/json']

export function listModels(signal?: AbortSignal): Promise<ModelList> {
  return apiRequest<ModelList>('/models', {
    signal,
    fallback: 'Could not load the models.',
  })
}
