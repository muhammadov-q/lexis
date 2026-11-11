import type { ModelList } from '@/lib/api/models'

export function makeModelList(overrides: Partial<ModelList> = {}): ModelList {
  return {
    models: [
      {
        id: 'kraken',
        name: 'Kraken, fine-tuned on Eparchos',
        description: 'Reads 16th-century Greek handwriting.',
      },
      {
        id: 'kraken-base',
        name: 'Kraken, not fine-tuned',
        description: 'The general PP-OCRv6 model, before we fine-tuned it.',
      },
      {
        id: 'dummy',
        name: 'Mock digits',
        description: 'Ignores the page and returns the same six digits. For testing.',
      },
    ],
    default_model: 'kraken',
    ...overrides,
  }
}
