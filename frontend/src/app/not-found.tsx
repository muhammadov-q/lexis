import Link from 'next/link'
import { StatusPage } from '@/components/status-page'
import { Button } from '@/components/ui/button'
import { Wordmark } from '@/components/wordmark'

export default function NotFound() {
  return (
    <StatusPage label="404" title="Page not found">
      <p className="mt-4 text-muted-foreground">This page doesn&apos;t exist or has moved.</p>
      <Button asChild className="mt-8">
        <Link href="/">
          <span>
            Back to <Wordmark />
          </span>
        </Link>
      </Button>
    </StatusPage>
  )
}
