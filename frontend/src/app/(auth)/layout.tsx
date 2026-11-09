import { Wordmark } from '@/components/wordmark'

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <main className="mx-auto flex w-full max-w-md flex-1 flex-col justify-center gap-8 px-4 py-12">
      <p className="text-xl font-semibold tracking-tight">
        <Wordmark />
      </p>
      {children}
    </main>
  )
}
