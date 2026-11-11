'use client'

import { ChevronLeftIcon, ChevronRightIcon } from 'lucide-react'
import { Annotation } from '@/components/ui/annotation'
import { Button } from '@/components/ui/button'
import type { WordSelection } from '@/hooks/use-word-selection'
import { formatPercent } from '@/lib/format'
import type { ImageSource } from '@/lib/image-file'
import { confidenceOf, hasBox, positionOf, type Word } from '@/lib/words'
import { Loupe } from './loupe'

interface WordDetailsProps {
  words: Word[]
  selection: WordSelection
  image?: ImageSource
}

export function WordDetails({ words, selection, image }: WordDetailsProps) {
  const { selected, select } = selection
  const word = selected === null ? undefined : words[selected]

  if (selected === null || word === undefined) {
    return (
      <div className="space-y-4">
        <div className="grid aspect-[3/1] place-items-center rounded-2xl bg-muted px-6 text-center">
          <div className="space-y-1">
            <p className="font-medium">Pick a word</p>
            <p className="text-sm text-muted-foreground">
              Click a word in the transcript or a box on the page.
            </p>
          </div>
        </div>
        <KeyHint>
          Or press <Kbd>→</Kbd> to start with the first word.
        </KeyHint>
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

      {gloss ? (
        <dl className="grid grid-cols-[auto_1fr] items-baseline gap-x-6 gap-y-3 rounded-xl bg-muted px-5 py-4">
          <Entry term="Translation">
            <span className="text-lg font-medium">{gloss.translation}</span>
          </Entry>
          <Entry term="Dictionary form">
            <span className="font-greek text-lg">{gloss.lemma}</span>
          </Entry>
          <Entry term="Definition">{gloss.definition}</Entry>
          <Entry term="Confidence">
            {confidence === null ? (
              <span className="text-sm">
                Not confirmed. kraken read this word differently, so check it on the page.
              </span>
            ) : (
              <span className="font-mono text-sm tabular-nums">{formatPercent(confidence)}</span>
            )}
          </Entry>
        </dl>
      ) : (
        <div className="space-y-1 rounded-xl bg-muted px-5 py-4">
          <p className="font-medium">No translation yet</p>
          <p className="text-sm text-muted-foreground">
            {confidence === null
              ? 'The model has no translation for this word.'
              : `The model read this word with ${formatPercent(confidence)} confidence but has no translation for it.`}
          </p>
        </div>
      )}

      <KeyHint>
        <Kbd>←</Kbd> <Kbd>→</Kbd> move between words, <Kbd>Esc</Kbd> clears
      </KeyHint>
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

function KeyHint({ children }: { children: React.ReactNode }) {
  return <p className="text-caption text-muted-foreground pointer-coarse:hidden">{children}</p>
}

function Kbd({ children }: { children: React.ReactNode }) {
  return (
    <kbd className="rounded bg-muted px-1.5 py-0.5 font-mono text-caption text-foreground">
      {children}
    </kbd>
  )
}
