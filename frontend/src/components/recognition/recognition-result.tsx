'use client'

import { Annotation } from '@/components/ui/annotation'
import { Chip } from '@/components/ui/chip'
import { WordDetails } from '@/components/transcript/word-details'
import type { WordSelection } from '@/hooks/use-word-selection'
import type { Prediction } from '@/lib/api/predict'
import { formatDuration } from '@/lib/format'
import type { ImageSource } from '@/lib/image-file'

const PLACEHOLDER_MODEL_VERSION = /^(mock|eparchos-.+)$/

interface RecognitionResultProps {
  recognition: Prediction
  selection: WordSelection
  image?: ImageSource
  durationMs?: number
}

export function RecognitionResult({
  recognition,
  selection,
  image,
  durationMs,
}: RecognitionResultProps) {
  const { predictions: words, model_version: modelVersion } = recognition
  const isPlaceholder = PLACEHOLDER_MODEL_VERSION.test(modelVersion)

  return (
    <section aria-label="Model response" className="space-y-6">
      <div className="flex items-center justify-between gap-3">
        <p className="font-mono text-sm text-muted-foreground">Word</p>
        <Chip>Machine output</Chip>
      </div>

      {words.length > 0 ? (
        <WordDetails words={words} selection={selection} image={image} />
      ) : (
        <div className="space-y-1 rounded-xl bg-muted px-5 py-4">
          <p className="text-lg font-medium">No words found</p>
          <p className="text-sm text-muted-foreground">
            Try a closer photo with dark ink on plain paper.
          </p>
        </div>
      )}

      <div className="space-y-1 text-sm text-muted-foreground">
        <p>
          Read by <span className="font-mono text-foreground">{modelVersion}</span>
          {durationMs !== undefined && (
            <>
              {' '}
              in <Annotation>{formatDuration(durationMs)}</Annotation>
            </>
          )}
        </p>
        {isPlaceholder && (
          <p>Placeholder output. This model returns the same words for every page.</p>
        )}
      </div>
    </section>
  )
}
