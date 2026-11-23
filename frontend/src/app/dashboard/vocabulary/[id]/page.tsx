import type { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { VocabularyWordView } from '@/components/vocabulary/vocabulary-word'

export const metadata: Metadata = { title: 'Word' }

export default async function VocabularyWordPage({
  params,
}: PageProps<'/dashboard/vocabulary/[id]'>) {
  const id = Number((await params).id)
  if (!Number.isInteger(id) || id < 1) notFound()

  return <VocabularyWordView id={id} />
}
