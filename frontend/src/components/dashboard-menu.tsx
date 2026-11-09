'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { ArrowUpRight, ChevronLeft, ChevronRight, History, ScanLine } from 'lucide-react'
import { useState } from 'react'
import { AccountCard } from '@/components/auth/account-card'
import { ThemeToggle } from '@/components/theme-toggle'
import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import { Wordmark } from '@/components/wordmark'
import { cn } from '@/lib/utils'

const REPO_URL = 'https://github.com/muhammadov-q/lexis'

const PAGES = [
  { href: '/dashboard', label: 'Read', icon: ScanLine, exact: true },
  { href: '/dashboard/history', label: 'History', icon: History, exact: false },
] as const

const PROJECT_LINKS = [
  { href: `${REPO_URL}/tree/develop/docs`, label: 'Docs' },
  { href: REPO_URL, label: 'Source' },
] as const

export function DashboardMenu() {
  const [expanded, setExpanded] = useState(true)
  const pathname = usePathname()

  return (
    <aside aria-label="Dashboard sidebar" className="sticky top-0 h-svh shrink-0 p-2 pr-0">
      <div
        className={cn(
          'flex h-full w-16 flex-col gap-6 rounded-2xl bg-card p-2',
          expanded && 'lg:w-64'
        )}
      >
        <div
          className={cn(
            'hidden min-h-12 items-center lg:flex',
            expanded ? 'justify-between pl-3' : 'justify-center'
          )}
        >
          {expanded && (
            <Link
              href="/dashboard"
              className="rounded-lg text-lg font-semibold tracking-tight outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <Wordmark />
            </Link>
          )}
          <Button
            variant="ghost"
            size={expanded ? 'icon-sm' : 'icon'}
            aria-label={expanded ? 'Collapse navigation' : 'Expand navigation'}
            aria-expanded={expanded}
            aria-controls="dashboard-navigation"
            onClick={() => setExpanded((value) => !value)}
          >
            {expanded ? (
              <ChevronLeft />
            ) : (
              <>
                <span
                  lang="el"
                  className="font-greek text-base font-semibold tracking-tight text-foreground group-hover/button:hidden group-focus-visible/button:hidden"
                >
                  Λ
                </span>
                <ChevronRight className="hidden group-hover/button:block group-focus-visible/button:block" />
              </>
            )}
          </Button>
        </div>

        <TooltipProvider>
          <nav
            id="dashboard-navigation"
            aria-label="Dashboard navigation"
            className="flex flex-col gap-1"
          >
            {PAGES.map(({ href, label, icon: Icon, exact }) => {
              const current = exact ? pathname === href : pathname.startsWith(href)
              return (
                <Tooltip key={href}>
                  <TooltipTrigger asChild>
                    <Link
                      href={href}
                      aria-current={current ? 'page' : undefined}
                      className={cn(
                        'flex min-h-11 items-center justify-center gap-3 rounded-xl px-3 text-muted-foreground outline-none hover:bg-muted hover:text-foreground focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-card',
                        expanded && 'lg:justify-start',
                        current &&
                          'bg-primary font-medium text-primary-foreground hover:bg-primary/90 hover:text-primary-foreground'
                      )}
                    >
                      <Icon className="size-5 shrink-0" aria-hidden="true" />
                      <span className={expanded ? 'sr-only lg:not-sr-only' : 'sr-only'}>
                        {label}
                      </span>
                    </Link>
                  </TooltipTrigger>
                  <TooltipContent
                    side="right"
                    sideOffset={12}
                    className={expanded ? 'lg:hidden' : undefined}
                  >
                    {label}
                  </TooltipContent>
                </Tooltip>
              )
            })}
          </nav>
        </TooltipProvider>

        <div className="mt-auto flex flex-col gap-2">
          <div className={cn('flex items-center justify-center', expanded && 'lg:justify-between')}>
            {expanded && (
              <div className="hidden gap-4 pl-3 lg:flex">
                {PROJECT_LINKS.map(({ href, label }) => (
                  <a
                    key={href}
                    href={href}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-0.5 rounded-md font-mono text-caption text-muted-foreground outline-none hover:text-foreground focus-visible:ring-2 focus-visible:ring-ring"
                  >
                    {label} <ArrowUpRight className="size-3" aria-hidden="true" />
                    <span className="sr-only">(opens in a new tab)</span>
                  </a>
                ))}
              </div>
            )}
            <ThemeToggle size="icon-sm" />
          </div>

          <AccountCard expanded={expanded} />
        </div>
      </div>
    </aside>
  )
}
