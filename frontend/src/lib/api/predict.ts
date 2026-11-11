import { apiRequest } from './client'
import type { paths } from './schema'

type PredictOperation = paths['/predict']['post']
type PredictForm = NonNullable<PredictOperation['requestBody']>['content']['multipart/form-data']

export type Prediction = PredictOperation['responses'][200]['content']['application/json']

const IMAGE_FIELD: keyof PredictForm = 'image'
const MODEL_FIELD: keyof PredictForm = 'model'

export function predictPage(
  file: File,
  { model, signal }: { model?: string; signal?: AbortSignal } = {}
): Promise<Prediction> {
  const body = new FormData()
  body.append(IMAGE_FIELD, file)
  if (model) body.append(MODEL_FIELD, model)

  return apiRequest<Prediction>('/predict', {
    method: 'POST',
    body,
    signal,
    fallback: 'The model could not read this image.',
  })
}
