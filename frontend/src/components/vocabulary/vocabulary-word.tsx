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
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { Textarea } from '@/components/ui/textarea'
import { useRemoveFromVocabulary, useUpdateNote, useVocabularyWord } from '@/hooks/use-vocabulary'
import type { VocabularyWord } from '@/lib/api/vocabulary'
import { formatDateTime } from '@/lib/format'

// The backend's limit, so the box stops where the API would refuse
const MAX_NOTE_LENGTH = 2000

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
          <div className="space-y-1 rounded-xl bg-muted px-5 py-4">
            <p className="font-medium">No translation</p>
            <p className="text-sm text-muted-foreground">
              The model that read this page gave no translation for this word.
            </p>
          </div>
        )}

        <div className="flex flex-wrap items-center justify-between gap-x-6 gap-y-2 text-sm">
          <p className="min-w-0 truncate text-muted-foreground">
            From {source.filename || 'Untitled page'}, line {source.line + 1}
          </p>
          <Link
            href={`/dashboard/history/${source.recognition_id}?word=${source.word_index}`}
            className="inline-flex items-center gap-1 font-medium underline underline-offset-4"
          >
            Show on the page
            <ArrowRightIcon className="size-4" aria-hidden="true" />
          </Link>
        </div>
      </Card>

      <Card className="space-y-8">
        <NoteEditor word={word} />
        <RemoveWord word={word} />
      </Card>
    </div>
  )
}

function NoteEditor({ word }: { word: VocabularyWord }) {
  const [draft, setDraft] = useState(word.note)
  const update = useUpdateNote(word.id)
  const changed = draft.trim() !== word.note

  return (
    <form
      className="space-y-3"
      onSubmit={(event) => {
        event.preventDefault()
        update.mutate(draft)
      }}
    >
      <Label htmlFor="vocabulary-note">Your note</Label>
      <Textarea
        id="vocabulary-note"
        value={draft}
        maxLength={MAX_NOTE_LENGTH}
        placeholder="What helps you remember this word?"
        onChange={(event) => setDraft(event.target.value)}
      />
      <div className="flex flex-wrap items-center gap-4">
        <Button type="submit" disabled={!changed || update.isPending}>
          {update.isPending ? 'Saving…' : 'Save note'}
        </Button>
        <p aria-live="polite" className="text-sm text-muted-foreground">
          {update.isSuccess && !changed ? 'Note saved' : ''}
        </p>
      </div>
      {update.isError && (
        <p role="alert" className="text-sm text-foreground">
          {update.error.message}
        </p>
      )}
    </form>
  )
}

function RemoveWord({ word }: { word: VocabularyWord }) {
  const [open, setOpen] = useState(false)
  const remove = useRemoveFromVocabulary(word.id)
  const router = useRouter()

  function confirm() {
    remove.mutate(undefined, {
      onSuccess: () => {
        toast.success(`Removed ${word.word} from your vocabulary`)
        router.replace('/dashboard/vocabulary')
      },
    })
  }

  return (
    <>
      <Button variant="ghost" className="-ml-3" onClick={() => setOpen(true)}>
        <Trash2Icon aria-hidden="true" />
        Remove from vocabulary
      </Button>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent showCloseButton={false}>
          <DialogHeader>
            <DialogTitle>
              Remove <span className="font-greek">{word.word}</span>?
            </DialogTitle>
            <DialogDescription>
              {word.note
                ? 'Your note on this word is deleted too.'
                : 'You can add it again from its page.'}
            </DialogDescription>
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
          <Skeleton className="h-4 w-20" />
          <Skeleton className="h-28 w-full rounded-xl" />
          <Skeleton className="h-10 w-28 rounded-lg" />
        </div>
        <Skeleton className="h-10 w-52 rounded-lg" />
      </Card>
    </div>
  )
}
