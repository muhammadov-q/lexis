import { act, fireEvent, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { toast } from 'sonner'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { makeModelList } from '@/test/models'
import { renderWithProviders } from '@/test/render'
import { RecognitionWorkbench } from './recognition-workbench'

vi.mock('./image-cropper', () => ({
  ImageCropper: ({
    onApply,
    onCancel,
  }: {
    onApply: (file: File) => void
    onCancel: () => void
  }) => (
    <div>
      <button
        onClick={() =>
          onApply(new File(['cropped-bytes'], 'folio-1r-cropped.png', { type: 'image/png' }))
        }
      >
        Apply crop
      </button>

      <button onClick={onCancel}>Cancel crop</button>
    </div>
  ),
}))

afterEach(() => {
  vi.unstubAllGlobals()
  vi.useRealTimers()
  vi.restoreAllMocks()
})

const page = () => new File(['png-bytes'], 'folio-1r.png', { type: 'image/png' })

const saved = { id: 1, created_at: '2026-10-15T12:00:00Z', filename: 'folio-1r.png' }

const mockResponse = (digits: string[]) => ({
  ...saved,
  predictions: digits.map((text) => ({
    text,
    probabilities: { [text]: 1 },
    box: { x: 0, y: 0, w: 0, h: 0 },
    line: 0,
    gloss: null,
  })),
  model_version: 'mock',
})

const greekResponse = {
  ...saved,
  predictions: [
    {
      text: 'Φύσις',
      probabilities: { Φύσις: 1 },
      box: { x: 10, y: 20, w: 30, h: 40 },
      line: 0,
      gloss: { lemma: 'φύσις', translation: 'nature', definition: 'noun: nature' },
    },
    {
      text: 'ἐστί,',
      probabilities: { 'ἐστί,': 1 },
      box: { x: 50, y: 20, w: 30, h: 40 },
      line: 0,
      gloss: { lemma: 'εἰμί', translation: 'is', definition: 'verb: to be' },
    },
  ],
  model_version: 'eparchos-6791',
}

type Upload = (url: string, init: RequestInit) => Promise<Response>

function stubBackend<T extends Upload>(upload: T, models = () => Response.json(makeModelList())) {
  vi.stubGlobal(
    'fetch',
    vi.fn(async (url: string, init: RequestInit = {}) =>
      url === '/api/models' ? models() : upload(url, init)
    )
  )
  return upload
}

const sentModel = (upload: ReturnType<typeof vi.fn>, call = 0) =>
  ((upload.mock.calls[call][1] as RequestInit).body as FormData).get('model')

function setup({ applyAccept = true } = {}) {
  const user = userEvent.setup({ applyAccept })
  renderWithProviders(<RecognitionWorkbench />)
  return { user }
}

const recognizeButton = () => screen.getByRole('button', { name: /recognize page/i })

describe('RecognitionWorkbench', () => {
  it('shows that a guest result is not saved', async () => {
    const response = mockResponse(['2', '5'])
    const prediction = {
      predictions: response.predictions,
      model_version: response.model_version,
    }

    const fetch = vi
      .fn()
      .mockResolvedValueOnce(Response.json({ detail: 'Sign in to continue.' }, { status: 401 }))
      .mockResolvedValueOnce(Response.json(prediction))

    stubBackend(fetch)
    const { user } = setup()

    await user.upload(screen.getByLabelText('Page image'), page())
    await user.click(recognizeButton())

    expect(await screen.findByText(/This result is not saved\./)).toBeInTheDocument()

    expect(screen.getByRole('link', { name: 'Sign in' })).toHaveAttribute('href', '/sign-in')
    expect(screen.queryByRole('link', { name: 'Open it' })).not.toBeInTheDocument()
    expect(screen.queryByText(/Saved to your history\./)).not.toBeInTheDocument()

    const transcript = screen.getByRole('region', { name: 'Transcript' })
    expect(
      within(transcript)
        .getAllByRole('button')
        .map((b) => b.textContent)
    ).toEqual(['2', '5'])

    expect(fetch.mock.calls.map(([url]) => url)).toEqual(['/api/recognitions', '/api/predict'])
  })

  it('introduces the page until a page image is chosen, then makes room for it', async () => {
    const { user } = setup()
    const intro = screen.getByText(/pick any word to see its translation/i)
    expect(screen.getByRole('heading', { level: 1, name: 'Read a manuscript page' })).toBeVisible()

    await user.upload(screen.getByLabelText('Page image'), page())

    expect(intro).not.toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 1, name: 'Read a manuscript page' })).toHaveClass(
      'sr-only'
    )
  })

  it('sends the chosen page to the backend and shows the response', async () => {
    const fetch = vi.fn().mockResolvedValue(Response.json(mockResponse(['2', '5', '0'])))
    stubBackend(fetch)
    const { user } = setup()

    expect(recognizeButton()).toBeDisabled()
    await user.upload(screen.getByLabelText('Page image'), page())
    expect(screen.getByRole('img', { name: 'Preview of folio-1r.png' })).toBeInTheDocument()

    await user.click(recognizeButton())

    const response = await screen.findByRole('region', { name: 'Model response' })
    const transcript = screen.getByRole('region', { name: 'Transcript' })
    expect(
      within(transcript)
        .getAllByRole('button')
        .map((b) => b.textContent)
    ).toEqual(['2', '5', '0'])
    expect(within(response).getByText('mock')).toBeInTheDocument()
    expect(within(response).getByText('Machine output')).toBeInTheDocument()
    expect(fetch).toHaveBeenCalledOnce()
    expect(fetch).toHaveBeenCalledWith(
      '/api/recognitions',
      expect.objectContaining({ method: 'POST' })
    )
    expect(screen.getByRole('link', { name: 'Open it' })).toHaveAttribute(
      'href',
      '/dashboard/history/1'
    )
  })

  it('shows the translation of a word picked in the transcript or on the page', async () => {
    stubBackend(vi.fn().mockResolvedValue(Response.json(greekResponse)))
    const { user } = setup()

    await user.upload(screen.getByLabelText('Page image'), page())
    const preview = screen.getByRole('img', { name: 'Preview of folio-1r.png' })
    Object.defineProperty(preview, 'naturalWidth', { value: 100 })
    Object.defineProperty(preview, 'naturalHeight', { value: 100 })
    fireEvent.load(preview)
    expect(screen.queryAllByTestId('word-box')).toHaveLength(0)

    await user.click(recognizeButton())
    const response = await screen.findByRole('region', { name: 'Model response' })
    expect(within(response).getByText('Pick a word')).toBeInTheDocument()

    const transcript = screen.getByRole('region', { name: 'Transcript' })
    await user.click(within(transcript).getByRole('button', { name: 'ἐστί,' }))

    const word = within(response).getByRole('region', { name: 'Selected word' })
    expect(word).toHaveTextContent('Line 1, word 2')
    expect(word).toHaveTextContent('is')
    const boxes = screen.getAllByTestId('word-box')
    expect(boxes[1]).toHaveAttribute('data-selected', 'true')
    expect(boxes[0]).toHaveStyle({ left: '10%', top: '20%', width: '30%', height: '40%' })

    await user.click(boxes[0])
    expect(word).toHaveTextContent('Line 1, word 1')
    expect(word).toHaveTextContent('nature')
    expect(within(transcript).getByRole('button', { name: 'Φύσις' })).toHaveAttribute(
      'aria-pressed',
      'true'
    )
  })

  it('rejects a file that is not a page image before it reaches the backend', async () => {
    const fetch = vi.fn()
    stubBackend(fetch)
    const { user } = setup({ applyAccept: false })

    await user.upload(
      screen.getByLabelText('Page image'),
      new File(['%PDF'], 'thesis.pdf', { type: 'application/pdf' })
    )

    expect(screen.getByRole('alert')).toHaveTextContent(
      'thesis.pdf is not a JPG, PNG or TIFF image.'
    )
    expect(recognizeButton()).toBeDisabled()
    expect(fetch).not.toHaveBeenCalled()
  })

  it("shows the backend's reason when it rejects the image", async () => {
    stubBackend(
      vi.fn().mockResolvedValue(Response.json({ detail: 'File must be an image' }, { status: 415 }))
    )
    const { user } = setup()

    await user.upload(screen.getByLabelText('Page image'), page())
    await user.click(recognizeButton())

    const alert = await screen.findByRole('alert')
    expect(alert).toHaveTextContent('Image rejected')
    expect(alert).toHaveTextContent('File must be an image')
    expect(screen.getByRole('button', { name: /try again/i })).toBeEnabled()
  })

  it('explains when the recognition service is offline', async () => {
    stubBackend(
      vi
        .fn()
        .mockResolvedValue(
          Response.json({ detail: 'The recognition service is not reachable.' }, { status: 502 })
        )
    )
    const { user } = setup()

    await user.upload(screen.getByLabelText('Page image'), page())
    await user.click(recognizeButton())

    expect(await screen.findByRole('alert')).toHaveTextContent('Recognition service offline')
  })

  it('can cancel a running recognition', async () => {
    const fetch = vi.fn(
      (_url: string, init: RequestInit) =>
        new Promise<Response>((_, reject) => {
          init.signal?.addEventListener('abort', () =>
            reject(new DOMException('Aborted', 'AbortError'))
          )
        })
    )
    stubBackend(fetch)
    const { user } = setup()

    await user.upload(screen.getByLabelText('Page image'), page())
    await screen.findByRole('combobox', { name: 'Model' })
    await user.click(recognizeButton())

    expect(await screen.findByText('Reading the page')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /recognizing/i })).toBeDisabled()
    expect(screen.getByRole('combobox', { name: 'Model' })).toBeDisabled()

    await user.click(screen.getByRole('button', { name: 'Cancel' }))

    expect(await screen.findByText('Ready to read')).toBeInTheDocument()
    expect(fetch.mock.calls[0][1].signal?.aborted).toBe(true)
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('clears the previous result when the page is replaced', async () => {
    stubBackend(vi.fn().mockResolvedValue(Response.json(mockResponse(['1']))))
    const { user } = setup()

    await user.upload(screen.getByLabelText('Page image'), page())
    await user.click(recognizeButton())
    await screen.findByRole('region', { name: 'Model response' })

    await user.upload(
      screen.getByLabelText('Replacement page image'),
      new File(['png'], 'folio-1v.png', { type: 'image/png' })
    )

    expect(screen.queryByRole('region', { name: 'Model response' })).not.toBeInTheDocument()
    expect(screen.queryByRole('region', { name: 'Transcript' })).not.toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'Preview of folio-1v.png' })).toBeInTheDocument()
  })

  it('opens the crop flow and returns to the preview when cancelled', async () => {
    const { user } = setup()

    await user.upload(screen.getByLabelText('Page image'), page())

    expect(screen.getByRole('img', { name: 'Preview of folio-1r.png' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Crop' }))

    expect(screen.getByRole('button', { name: 'Cancel crop' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Cancel crop' }))

    expect(screen.getByRole('img', { name: 'Preview of folio-1r.png' })).toBeInTheDocument()
  })

  it('sends the cropped image to the backend after applying a crop', async () => {
    const fetch = vi.fn().mockResolvedValue(Response.json(mockResponse(['7'])))

    stubBackend(fetch)

    const { user } = setup()

    await user.upload(screen.getByLabelText('Page image'), page())

    await user.click(screen.getByRole('button', { name: 'Crop' }))
    await user.click(screen.getByRole('button', { name: 'Apply crop' }))

    expect(
      screen.getByRole('img', {
        name: 'Preview of folio-1r-cropped.png',
      })
    ).toBeInTheDocument()

    await user.click(recognizeButton())

    expect(fetch).toHaveBeenCalledOnce()

    const requestInit = fetch.mock.calls[0][1] as RequestInit
    const body = requestInit.body as FormData
    const uploadedImage = body.get('image')

    expect(uploadedImage).toBeInstanceOf(File)
    expect((uploadedImage as File).name).toBe('folio-1r-cropped.png')
  })

  it('reads with the default model, then again with the model chosen', async () => {
    const upload = stubBackend(vi.fn(async () => Response.json(greekResponse)))
    const { user } = setup()

    await user.upload(screen.getByLabelText('Page image'), page())
    expect(await screen.findByRole('combobox', { name: 'Model' })).toHaveTextContent(
      'Greek handwriting'
    )
    await user.click(recognizeButton())
    await screen.findByRole('region', { name: 'Model response' })
    expect(sentModel(upload)).toBe('kraken')

    await user.click(screen.getByRole('button', { name: 'Read with another model' }))
    expect(screen.queryByRole('region', { name: 'Transcript' })).not.toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'Preview of folio-1r.png' })).toBeInTheDocument()

    await user.click(screen.getByRole('combobox', { name: 'Model' }))
    await user.click(screen.getByRole('option', { name: /Untrained reader, for comparison/ }))
    await user.click(recognizeButton())

    await screen.findByRole('region', { name: 'Model response' })
    expect(sentModel(upload, 1)).toBe('kraken-base')
  })

  it('still reads the page with the default model when the models cannot be listed', async () => {
    const upload = stubBackend(vi.fn().mockResolvedValue(Response.json(greekResponse)), () =>
      Response.json({ detail: 'Not reachable' }, { status: 502 })
    )
    const { user } = setup()

    await user.upload(screen.getByLabelText('Page image'), page())
    expect(await screen.findByText(/Couldn't load the models/)).toBeInTheDocument()
    await user.click(recognizeButton())

    await screen.findByRole('region', { name: 'Model response' })
    expect(sentModel(upload)).toBeNull()
  })

  it('takes a page pasted from the clipboard', () => {
    setup()
    const paste = new Event('paste', { cancelable: true })
    Object.defineProperty(paste, 'clipboardData', { value: { files: [page()] } })

    act(() => {
      window.dispatchEvent(paste)
    })

    expect(screen.getByRole('img', { name: 'Preview of folio-1r.png' })).toBeInTheDocument()
    expect(paste.defaultPrevented).toBe(true)
  })
})

describe('RecognitionWorkbench with a phone', () => {
  function stubPhoneBackend({
    uploads = () => 0,
    create = () => true,
  }: { uploads?: () => number; create?: () => boolean } = {}) {
    const fetch = vi.fn(async (url: string, init: RequestInit = {}) => {
      if (url === '/api/models') {
        return Response.json(makeModelList())
      }
      if (url === '/api/capture-sessions') {
        return create()
          ? Response.json({ id: 'abc', upload_count: 0 }, { status: 201 })
          : Response.json({ detail: 'Not reachable' }, { status: 502 })
      }
      if (url === '/api/capture-sessions/abc' && init.method === 'DELETE') {
        return new Response(null, { status: 204 })
      }
      if (url === '/api/capture-sessions/abc') {
        return Response.json({ id: 'abc', upload_count: uploads() })
      }
      if (url === '/api/capture-sessions/abc/image') {
        return new Response(new Blob(['jpeg'], { type: 'image/jpeg' }), {
          headers: { 'Content-Type': 'image/jpeg' },
        })
      }
      throw new Error(`Unexpected request ${url}`)
    })
    vi.stubGlobal('fetch', fetch)
    return fetch
  }

  function setupPhone(lanAddress: string | null = '192.168.1.20') {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
    renderWithProviders(<RecognitionWorkbench lanAddress={lanAddress} />)
    return { user }
  }

  const poll = () => act(() => vi.advanceTimersByTimeAsync(1500))

  it('shows a QR code, then puts the photo from the phone on the page', async () => {
    let uploads = 0
    stubPhoneBackend({ uploads: () => uploads })
    const { user } = setupPhone()

    await user.click(screen.getByRole('button', { name: 'Use your phone' }))
    const dialog = await screen.findByRole('dialog', { name: 'Use your phone' })
    expect(await within(dialog).findByRole('img', { name: /QR code/ })).toBeInTheDocument()
    expect(within(dialog).getByText('http://192.168.1.20:3000/capture/abc')).toBeInTheDocument()
    expect(within(dialog).getByText('Waiting for a photo')).toBeInTheDocument()

    uploads = 1
    await poll()

    expect(
      await screen.findByRole('img', { name: 'Preview of Phone photo 1.jpg' })
    ).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.getByText('Phone linked. New photos replace the page.')).toBeInTheDocument()
    expect(recognizeButton()).toBeEnabled()
  })

  it('replaces the page when the phone sends another photo', async () => {
    let uploads = 1
    stubPhoneBackend({ uploads: () => uploads })
    const { user } = setupPhone()

    await user.click(screen.getByRole('button', { name: 'Use your phone' }))
    await screen.findByRole('img', { name: 'Preview of Phone photo 1.jpg' })

    uploads = 2
    await poll()

    expect(
      await screen.findByRole('img', { name: 'Preview of Phone photo 2.jpg' })
    ).toBeInTheDocument()
  })

  it('stops listening when the phone is unlinked', async () => {
    const fetch = stubPhoneBackend({ uploads: () => 1 })
    const { user } = setupPhone()

    await user.click(screen.getByRole('button', { name: 'Use your phone' }))
    await screen.findByText('Phone linked. New photos replace the page.')
    await user.click(screen.getByRole('button', { name: 'Unlink' }))

    expect(screen.queryByText(/Phone linked/)).not.toBeInTheDocument()
    expect(fetch).toHaveBeenCalledWith(
      '/api/capture-sessions/abc',
      expect.objectContaining({ method: 'DELETE' })
    )
    const calls = fetch.mock.calls.length
    await poll()
    expect(fetch).toHaveBeenCalledTimes(calls)
  })

  it('offers a retry when no code can be made', async () => {
    let reachable = false
    stubPhoneBackend({ create: () => reachable })
    const { user } = setupPhone()

    await user.click(screen.getByRole('button', { name: 'Use your phone' }))
    const dialog = await screen.findByRole('dialog', { name: 'Use your phone' })
    expect(await within(dialog).findByRole('alert')).toHaveTextContent("Couldn't make a code")

    reachable = true
    await user.click(within(dialog).getByRole('button', { name: 'Try again' }))
    expect(await within(dialog).findByRole('img', { name: /QR code/ })).toBeInTheDocument()
  })

  it('offers a new code when the old one expired', async () => {
    const fetch = stubPhoneBackend()
    const { user } = setupPhone()
    await screen.findByRole('combobox', { name: 'Model' })
    fetch.mockImplementationOnce(async () =>
      Response.json({ id: 'old', upload_count: 0 }, { status: 201 })
    )
    fetch.mockImplementationOnce(async () => Response.json({ detail: 'Expired' }, { status: 404 }))

    await user.click(screen.getByRole('button', { name: 'Use your phone' }))
    const dialog = await screen.findByRole('dialog', { name: 'Use your phone' })
    expect(await within(dialog).findByRole('alert')).toHaveTextContent('This code has expired')

    await user.click(within(dialog).getByRole('button', { name: 'New code' }))
    expect(
      await within(dialog).findByText('http://192.168.1.20:3000/capture/abc')
    ).toBeInTheDocument()
  })

  it('copies the phone link', async () => {
    stubPhoneBackend()
    const { user } = setupPhone()

    await user.click(screen.getByRole('button', { name: 'Use your phone' }))
    await user.click(await screen.findByRole('button', { name: 'Copy link' }))

    expect(await navigator.clipboard.readText()).toBe('http://192.168.1.20:3000/capture/abc')
  })

  it('says so instead of failing when the browser blocks copying', async () => {
    stubPhoneBackend()
    const { user } = setupPhone()
    vi.spyOn(navigator, 'clipboard', 'get').mockReturnValue(undefined as unknown as Clipboard)
    const error = vi.spyOn(toast, 'error')

    await user.click(screen.getByRole('button', { name: 'Use your phone' }))
    await user.click(await screen.findByRole('button', { name: 'Copy link' }))

    expect(error).toHaveBeenCalledWith(expect.stringContaining("Couldn't copy the link"))
  })

  it("explains that a phone can't open localhost when there is no network address", async () => {
    stubPhoneBackend()
    const { user } = setupPhone(null)

    await user.click(screen.getByRole('button', { name: 'Use your phone' }))

    expect(await screen.findByRole('alert')).toHaveTextContent("Your phone can't open localhost")
  })
})
