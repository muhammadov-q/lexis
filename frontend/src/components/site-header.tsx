import Link from 'next/link'
import { ThemeToggle } from '@/components/theme-toggle'
import { Wordmark } from '@/components/wordmark'

export function SiteHeader() {
  return (
    <header className="mx-auto flex w-full max-w-[1200px] items-center justify-between gap-4 px-4 pt-4 sm:px-6 sm:pt-6">
      <Link
        href="/"
        className="rounded-lg text-xl font-semibold tracking-tight outline-none focus-visible:ring-3 focus-visible:ring-ring/50"
      >
        <Wordmark />
      </Link>
      <ThemeToggle />
    </header>
  )
}
