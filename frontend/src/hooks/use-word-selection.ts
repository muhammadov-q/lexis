'use client'

import { useEffect, useEffectEvent, useState } from 'react'

export interface WordSelection {
  selected: number | null
  hovered: number | null
  select: (index: number | null) => void
  hover: (index: number | null) => void
}

export function useWordSelection(
  result: object | undefined,
  count: number,
  initial: number | null = null
): WordSelection {
  const [selection, setSelection] = useState<{ result: object; index: number } | null>(() =>
    result !== undefined && initial !== null && initial < count ? { result, index: initial } : null
  )
  const [hovered, setHovered] = useState<number | null>(null)
  const selected = selection !== null && selection.result === result ? selection.index : null

  function select(index: number | null) {
    if (result === undefined) return
    setSelection(index === null ? null : { result, index })
  }

  const onKeyDown = useEffectEvent((event: KeyboardEvent) => {
    if (result === undefined || count === 0 || shouldIgnore(event)) return

    if (event.key === 'ArrowRight' || event.key === 'ArrowLeft') {
      event.preventDefault()
      const step = event.key === 'ArrowRight' ? 1 : -1
      select(selected === null ? 0 : Math.min(Math.max(selected + step, 0), count - 1))
    } else if (event.key === 'Escape' && selected !== null) {
      select(null)
    }
  })

  useEffect(() => {
    const listener = (event: KeyboardEvent) => onKeyDown(event)

    window.addEventListener('keydown', listener)

    return () => window.removeEventListener('keydown', listener)
  }, [])

  return { selected, hovered, select, hover: setHovered }
}

function shouldIgnore(event: KeyboardEvent): boolean {
  const target = event.target
  return (
    event.altKey ||
    event.ctrlKey ||
    event.metaKey ||
    (target instanceof HTMLElement &&
      (target.isContentEditable ||
        ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName) ||
        target.closest('[role="dialog"]') !== null))
  )
}
