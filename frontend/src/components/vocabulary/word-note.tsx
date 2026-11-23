'use client'

import { ArrowUpRightIcon } from 'lucide-react'
import Link from 'next/link'
import { Skeleton } from '@/components/ui/skeleton'
import { useVocabulary } from '@/hooks/use-vocabulary'
import { headword } from '@/lib/words'
import { NoteForm } from './note-form'

interface WordNoteProps {
  recognitionId: number
  wordIndex: number
  text: string
}

export function WordNote({ recognitionId, wordIndex, text }: WordNoteProps) {
  const vocabulary = useVocabulary()
  const word = headword(text)

  if (!word || vocabulary.isError) return null
  if (vocabulary.isPending) {
    return (
      <div aria-hidden className="space-y-3">
        <Skeleton className="h-4 w-10" />
        <Skeleton className="h-20 w-full rounded-xl" />
        <Skeleton className="h-10 w-28 rounded-lg" />
      </div>
    )
  }

  const entry = vocabulary.data.find((saved) => saved.word === word)

  return (
    <NoteForm
      id="word-note"
      entry={entry}
      source={{ recognition_id: recognitionId, word_index: wordIndex }}
      aside={
        entry && (
          <Link
            href={`/dashboard/vocabulary/${entry.id}`}
            className="inline-flex items-center gap-1 text-sm text-muted-foreground underline-offset-4 hover:text-foreground hover:underline"
          >
            In vocabulary
            <ArrowUpRightIcon className="size-4" aria-hidden="true" />
          </Link>
        )
      }
    />
  )
}
