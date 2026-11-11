'use client'

import { useEffect, useEffectEvent, useRef, useState } from 'react'
import { toast } from 'sonner'
import { useModelChoice } from '@/hooks/use-models'
import { usePhoneLink } from '@/hooks/use-phone-link'
import { useRecognize } from '@/hooks/use-recognitions'
import { useWordSelection } from '@/hooks/use-word-selection'
import { checkImageFile } from '@/lib/image-file'
import { PhoneLinkBar } from '@/components/phone-capture/phone-link-bar'
import { PhoneLinkDialog } from '@/components/phone-capture/phone-link-dialog'
import { Transcript } from '@/components/transcript/transcript'
import { ImageCropper } from './image-cropper'
import { PageDropzone } from './page-dropzone'
import { PagePreview } from './page-preview'
import { RecognitionPanel } from './recognition-panel'

export function RecognitionWorkbench({ lanAddress = null }: { lanAddress?: string | null }) {
  const [page, setPage] = useState<File | null>(null)
  const [rejection, setRejection] = useState<string | null>(null)
  const [cropping, setCropping] = useState(false)

  const run = useRecognize()
  const models = useModelChoice()
  const abortRef = useRef<AbortController | null>(null)
  const recognition = run.isSuccess ? run.data.recognition : undefined
  const selection = useWordSelection(recognition, recognition?.predictions.length ?? 0)

  function stopRun() {
    abortRef.current?.abort()
    abortRef.current = null
    run.reset()
  }

  function choosePage(file: File): boolean {
    const checked = checkImageFile(file)

    if (!checked.ok) {
      setRejection(checked.reason)
      return false
    }

    stopRun()
    setRejection(null)
    setCropping(false)
    setPage(checked.file)
    return true
  }

  const phone = usePhoneLink((file) => {
    if (choosePage(file)) toast.success('Photo received from your phone')
  })
  const phoneActive = phone.status === 'waiting' || phone.status === 'linked'

  function removePage() {
    stopRun()
    setRejection(null)
    setCropping(false)
    setPage(null)
  }

  function applyCrop(file: File) {
    stopRun()
    setRejection(null)
    setPage(file)
    setCropping(false)
  }

  function recognize() {
    if (!page) return

    abortRef.current?.abort()

    const controller = new AbortController()
    abortRef.current = controller

    run.mutate({
      file: page,
      model: models.selected,
      signal: controller.signal,
    })
  }

  const onPaste = useEffectEvent((event: ClipboardEvent) => {
    const file = Array.from(event.clipboardData?.files ?? []).find((f) =>
      f.type.startsWith('image/')
    )

    if (!file) return

    event.preventDefault()
    choosePage(file)
  })

  useEffect(() => {
    const listener = (event: ClipboardEvent) => onPaste(event)

    window.addEventListener('paste', listener)

    return () => window.removeEventListener('paste', listener)
  }, [])

  useEffect(() => () => abortRef.current?.abort(), [])

  return (
    <>
      {page ? (
        <h1 className="sr-only">Read a manuscript page</h1>
      ) : (
        <div className="mb-10">
          <h1 className="text-3xl font-semibold tracking-tight lg:text-4xl">
            Read a manuscript page
          </h1>
          <p className="mt-3 text-lg text-muted-foreground">
            Upload a photo of the page. The model reads each word, and you can pick any word to see
            its translation.
          </p>
        </div>
      )}

      <div
        id="workbench"
        className="grid scroll-mt-28 grid-cols-1 items-start gap-8 lg:grid-cols-[minmax(0,7fr)_minmax(0,5fr)]"
      >
        <div className="space-y-3">
          {phoneActive && (
            <PhoneLinkBar
              linked={phone.status === 'linked'}
              onShowCode={phone.start}
              onStop={phone.stop}
            />
          )}

          {page ? (
            cropping ? (
              <ImageCropper file={page} onApply={applyCrop} onCancel={() => setCropping(false)} />
            ) : (
              <PagePreview
                file={page}
                words={recognition?.predictions}
                selection={selection}
                onReplace={choosePage}
                onCrop={() => {
                  stopRun()
                  setCropping(true)
                }}
                onRemove={removePage}
                onUsePhone={phoneActive ? undefined : phone.start}
              />
            )
          ) : (
            <PageDropzone onFile={choosePage} onUsePhone={phone.start} />
          )}

          {rejection && (
            <p role="alert" className="text-sm text-foreground">
              {rejection}
            </p>
          )}

          {recognition && !cropping && (
            <Transcript words={recognition.predictions} selection={selection} />
          )}
        </div>

        <RecognitionPanel
          hasPage={page !== null && !cropping}
          run={run}
          models={models}
          selection={selection}
          image={page ?? undefined}
          onRecognize={recognize}
          onCancel={stopRun}
          onReadAgain={stopRun}
        />

        <PhoneLinkDialog
          open={phone.open}
          onOpenChange={phone.setOpen}
          status={phone.status}
          sessionId={phone.sessionId}
          lanAddress={lanAddress}
          onNewCode={phone.start}
        />
      </div>
    </>
  )
}
