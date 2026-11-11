import { fireEvent, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { makeRecognition } from '@/test/recognitions'
import { renderWithProviders } from '@/test/render'
import { makeSelection } from '@/test/selection'
import { WordDetails } from './word-details'

const words = makeRecognition().predictions

function renderDetails(selected: number | null, image?: string) {
  const selection = makeSelection({ selected })
  renderWithProviders(<WordDetails words={words} selection={selection} image={image} />)
  return selection
}

const entry = (term: string) => screen.getByText(term).nextElementSibling

describe('WordDetails', () => {
  it('asks to pick a word when none is picked', () => {
    renderDetails(null)

    expect(screen.getByText('Pick a word')).toBeInTheDocument()
    expect(screen.getByText(/to start with the first word/)).toBeInTheDocument()
    expect(screen.queryByRole('region', { name: 'Selected word' })).not.toBeInTheDocument()
  })

  it('shows where the word is, its translation and what it means', () => {
    renderDetails(1)

    const word = screen.getByRole('region', { name: 'Selected word' })
    expect(within(word).getByText('ἐστί,')).toBeInTheDocument()
    expect(within(word).getByText('Line 1, word 2')).toBeInTheDocument()
    expect(entry('Translation')).toHaveTextContent('is')
    expect(entry('Dictionary form')).toHaveTextContent('εἰμί')
    expect(entry('Definition')).toHaveTextContent('verb: to be')
    expect(entry('Confidence')).toHaveTextContent('80%')
  })

  it('shows the word up close on the page once the image loads', () => {
    renderDetails(0, '/api/recognitions/1/image')

    const loupe = screen.getByRole('img', { name: 'Close-up of Φύσις on the page' })
    const image = loupe.querySelector('img')!
    expect(image).toHaveAttribute('src', '/api/recognitions/1/image')
    expect(image).toHaveStyle({ visibility: 'hidden' })

    Object.defineProperty(image, 'naturalWidth', { value: 1000 })
    Object.defineProperty(image, 'naturalHeight', { value: 1000 })
    fireEvent.load(image)

    expect(image).not.toHaveStyle({ visibility: 'hidden' })
  })

  it('leaves the close-up out without a page image', () => {
    renderDetails(0)

    expect(screen.queryByRole('img', { name: /close-up/i })).not.toBeInTheDocument()
  })

  it('says a word is not confirmed when its reading has no confidence', () => {
    const unconfirmed = words.map((word) => ({ ...word, probabilities: {} }))
    renderWithProviders(
      <WordDetails words={unconfirmed} selection={makeSelection({ selected: 1 })} />
    )

    expect(entry('Confidence')).toHaveTextContent(
      'Not confirmed. kraken read this word differently, so check it on the page.'
    )
  })

  it('says so when a word has no translation', () => {
    renderDetails(2)

    expect(screen.getByText('Line 2, word 1')).toBeInTheDocument()
    expect(screen.getByText('No translation yet')).toBeInTheDocument()
    expect(screen.getByText(/40% confidence/)).toBeInTheDocument()
    expect(screen.queryByText('Translation')).not.toBeInTheDocument()
  })

  it('steps to the previous and next word', async () => {
    const selection = renderDetails(1)

    await userEvent.click(screen.getByRole('button', { name: 'Previous word' }))
    await userEvent.click(screen.getByRole('button', { name: 'Next word' }))

    expect(vi.mocked(selection.select).mock.calls).toEqual([[0], [2]])
  })

  it.each([
    [0, 'Previous word'],
    [2, 'Next word'],
  ])('can not step past the edge from word %i', (selected, name) => {
    renderDetails(selected)

    expect(screen.getByRole('button', { name })).toBeDisabled()
  })
})
