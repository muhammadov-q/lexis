import { fireEvent, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import type { Word } from '@/lib/words'
import { renderWithProviders } from '@/test/render'
import { makeSelection } from '@/test/selection'
import { PagePreview } from './page-preview'

const page = new File(['png-bytes'], 'folio.png', { type: 'image/png' })

const word = (text: string, box: Word['box'], line = 0): Word => ({
  text,
  probabilities: { [text]: 1 },
  box,
  line,
  gloss: null,
  line_translation: null,
})

const words = [
  word('καὶ', { x: 100, y: 50, w: 200, h: 100 }),
  word('μὲν', { x: 500, y: 50, w: 200, h: 100 }),
  word('δὲ', { x: 0, y: 0, w: 0, h: 0 }),
  word('γάρ', { x: 100, y: 300, w: 200, h: 100 }, 1),
]

function renderPreview(props: Partial<React.ComponentProps<typeof PagePreview>> = {}) {
  renderWithProviders(
    <PagePreview file={page} onReplace={vi.fn()} onCrop={vi.fn()} onRemove={vi.fn()} {...props} />
  )
  return screen.getByRole('img', { name: 'Preview of folio.png' })
}

function load(image: HTMLElement, width: number, height: number) {
  Object.defineProperty(image, 'naturalWidth', { value: width })
  Object.defineProperty(image, 'naturalHeight', { value: height })
  fireEvent.load(image)
}

describe('PagePreview', () => {
  it('draws a box over each word the model found once the image loads', () => {
    const image = renderPreview({ words, selection: makeSelection() })

    expect(screen.queryAllByTestId('word-box')).toHaveLength(0)
    load(image, 1000, 500)

    const boxes = screen.getAllByTestId('word-box')
    expect(boxes).toHaveLength(3)
    expect(boxes[1]).toHaveStyle({ left: '50%', top: '10%', width: '20%', height: '20%' })
  })

  it('numbers the lines beside the page and marks the picked line', () => {
    load(renderPreview({ words, selection: makeSelection({ selected: 3 }) }), 1000, 500)

    const numbers = screen.getByTestId('line-numbers').children
    expect([...numbers].map((n) => n.textContent)).toEqual(['1', '2'])
    expect(numbers[0]).toHaveStyle({ top: '20%' })
    expect(numbers[1]).toHaveStyle({ top: '70%' })
    expect(numbers[1]).toHaveClass('bg-primary')
  })

  it('picks a word when its box is clicked and marks the picked one', async () => {
    const selection = makeSelection({ selected: 0 })
    load(renderPreview({ words, selection }), 1000, 500)

    const [first, second] = screen.getAllByTestId('word-box')
    expect(first).toHaveAttribute('data-selected', 'true')
    expect(second).not.toHaveAttribute('data-selected')

    await userEvent.click(second)
    expect(selection.select).toHaveBeenCalledWith(1)
    expect(selection.hover).toHaveBeenCalledWith(1)
  })

  it('lights up the box of the word the mouse is over in the transcript', () => {
    load(renderPreview({ words, selection: makeSelection({ hovered: 1 }) }), 1000, 500)

    expect(screen.getAllByTestId('word-box')[1]).toHaveClass('bg-primary/15')
  })

  it('keeps the page actions in a labelled toolbar on the page', async () => {
    const onRemove = vi.fn()
    const onCrop = vi.fn()
    renderPreview({ onRemove, onCrop, onUsePhone: vi.fn() })

    expect(screen.getByText('folio.png')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Use phone' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Replace page' })).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Crop' }))
    await userEvent.click(screen.getByRole('button', { name: 'Remove page' }))

    expect(onCrop).toHaveBeenCalledOnce()
    expect(onRemove).toHaveBeenCalledOnce()
  })

  it('shows the plain page before there is a result', () => {
    load(renderPreview(), 1000, 500)

    expect(screen.queryAllByTestId('word-box')).toHaveLength(0)
  })

  it('explains when the browser cannot show the image', () => {
    fireEvent.error(renderPreview({ words }))

    expect(screen.getByText(/can't preview this image/i)).toBeInTheDocument()
    expect(screen.queryAllByTestId('word-box')).toHaveLength(0)
  })
})
