import { act, fireEvent, renderHook } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { useWordSelection } from './use-word-selection'

const press = (key: string, init: KeyboardEventInit = {}) =>
  act(() => {
    fireEvent.keyDown(window, { key, ...init })
  })

const first = {}
const second = {}

describe('useWordSelection', () => {
  it('starts with no word picked', () => {
    const { result } = renderHook(() => useWordSelection(first, 3))

    expect(result.current.selected).toBeNull()
  })

  it('can start on a given word, if the result has it', () => {
    expect(renderHook(() => useWordSelection(first, 3, 2)).result.current.selected).toBe(2)
    expect(renderHook(() => useWordSelection(first, 3, 3)).result.current.selected).toBeNull()
  })

  it('remembers the picked word and lets go of it', () => {
    const { result } = renderHook(() => useWordSelection(first, 3))

    act(() => result.current.select(2))
    expect(result.current.selected).toBe(2)

    act(() => result.current.select(null))
    expect(result.current.selected).toBeNull()
  })

  it('forgets the word when another result comes in', () => {
    const { result, rerender } = renderHook(({ shown }) => useWordSelection(shown, 3), {
      initialProps: { shown: first as object | undefined },
    })
    act(() => result.current.select(2))

    rerender({ shown: second })
    expect(result.current.selected).toBeNull()

    rerender({ shown: undefined })
    expect(result.current.selected).toBeNull()
  })

  it('ignores picks while there is no result', () => {
    const { result } = renderHook(() => useWordSelection(undefined, 0))

    act(() => result.current.select(0))

    expect(result.current.selected).toBeNull()
  })

  it('remembers the word under the mouse', () => {
    const { result } = renderHook(() => useWordSelection(first, 3))

    act(() => result.current.hover(1))
    expect(result.current.hovered).toBe(1)
  })

  it('steps through the words with the arrow keys and stops at the edges', () => {
    const { result } = renderHook(() => useWordSelection(first, 3))

    press('ArrowRight')
    expect(result.current.selected).toBe(0)
    press('ArrowLeft')
    expect(result.current.selected).toBe(0)
    press('ArrowRight')
    press('ArrowRight')
    press('ArrowRight')
    expect(result.current.selected).toBe(2)
    press('ArrowLeft')
    expect(result.current.selected).toBe(1)
  })

  it('lets go of the word with Escape', () => {
    const { result } = renderHook(() => useWordSelection(first, 3))
    act(() => result.current.select(1))

    press('Escape')

    expect(result.current.selected).toBeNull()
  })

  it('leaves the keys alone while typing, in a dialog, with modifiers, or without a result', () => {
    const { result } = renderHook(() => useWordSelection(first, 3))
    const input = document.createElement('input')
    document.body.append(input)

    act(() => {
      fireEvent.keyDown(input, { key: 'ArrowRight' })
    })
    press('ArrowRight', { metaKey: true })
    expect(result.current.selected).toBeNull()
    input.remove()

    const dialog = document.createElement('div')
    dialog.setAttribute('role', 'dialog')
    dialog.append(document.createElement('button'))
    document.body.append(dialog)
    act(() => result.current.select(1))
    act(() => {
      fireEvent.keyDown(dialog.firstChild!, { key: 'Escape' })
      fireEvent.keyDown(dialog.firstChild!, { key: 'ArrowRight' })
    })
    expect(result.current.selected).toBe(1)
    dialog.remove()

    const empty = renderHook(() => useWordSelection(undefined, 0))
    press('ArrowRight')
    expect(empty.result.current.selected).toBeNull()
  })
})
