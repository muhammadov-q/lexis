'use client'

import { InfoIcon } from 'lucide-react'
import { useId } from 'react'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectSeparator,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import type { ModelChoice } from '@/hooks/use-models'
import type { ModelList } from '@/lib/api/models'

type Model = ModelList['models'][number]

export function ModelPicker({
  choice,
  disabled = false,
}: {
  choice: ModelChoice
  disabled?: boolean
}) {
  const id = useId()
  const descriptionId = `${id}-description`
  const { query, models, selected } = choice
  const current = models.find((model) => model.id === selected)
  const readers = models.filter((model) => !model.demo)
  const demos = models.filter((model) => model.demo)

  return (
    <div className="space-y-2">
      <Label htmlFor={id}>Model</Label>
      {query.data ? (
        <div className="flex items-center gap-2">
          <Select value={selected} onValueChange={choice.choose} disabled={disabled}>
            <SelectTrigger
              id={id}
              aria-describedby={current ? descriptionId : undefined}
              className="w-full min-w-0 flex-1"
            >
              <SelectValue>{current?.name}</SelectValue>
            </SelectTrigger>
            <SelectContent className="w-(--radix-select-trigger-width)">
              <ModelGroup label="Read your page" models={readers} />
              {readers.length > 0 && demos.length > 0 && <SelectSeparator />}
              <ModelGroup label="Demos" models={demos} />
            </SelectContent>
          </Select>
          {current && (
            <>
              <span id={descriptionId} className="sr-only">
                {current.description}
              </span>
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="ghost" size="icon" aria-label={`About ${current.name}`}>
                    <InfoIcon />
                  </Button>
                </PopoverTrigger>
                <PopoverContent align="end" className="w-80 gap-1.5">
                  <p className="font-medium">{current.name}</p>
                  <p className="text-muted-foreground">{current.description}</p>
                  {current.demo && (
                    <p className="text-muted-foreground">Demo: it doesn&apos;t read your page.</p>
                  )}
                </PopoverContent>
              </Popover>
            </>
          )}
        </div>
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

function ModelGroup({ label, models }: { label: string; models: Model[] }) {
  if (models.length === 0) return null
  return (
    <SelectGroup>
      <SelectLabel>{label}</SelectLabel>
      {models.map((model) => (
        <SelectItem key={model.id} value={model.id}>
          {model.name}
        </SelectItem>
      ))}
    </SelectGroup>
  )
}
