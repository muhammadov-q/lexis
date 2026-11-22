'use client'

import { useRef, useState, type ReactNode } from 'react'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { useAddToVocabulary, useUpdateNote } from '@/hooks/use-vocabulary'
import type { NewVocabularyWord, VocabularyWord } from '@/lib/api/vocabulary'

const MAX_NOTE_LENGTH = 2000

interface NoteFormProps {
  id: string
  entry: VocabularyWord | undefined
  source?: Omit<NewVocabularyWord, 'note'>
  aside?: ReactNode
}

export function NoteForm({ id, entry, source, aside }: NoteFormProps) {
  const [draft, setDraft] = useState(entry?.note ?? '')
  const add = useAddToVocabulary()
  const update = useUpdateNote()
  const saving = add.isPending || update.isPending
  const error = add.error ?? update.error
  const changed = draft.trim() !== (entry?.note ?? '')
  const sent = useRef<string | null>(null)

  function save() {
    if (sent.current === draft.trim()) return
    sent.current = draft.trim()
    const retry = { onError: () => (sent.current = null) }

    if (entry) update.mutate({ id: entry.id, note: draft }, retry)
    else if (source) add.mutate({ ...source, note: draft }, retry)
  }

  return (
    <form
      className="space-y-3"
      onSubmit={(event) => {
        event.preventDefault()
        save()
      }}
    >
      <div className="flex items-center justify-between gap-3">
        <Label htmlFor={id}>Note</Label>
        {aside}
      </div>
      <Textarea
        id={id}
        value={draft}
        maxLength={MAX_NOTE_LENGTH}
        placeholder="Add a note"
        className="min-h-20"
        onChange={(event) => setDraft(event.target.value)}
        onBlur={() => changed && save()}
      />
      <div className="flex flex-wrap items-center gap-4">
        <Button type="submit" disabled={saving || (entry !== undefined && !changed)}>
          {saving ? 'Saving…' : entry ? 'Save note' : 'Save word'}
        </Button>
        <p aria-live="polite" className="text-sm text-muted-foreground">
          {(add.isSuccess || update.isSuccess) && !changed ? 'Saved' : ''}
        </p>
      </div>
      {error && (
        <p role="alert" className="text-sm text-foreground">
          {error.message}
        </p>
      )}
    </form>
  )
}
