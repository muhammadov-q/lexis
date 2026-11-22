import type { Metadata } from 'next'
import { VocabularyList } from '@/components/vocabulary/vocabulary-list'

export const metadata: Metadata = { title: 'Vocabulary' }

export default function VocabularyPage() {
  return (
    <>
      <div className="mb-10">
        <h1 className="text-3xl font-semibold tracking-tight lg:text-4xl">Vocabulary</h1>
        <p className="mt-3 text-lg text-muted-foreground">
          Words you saved from your pages, newest first.
        </p>
      </div>

      <VocabularyList />
    </>
  )
}
