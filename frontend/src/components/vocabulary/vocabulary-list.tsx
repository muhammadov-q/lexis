'use client'

import Link from 'next/link'
import { ErrorNotice } from '@/components/error-notice'
import { Annotation } from '@/components/ui/annotation'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Chip } from '@/components/ui/chip'
import { Skeleton } from '@/components/ui/skeleton'
import { useVocabulary } from '@/hooks/use-vocabulary'
import { ApiError } from '@/lib/api/client'
import type { VocabularyWord } from '@/lib/api/vocabulary'

export function VocabularyList() {
  const vocabulary = useVocabulary()

  if (vocabulary.isPending) return <VocabularySkeleton />

  if (vocabulary.error instanceof ApiError && vocabulary.error.status === 401) {
    return (
      <Card className="space-y-4">
        <p className="font-medium">Sign in to keep a vocabulary</p>
        <p className="text-muted-foreground">
          Words you add from your saved pages are kept with your account.
        </p>
        <Button asChild>
          <Link href="/sign-in">Sign in</Link>
        </Button>
      </Card>
    )
  }

  if (vocabulary.isError) {
    return (
      <Card>
        <ErrorNotice title="Could not load your vocabulary" message={vocabulary.error.message} />
      </Card>
    )
  }

  if (vocabulary.data.length === 0) {
    return (
      <Card className="space-y-4">
        <p className="font-medium">No words yet</p>
        <p className="text-muted-foreground">
          Pick a word on a page you read and add it to your vocabulary.
        </p>
        <Button asChild>
          <Link href="/dashboard/history">Open your history</Link>
        </Button>
      </Card>
    )
  }

  const count = vocabulary.data.length

  return (
    <Card className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="font-medium">
          {count} {count === 1 ? 'word' : 'words'}
        </p>
        <Chip>Machine translations</Chip>
      </div>

      <ul aria-label="Saved words" className="space-y-2">
        {vocabulary.data.map((word) => (
          <li key={word.id}>
            <Link
              href={`/dashboard/vocabulary/${word.id}`}
              className="flex items-center justify-between gap-4 rounded-xl bg-muted px-4 py-3 outline-none hover:bg-muted/60 focus-visible:ring-2 focus-visible:ring-ring"
            >
              <span className="min-w-0 space-y-0.5">
                <span className="block truncate font-greek text-lg font-medium">{word.word}</span>
                <Annotation className="block truncate">{sourceOf(word)}</Annotation>
              </span>
              <span className="max-w-[50%] shrink-0 truncate text-right text-muted-foreground">
                {word.gloss?.translation || 'No translation'}
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </Card>
  )
}

function sourceOf(word: VocabularyWord): string {
  return `${word.source.filename || 'Untitled page'}, line ${word.source.line + 1}`
}

function VocabularySkeleton() {
  return (
    <Card aria-hidden className="space-y-6">
      <div className="flex items-center justify-between">
        <Skeleton className="h-5 w-20" />
        <Skeleton className="h-6 w-40" />
      </div>
      <div className="space-y-2">
        {['w-28', 'w-36', 'w-24'].map((width) => (
          <div
            key={width}
            className="flex items-center justify-between rounded-xl bg-muted px-4 py-3"
          >
            <div className="space-y-2">
              <Skeleton className={`h-6 ${width}`} />
              <Skeleton className="h-3 w-32" />
            </div>
            <Skeleton className="h-4 w-20" />
          </div>
        ))}
      </div>
    </Card>
  )
}
