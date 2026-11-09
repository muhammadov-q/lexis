import type { Metadata } from 'next'
import { PhoneCapture } from '@/components/phone-capture/phone-capture'
import { Wordmark } from '@/components/wordmark'

export const metadata: Metadata = {
  title: 'Take a photo',
  robots: { index: false },
}

export default async function CapturePage({ params }: PageProps<'/capture/[id]'>) {
  const { id } = await params

  return (
    <main className="mx-auto flex w-full max-w-md flex-1 flex-col gap-8 px-4 pt-6 pb-8">
      <p className="text-xl font-semibold tracking-tight">
        <Wordmark />
      </p>
      <PhoneCapture sessionId={id} />
    </main>
  )
}
