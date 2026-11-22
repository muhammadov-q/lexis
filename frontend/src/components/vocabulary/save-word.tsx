'use client'

import { BookmarkCheckIcon, BookmarkPlusIcon } from 'lucide-react'
import Link from 'next/link'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useAddToVocabulary, useVocabulary } from '@/hooks/use-vocabulary'
import { headword } from '@/lib/words'

interface SaveWordProps {
  recognitionId: number
  wordIndex: number
  text: string
}

export function SaveWord({ recognitionId, wordIndex, text }: SaveWordProps) {
  const vocabulary = useVocabulary()
  const add = useAddToVocabulary()
  const word = headword(text)

  if (!word || vocabulary.isError) return null
  if (vocabulary.isPending) return <Skeleton aria-hidden className="h-10 w-48 rounded-lg" />

  const saved = vocabulary.data.find((entry) => entry.word === word)

  if (saved) {
    return (
      <p className="flex items-center gap-2 text-sm text-muted-foreground">
        <BookmarkCheckIcon className="size-4 shrink-0 text-foreground" aria-hidden="true" />
        <span>
          In your vocabulary.{' '}
          <Link
            href={`/dashboard/vocabulary/${saved.id}`}
            className="font-medium text-foreground underline underline-offset-4"
          >
            Open it
          </Link>
        </span>
      </p>
    )
  }

  return (
    <div className="space-y-2">
      <Button
        variant="outline"
        disabled={add.isPending}
        onClick={() => add.mutate({ recognition_id: recognitionId, word_index: wordIndex })}
      >
        <BookmarkPlusIcon aria-hidden="true" />
        {add.isPending ? 'Adding…' : 'Add to vocabulary'}
      </Button>
      {add.isError && (
        <p role="alert" className="text-sm text-foreground">
          {add.error.message}
        </p>
      )}
    </div>
  )
}
