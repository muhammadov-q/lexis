import type { Recognition } from '@/lib/api/recognitions'

export type Word = Recognition['predictions'][number]
export type WordBox = Word['box']

export interface ImageSize {
  width: number
  height: number
}

export interface BoxPercent {
  left: number
  top: number
  width: number
  height: number
}

export interface Line {
  number: number
  translation: string | null
  words: { word: Word; index: number }[]
}

export function readText(words: Word[]): string {
  return words.map((word) => word.text).join(' ')
}

export function groupLines(words: Word[]): Line[] {
  const lines = new Map<number, Line>()

  words.forEach((word, index) => {
    const line = lines.get(word.line) ?? {
      number: word.line + 1,
      translation: word.line_translation || null,
      words: [],
    }
    line.words.push({ word, index })
    lines.set(word.line, line)
  })

  return [...lines.values()]
}

export function positionOf(words: Word[], index: number): { line: number; word: number } {
  const { line } = words[index]
  const before = words.slice(0, index).filter((word) => word.line === line).length
  return { line: line + 1, word: before + 1 }
}

export function confidenceOf(word: Word): number {
  return word.probabilities[word.text] ?? 0
}

export function hasBox(box: WordBox): boolean {
  return box.w > 0 && box.h > 0
}

export function boxToPercent(box: WordBox, image: ImageSize): BoxPercent {
  return {
    left: (box.x / image.width) * 100,
    top: (box.y / image.height) * 100,
    width: (box.w / image.width) * 100,
    height: (box.h / image.height) * 100,
  }
}

export interface LineSpan {
  line: number
  top: number
  bottom: number
}

export function lineSpans(words: Word[]): LineSpan[] {
  const spans = new Map<number, LineSpan>()

  for (const word of words) {
    if (!hasBox(word.box)) continue
    const top = word.box.y
    const bottom = word.box.y + word.box.h
    const span = spans.get(word.line)
    spans.set(
      word.line,
      span
        ? { line: word.line, top: Math.min(span.top, top), bottom: Math.max(span.bottom, bottom) }
        : { line: word.line, top, bottom }
    )
  }

  return [...spans.values()]
}

export interface Crop {
  x: number
  y: number
  w: number
  h: number
}

export function loupeCrop(box: WordBox, aspect: number, image: ImageSize): Crop {
  const margin = box.h * 0.6
  let w = box.w + margin * 2
  let h = box.h + margin * 2

  if (w / h < aspect) w = h * aspect
  else h = w / aspect

  return {
    x: clamp(box.x + box.w / 2 - w / 2, 0, image.width - w),
    y: clamp(box.y + box.h / 2 - h / 2, 0, image.height - h),
    w,
    h,
  }
}

function clamp(value: number, min: number, max: number): number {
  return Math.max(min, Math.min(value, max))
}
