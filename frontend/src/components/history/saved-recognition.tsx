'use client'

import { ArrowLeftIcon } from 'lucide-react'
import Link from 'next/link'
import { useState } from 'react'
import { ErrorNotice } from '@/components/error-notice'
import { RecognitionResult } from '@/components/recognition/recognition-result'
import { Transcript } from '@/components/transcript/transcript'
import { PageScan } from '@/components/transcript/page-scan'
import { Card } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useRecognition } from '@/hooks/use-recognitions'
import { useWordSelection } from '@/hooks/use-word-selection'
import { recognitionImageUrl, type Recognition } from '@/lib/api/recognitions'
import { formatDateTime } from '@/lib/format'

export function SavedRecognition({ id, word = null }: { id: number; word?: number | null }) {
  const query = useRecognition(id)

  return (
    <>
      <div className="mb-6 space-y-2">
        <Link
          href="/dashboard/history"
          className="inline-flex items-center gap-2 rounded-lg text-sm text-muted-foreground outline-none hover:text-foreground focus-visible:ring-2 focus-visible:ring-ring"
        >
          <ArrowLeftIcon className="size-4" aria-hidden="true" />
          History
        </Link>

        <div className="flex flex-wrap items-baseline justify-between gap-x-6 gap-y-1">
          <h1 className="min-w-0 truncate text-2xl font-semibold tracking-tight">
            {query.isSuccess ? query.data.filename || 'Untitled page' : 'Result'}
          </h1>
          {query.isSuccess && (
            <p className="text-muted-foreground">Read on {formatDateTime(query.data.created_at)}</p>
          )}
        </div>
      </div>

      {query.isPending && <SavedRecognitionSkeleton />}

      {query.isError && (
        <Card>
          <ErrorNotice title="Could not open this result" message={query.error.message} />
        </Card>
      )}

      {query.isSuccess && <SavedReading key={query.data.id} recognition={query.data} word={word} />}
    </>
  )
}

function SavedReading({ recognition, word }: { recognition: Recognition; word: number | null }) {
  const selection = useWordSelection(recognition, recognition.predictions.length, word)
  const [failed, setFailed] = useState(false)
  const image = recognitionImageUrl(recognition.id)

  return (
    <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-[minmax(0,7fr)_minmax(0,5fr)]">
      <div className="space-y-8">
        <div className="grid min-h-72 place-items-center overflow-hidden rounded-2xl bg-card p-4">
          {failed ? (
            <p className="max-w-xs p-6 text-center text-sm text-muted-foreground">
              This browser can&apos;t show this image (TIFF usually).
            </p>
          ) : (
            <PageScan
              image={image}
              alt={`Saved page ${recognition.filename}`}
              words={recognition.predictions}
              selection={selection}
              onError={() => setFailed(true)}
            />
          )}
        </div>
        <Transcript words={recognition.predictions} selection={selection} />
      </div>
      <Card className="lg:sticky lg:top-8">
        <RecognitionResult
          recognition={recognition}
          selection={selection}
          image={failed ? undefined : image}
          savedId={recognition.id}
        />
      </Card>
    </div>
  )
}

function SavedRecognitionSkeleton() {
  return (
    <div
      aria-hidden
      className="grid grid-cols-1 items-start gap-8 lg:grid-cols-[minmax(0,7fr)_minmax(0,5fr)]"
    >
      <div className="space-y-8">
        <Skeleton className="h-[60vh] rounded-2xl" />
        <Skeleton className="h-64 rounded-2xl" />
      </div>
      <Card className="space-y-6">
        <Skeleton className="h-4 w-16" />
        <Skeleton className="aspect-[3/1] w-full rounded-2xl" />
        <Skeleton className="h-9 w-40" />
        <Skeleton className="h-36 w-full rounded-xl" />
      </Card>
    </div>
  )
}
