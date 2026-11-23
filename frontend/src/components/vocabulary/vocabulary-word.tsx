'use client'

import { ArrowLeftIcon, ArrowRightIcon, Trash2Icon } from 'lucide-react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { toast } from 'sonner'
import { ErrorNotice } from '@/components/error-notice'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Chip } from '@/components/ui/chip'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Skeleton } from '@/components/ui/skeleton'
import { useRemoveFromVocabulary, useVocabularyWord } from '@/hooks/use-vocabulary'
import type { VocabularyWord } from '@/lib/api/vocabulary'
import { formatDateTime } from '@/lib/format'
import { NoteForm } from './note-form'

export function VocabularyWordView({ id }: { id: number }) {
  const query = useVocabularyWord(id)

  return (
    <>
      <div className="mb-6 space-y-2">
        <Link
          href="/dashboard/vocabulary"
          className="inline-flex items-center gap-2 rounded-lg text-sm text-muted-foreground outline-none hover:text-foreground focus-visible:ring-2 focus-visible:ring-ring"
        >
          <ArrowLeftIcon className="size-4" aria-hidden="true" />
          Vocabulary
        </Link>

        <div className="flex flex-wrap items-baseline justify-between gap-x-6 gap-y-1">
          <h1 className="min-w-0 truncate text-3xl font-semibold tracking-tight">
            {query.isSuccess ? <span className="font-greek">{query.data.word}</span> : 'Word'}
          </h1>
          {query.isSuccess && (
            <p className="text-muted-foreground">
              Added on {formatDateTime(query.data.created_at)}
            </p>
          )}
        </div>
      </div>

      {query.isPending && <WordSkeleton />}

      {query.isError && (
        <Card>
          <ErrorNotice title="Could not open this word" message={query.error.message} />
        </Card>
      )}

      {query.isSuccess && <WordEntry key={query.data.id} word={query.data} />}
    </>
  )
}

function WordEntry({ word }: { word: VocabularyWord }) {
  const { gloss, source } = word

  return (
    <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-2">
      <Card className="space-y-6">
        <div className="flex items-center justify-between gap-3">
          <p className="font-mono text-sm text-muted-foreground">Meaning</p>
          <Chip>Machine output</Chip>
        </div>

        {gloss ? (
          <dl className="grid grid-cols-[auto_1fr] items-baseline gap-x-6 gap-y-3 rounded-xl bg-muted px-5 py-4">
            <dt className="text-sm text-muted-foreground">Translation</dt>
            <dd className="text-lg font-medium">{gloss.translation}</dd>
            <dt className="text-sm text-muted-foreground">Dictionary form</dt>
            <dd className="font-greek text-lg">{gloss.lemma}</dd>
            <dt className="text-sm text-muted-foreground">Definition</dt>
            <dd>{gloss.definition}</dd>
          </dl>
        ) : (
          <p className="rounded-xl bg-muted px-5 py-4 font-medium">No translation</p>
        )}

        <Link
          href={`/dashboard/history/${source.recognition_id}?word=${source.word_index}`}
          className="inline-flex max-w-full items-center gap-1 text-sm text-muted-foreground underline-offset-4 hover:text-foreground hover:underline"
        >
          <span className="truncate">
            {source.filename || 'Untitled page'}, line {source.line + 1}
          </span>
          <ArrowRightIcon className="size-4 shrink-0" aria-hidden="true" />
        </Link>
      </Card>

      <Card className="space-y-8">
        <NoteForm id="vocabulary-note" entry={word} />
        <RemoveWord word={word} />
      </Card>
    </div>
  )
}

const NO_DESCRIPTION = { 'aria-describedby': undefined }

function RemoveWord({ word }: { word: VocabularyWord }) {
  const [open, setOpen] = useState(false)
  const remove = useRemoveFromVocabulary(word.id)
  const router = useRouter()

  function confirm() {
    remove.mutate(undefined, {
      onSuccess: () => {
        toast.success(`Removed ${word.word}`)
        router.replace('/dashboard/vocabulary')
      },
    })
  }

  return (
    <>
      <Button variant="ghost" className="-ml-3" onClick={() => setOpen(true)}>
        <Trash2Icon aria-hidden="true" />
        Remove word
      </Button>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent showCloseButton={false} {...(word.note ? {} : NO_DESCRIPTION)}>
          <DialogHeader>
            <DialogTitle>
              Remove <span className="font-greek">{word.word}</span>?
            </DialogTitle>
            {word.note && <DialogDescription>Your note is deleted too.</DialogDescription>}
          </DialogHeader>
          {remove.isError && (
            <p role="alert" className="text-sm text-foreground">
              {remove.error.message}
            </p>
          )}
          <div className="flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button disabled={remove.isPending} onClick={confirm}>
              {remove.isPending ? 'Removing…' : 'Remove'}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </>
  )
}

function WordSkeleton() {
  return (
    <div aria-hidden className="grid grid-cols-1 items-start gap-8 lg:grid-cols-2">
      <Card className="space-y-6">
        <div className="flex items-center justify-between">
          <Skeleton className="h-4 w-16" />
          <Skeleton className="h-6 w-28" />
        </div>
        <Skeleton className="h-32 w-full rounded-xl" />
        <Skeleton className="h-4 w-48" />
      </Card>
      <Card className="space-y-8">
        <div className="space-y-3">
          <Skeleton className="h-4 w-10" />
          <Skeleton className="h-20 w-full rounded-xl" />
          <Skeleton className="h-10 w-28 rounded-lg" />
        </div>
        <Skeleton className="h-10 w-36 rounded-lg" />
      </Card>
    </div>
  )
}
