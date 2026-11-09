import { cn } from '@/lib/utils'

export const APP_NAME = 'Λέξις'

export function Wordmark({ className }: { className?: string }) {
  return (
    <span lang="el" className={cn('font-greek', className)}>
      {APP_NAME}
    </span>
  )
}
