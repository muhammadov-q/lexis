'use client'

import { useEffect, useRef, useState } from 'react'
import { cn } from '@/lib/utils'
import { Annotation } from '@/components/ui/annotation'
import { Card } from '@/components/ui/card'
import { Chip } from '@/components/ui/chip'
import type { WordSelection } from '@/hooks/use-word-selection'
import { groupLines, type Word } from '@/lib/words'

interface TranscriptProps {
  words: Word[]
  selection: WordSelection
}

export function Transcript({ words, selection }: TranscriptProps) {
  const [withTranslation, setWithTranslation] = useState(false)
  const listRef = useRef<HTMLOListElement>(null)
  const lines = groupLines(words)
  const translated =
    words.some((word) => word.gloss !== null) || lines.some((line) => line.translation !== null)
  const interlinear = translated && withTranslation

  useEffect(() => {
    const list = listRef.current
    if (!list || selection.selected === null || !list.contains(document.activeElement)) return
    list.querySelector<HTMLElement>(`[data-index="${selection.selected}"]`)?.focus()
  }, [selection.selected])

  return (
    <Card role="region" aria-label="Transcript" className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="space-y-0.5">
          <p className="font-medium">Transcript</p>
          <Annotation>
            {count(words.length, 'word')} on {count(lines.length, 'line')}
          </Annotation>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          {translated && (
            <div role="group" aria-label="Transcript view" className="flex rounded-lg bg-muted p-1">
              <ViewButton pressed={!withTranslation} onClick={() => setWithTranslation(false)}>
                Original
              </ViewButton>
              <ViewButton pressed={withTranslation} onClick={() => setWithTranslation(true)}>
                With translation
              </ViewButton>
            </div>
          )}
          <Chip>Machine output</Chip>
        </div>
      </div>

      {lines.length === 0 ? (
        <p className="text-muted-foreground">The model found no words on this page.</p>
      ) : (
        <ol ref={listRef} className={interlinear ? 'space-y-4' : 'space-y-2'}>
          {lines.map((line) => {
            const lineTranslation = interlinear ? line.translation : null
            const wordGlosses = interlinear && lineTranslation === null
            return (
              <li key={line.number} className="grid grid-cols-[2rem_1fr] items-baseline gap-2">
                <Annotation>
                  <span className="sr-only">Line </span>
                  {line.number}
                </Annotation>
                <div className="space-y-1">
                  <p
                    className={cn(
                      'flex flex-wrap font-greek leading-relaxed',
                      wordGlosses ? 'gap-x-2 gap-y-2' : 'gap-x-1 gap-y-0.5'
                    )}
                  >
                    {line.words.map(({ word, index }) => {
                      const isSelected = index === selection.selected
                      return (
                        <button
                          key={index}
                          type="button"
                          data-index={index}
                          aria-pressed={isSelected}
                          onClick={() => selection.select(isSelected ? null : index)}
                          onMouseEnter={() => selection.hover(index)}
                          onMouseLeave={() => selection.hover(null)}
                          className={cn(
                            'rounded-md px-0.5 text-left outline-none focus-visible:ring-2 focus-visible:ring-ring',
                            wordGlosses && 'flex flex-col px-1.5 py-0.5',
                            isSelected
                              ? 'bg-primary text-primary-foreground'
                              : index === selection.hovered
                                ? 'bg-muted'
                                : 'hover:bg-muted'
                          )}
                        >
                          {word.text}
                          {wordGlosses && ' '}
                          {wordGlosses && (
                            <span
                              className={cn(
                                'font-sans text-caption',
                                isSelected ? 'text-primary-foreground/70' : 'text-muted-foreground'
                              )}
                            >
                              {word.gloss?.translation ?? '·'}
                            </span>
                          )}
                        </button>
                      )
                    })}
                  </p>
                  {lineTranslation && <p className="text-muted-foreground">{lineTranslation}</p>}
                </div>
              </li>
            )
          })}
        </ol>
      )}
    </Card>
  )
}

function ViewButton({
  pressed,
  onClick,
  children,
}: {
  pressed: boolean
  onClick: () => void
  children: React.ReactNode
}) {
  return (
    <button
      type="button"
      aria-pressed={pressed}
      onClick={onClick}
      className={cn(
        'rounded-md px-3 py-1 text-xs font-medium outline-none focus-visible:ring-2 focus-visible:ring-ring',
        pressed ? 'bg-card text-foreground' : 'text-muted-foreground hover:text-foreground'
      )}
    >
      {children}
    </button>
  )
}

function count(n: number, noun: string): string {
  return `${n} ${noun}${n === 1 ? '' : 's'}`
}
