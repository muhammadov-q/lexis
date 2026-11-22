import type { VocabularyWord } from '@/lib/api/vocabulary'

export function makeVocabularyWord(overrides: Partial<VocabularyWord> = {}): VocabularyWord {
  return {
    id: 1,
    created_at: '2026-11-22T12:00:00Z',
    word: 'Φύσις',
    gloss: { lemma: 'φύσις', translation: 'nature', definition: 'noun: nature' },
    note: '',
    source: { recognition_id: 7, filename: 'folio-1r.png', line: 0, word_index: 0 },
    ...overrides,
  }
}
