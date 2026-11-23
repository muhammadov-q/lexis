'use client'

import { ChevronLeftIcon, ChevronRightIcon } from 'lucide-react'
import { Annotation } from '@/components/ui/annotation'
import { Button } from '@/components/ui/button'
import { WordNote } from '@/components/vocabulary/word-note'
import type { WordSelection } from '@/hooks/use-word-selection'
import { formatPercent } from '@/lib/format'
import type { ImageSource } from '@/lib/image-file'
import { confidenceOf, hasBox, positionOf, type Word } from '@/lib/words'
import { Loupe } from './loupe'

interface WordDetailsProps {
  words: Word[]
  selection: WordSelection
  image?: ImageSource
  recognitionId?: number
}

export function WordDetails({ words, selection, image, recognitionId }: WordDetailsProps) {
  const { selected, select } = selection
  const word = selected === null ? undefined : words[selected]

  if (selected === null || word === undefined) {
    return (
      <div className="grid aspect-[3/1] place-items-center rounded-2xl bg-muted px-6 text-center">
        <div className="space-y-1">
          <p className="font-medium">Pick a word</p>
          <p className="text-sm text-muted-foreground">On the page or in the transcript.</p>
        </div>
      </div>
    )
  }

  const position = positionOf(words, selected)
  const { gloss } = word
  const confidence = confidenceOf(word)

  return (
    <section aria-label="Selected word" className="space-y-5">
      {image && hasBox(word.box) && <Loupe image={image} box={word.box} word={word.text} />}

      <div className="flex items-end justify-between gap-4">
        <div className="min-w-0 space-y-2">
          <p className="font-greek text-heading leading-none font-medium break-words">
            {word.text}
          </p>
          <Annotation className="block">
            Line {position.line}, word {position.word}
          </Annotation>
        </div>
        <div className="flex shrink-0 gap-1">
          <Button
            variant="outline"
            size="icon-sm"
            aria-label="Previous word"
            disabled={selected === 0}
            onClick={() => select(selected - 1)}
          >
            <ChevronLeftIcon />
          </Button>
          <Button
            variant="outline"
            size="icon-sm"
            aria-label="Next word"
            disabled={selected === words.length - 1}
            onClick={() => select(selected + 1)}
          >
            <ChevronRightIcon />
          </Button>
        </div>
      </div>

      <dl className="grid grid-cols-[auto_1fr] items-baseline gap-x-6 gap-y-3 rounded-xl bg-muted px-5 py-4">
        {gloss ? (
          <>
            <Entry term="Translation">
              <span className="text-lg font-medium">{gloss.translation}</span>
            </Entry>
            <Entry term="Dictionary form">
              <span className="font-greek text-lg">{gloss.lemma}</span>
            </Entry>
            <Entry term="Definition">{gloss.definition}</Entry>
          </>
        ) : (
          <Entry term="Translation">
            <span className="text-muted-foreground">None</span>
          </Entry>
        )}
        <Entry term="Confidence">
          {confidence === null ? (
            <span className="text-sm">Not confirmed by kraken</span>
          ) : (
            <span className="font-mono text-sm tabular-nums">{formatPercent(confidence)}</span>
          )}
        </Entry>
      </dl>

      {recognitionId !== undefined && (
        <WordNote
          key={selected}
          recognitionId={recognitionId}
          wordIndex={selected}
          text={word.text}
        />
      )}
    </section>
  )
}

function Entry({ term, children }: { term: string; children: React.ReactNode }) {
  return (
    <>
      <dt className="text-sm text-muted-foreground">{term}</dt>
      <dd>{children}</dd>
    </>
  )
}
