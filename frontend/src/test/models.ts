import type { ModelList } from '@/lib/api/models'

export function makeModelList(overrides: Partial<ModelList> = {}): ModelList {
  return {
    models: [
      {
        id: 'kraken',
        name: 'Greek handwriting',
        description: 'Reads the page in about 10 seconds and puts a box around every word.',
        demo: false,
      },
      {
        id: 'kraken-base',
        name: 'Untrained reader, for comparison',
        description: 'The same reader before it learned these manuscripts.',
        demo: false,
      },
      {
        id: 'dummy',
        name: 'Test output',
        description: 'Always returns the same six digits, whatever you upload.',
        demo: true,
      },
    ],
    default_model: 'kraken',
    ...overrides,
  }
}
