import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import type { WordSelection } from '@/hooks/use-word-selection'
import type { Word } from '@/lib/words'
import { makeRecognition } from '@/test/recognitions'
import { renderWithProviders } from '@/test/render'
import { makeSelection } from '@/test/selection'
import { Transcript } from './transcript'

const words = makeRecognition().predictions

function renderTranscript(selection: WordSelection = makeSelection(), list: Word[] = words) {
  renderWithProviders(<Transcript words={list} selection={selection} />)
  return screen.getByRole('region', { name: 'Transcript' })
}

describe('Transcript', () => {
  it('shows the words line by line, labelled as machine output', () => {
    const region = renderTranscript()

    const lines = within(region).getAllByRole('listitem')
    expect(lines).toHaveLength(2)
    expect(
      within(lines[0])
        .getAllByRole('button')
        .map((b) => b.textContent)
    ).toEqual(['Φύσις', 'ἐστί,'])
    expect(lines[1]).toHaveTextContent('Line 2?')
    expect(region).toHaveTextContent('3 words on 2 lines')
    expect(within(region).getByText('Machine output')).toBeInTheDocument()
  })

  it('picks a word when it is clicked', async () => {
    const selection = makeSelection()
    const region = renderTranscript(selection)

    await userEvent.click(within(region).getByRole('button', { name: 'ἐστί,' }))

    expect(selection.select).toHaveBeenCalledWith(1)
  })

  it('lets go of the picked word on a second click', async () => {
    const selection = makeSelection({ selected: 1 })
    const region = renderTranscript(selection)

    await userEvent.click(within(region).getByRole('button', { name: 'ἐστί,' }))

    expect(selection.select).toHaveBeenCalledWith(null)
  })

  it('marks the picked word as pressed', () => {
    const region = renderTranscript(makeSelection({ selected: 2 }))

    expect(within(region).getByRole('button', { name: '?' })).toHaveAttribute(
      'aria-pressed',
      'true'
    )
    expect(within(region).getByRole('button', { name: 'Φύσις' })).toHaveAttribute(
      'aria-pressed',
      'false'
    )
  })

  it('moves keyboard focus along with the picked word', () => {
    const { rerender } = render(
      <Transcript words={words} selection={makeSelection({ selected: 0 })} />
    )
    screen.getByRole('button', { name: 'Φύσις' }).focus()

    rerender(<Transcript words={words} selection={makeSelection({ selected: 1 })} />)

    expect(screen.getByRole('button', { name: 'ἐστί,' })).toHaveFocus()
  })

  it('tells the page which word the mouse is over', async () => {
    const selection = makeSelection()
    const region = renderTranscript(selection)
    const word = within(region).getByRole('button', { name: 'Φύσις' })

    await userEvent.hover(word)
    await userEvent.unhover(word)

    expect(selection.hover).toHaveBeenNthCalledWith(1, 0)
    expect(selection.hover).toHaveBeenLastCalledWith(null)
  })

  it('shows each translation under its word on request', async () => {
    const region = renderTranscript()
    const view = within(region).getByRole('group', { name: 'Transcript view' })

    expect(within(view).getByRole('button', { name: 'Original' })).toHaveAttribute(
      'aria-pressed',
      'true'
    )
    await userEvent.click(within(view).getByRole('button', { name: 'With translation' }))

    expect(within(region).getByRole('button', { name: 'Φύσις nature' })).toBeInTheDocument()
    expect(within(region).getByRole('button', { name: '? ·' })).toBeInTheDocument()
  })

  it('shows the translation of each line under it, instead of word by word', async () => {
    const translated = words.map((word) => ({
      ...word,
      line_translation: word.line === 0 ? 'Nature is.' : 'Unclear.',
    }))
    const region = renderTranscript(makeSelection(), translated)

    expect(within(region).queryByText('Nature is.')).not.toBeInTheDocument()
    await userEvent.click(within(region).getByRole('button', { name: 'With translation' }))

    expect(within(region).getByText('Nature is.')).toBeInTheDocument()
    expect(within(region).getByText('Unclear.')).toBeInTheDocument()
    expect(within(region).getByRole('button', { name: 'Φύσις' })).toBeInTheDocument()
  })

  it('offers the translation view when only the lines are translated', () => {
    const translated = words.map((word) => ({ ...word, gloss: null, line_translation: 'A line.' }))
    const region = renderTranscript(makeSelection(), translated)

    expect(within(region).getByRole('group', { name: 'Transcript view' })).toBeInTheDocument()
  })

  it('offers no translation view when no word has a translation', () => {
    const untranslated = words.map((word) => ({ ...word, gloss: null }))
    const region = renderTranscript(makeSelection(), untranslated)

    expect(within(region).queryByRole('group', { name: 'Transcript view' })).not.toBeInTheDocument()
  })

  it('says so when the model found no words', () => {
    const region = renderTranscript(makeSelection(), [])

    expect(region).toHaveTextContent('0 words on 0 lines')
    expect(within(region).getByText('The model found no words on this page.')).toBeInTheDocument()
    expect(within(region).queryByRole('button')).not.toBeInTheDocument()
  })
})
