import type { Recognition } from '@/lib/api/recognitions'

export function makeRecognition(overrides: Partial<Recognition> = {}): Recognition {
  return {
    id: 1,
    created_at: '2026-10-15T12:00:00Z',
    filename: 'folio-1r.png',
    model_version: 'eparchos-6791',
    predictions: [
      {
        text: 'Φύσις',
        probabilities: { Φύσις: 1 },
        box: { x: 10, y: 20, w: 30, h: 40 },
        line: 0,
        gloss: { lemma: 'φύσις', translation: 'nature', definition: 'noun: nature' },
        line_translation: null,
      },
      {
        text: 'ἐστί,',
        probabilities: { 'ἐστί,': 0.8 },
        box: { x: 50, y: 20, w: 30, h: 40 },
        line: 0,
        gloss: { lemma: 'εἰμί', translation: 'is', definition: 'verb: to be' },
        line_translation: null,
      },
      {
        text: '?',
        probabilities: { '?': 0.4 },
        box: { x: 10, y: 70, w: 30, h: 40 },
        line: 1,
        gloss: null,
        line_translation: null,
      },
    ],
    ...overrides,
  }
}
