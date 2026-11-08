import { API_URL, apiRequest } from './client'
import type { paths } from './schema'

type CreateOperation = paths['/recognitions']['post']
type RecognitionForm = NonNullable<CreateOperation['requestBody']>['content']['multipart/form-data']

export type Recognition = CreateOperation['responses'][201]['content']['application/json']

const IMAGE_FIELD: keyof RecognitionForm = 'image'
const MODEL_FIELD: keyof RecognitionForm = 'model'

export function recognizePage(
  file: File,
  { model, signal }: { model?: string; signal?: AbortSignal } = {}
): Promise<Recognition> {
  const body = new FormData()
  body.append(IMAGE_FIELD, file)
  if (model) body.append(MODEL_FIELD, model)
  return apiRequest<Recognition>('/recognitions', {
    method: 'POST',
    body,
    signal,
    fallback: 'The model could not read this image.',
  })
}

export function listRecognitions(signal?: AbortSignal): Promise<Recognition[]> {
  return apiRequest<Recognition[]>('/recognitions', {
    signal,
    fallback: 'Could not load your history.',
  })
}

export function getRecognition(id: number, signal?: AbortSignal): Promise<Recognition> {
  return apiRequest<Recognition>(`/recognitions/${id}`, {
    signal,
    fallback: 'Could not load this result.',
  })
}

export function recognitionImageUrl(id: number): string {
  return `${API_URL}/recognitions/${id}/image`
}
