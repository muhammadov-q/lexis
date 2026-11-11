'use client'

import { useId } from 'react'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import type { ModelChoice } from '@/hooks/use-models'

export function ModelPicker({
  choice,
  disabled = false,
}: {
  choice: ModelChoice
  disabled?: boolean
}) {
  const id = useId()
  const { query, models, selected } = choice

  return (
    <div className="space-y-2">
      <Label htmlFor={id}>Model</Label>
      {query.data ? (
        <Select value={selected} onValueChange={choice.choose} disabled={disabled}>
          <SelectTrigger id={id} className="w-full">
            <SelectValue>{models.find((model) => model.id === selected)?.name}</SelectValue>
          </SelectTrigger>
          <SelectContent className="w-(--radix-select-trigger-width)">
            {models.map((model) => (
              <SelectItem key={model.id} value={model.id}>
                <span className="flex flex-col gap-0.5">
                  <span className="font-medium">{model.name}</span>
                  <span className="text-xs text-muted-foreground">{model.description}</span>
                </span>
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      ) : query.isPending ? (
        <Skeleton aria-hidden className="h-11 w-full rounded-xl" />
      ) : (
        <div className="flex items-center justify-between gap-3 rounded-xl bg-muted py-2 pr-2 pl-4">
          <p className="text-sm text-muted-foreground">
            Couldn&apos;t load the models. The default model reads the page.
          </p>
          <Button variant="ghost" size="sm" onClick={() => void query.refetch()}>
            Reload
          </Button>
        </div>
      )}
    </div>
  )
}
