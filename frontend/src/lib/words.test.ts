import { describe, expect, it } from 'vitest'
import {
  boxToPercent,
  confidenceOf,
  groupLines,
  hasBox,
  lineSpans,
  loupeCrop,
  positionOf,
  readText,
  type Word,
} from './words'

const word = (text: string, line = 0, probabilities: Record<string, number> = {}): Word => ({
  text,
  probabilities,
  box: { x: 0, y: 0, w: 10, h: 10 },
  line,
  gloss: null,
  line_translation: null,
})

const page = [word('Φύσις', 0), word('ἐστί', 0), word('δύναμις', 1)]

describe('readText', () => {
  it('joins the words in the order the backend returns them', () => {
    expect(readText(page)).toBe('Φύσις ἐστί δύναμις')
  })

  it('is empty when nothing was found', () => {
    expect(readText([])).toBe('')
  })
})

describe('groupLines', () => {
  it('puts each word on its line, numbered from 1, and keeps its index', () => {
    expect(groupLines(page)).toEqual([
      {
        number: 1,
        translation: null,
        words: [
          { word: page[0], index: 0 },
          { word: page[1], index: 1 },
        ],
      },
      { number: 2, translation: null, words: [{ word: page[2], index: 2 }] },
    ])
  })

  it('has no lines when nothing was found', () => {
    expect(groupLines([])).toEqual([])
  })

  it('treats an empty line translation as none', () => {
    const empty = page.map((word) => ({ ...word, line_translation: '' }))

    expect(groupLines(empty).map((line) => line.translation)).toEqual([null, null])
  })

  it('takes the translation of each line from its words', () => {
    const translated = page.map((word) => ({ ...word, line_translation: `Line ${word.line}.` }))

    expect(groupLines(translated).map((line) => line.translation)).toEqual(['Line 0.', 'Line 1.'])
  })
})

describe('positionOf', () => {
  it.each([
    [0, { line: 1, word: 1 }],
    [1, { line: 1, word: 2 }],
    [2, { line: 2, word: 1 }],
  ])('word %i sits at %o', (index, position) => {
    expect(positionOf(page, index)).toEqual(position)
  })
})

describe('confidenceOf', () => {
  it("reads the predicted text's probability", () => {
    expect(confidenceOf(word('7', 0, { '1': 0.08, '7': 0.92 }))).toBe(0.92)
  })

  it('is 0 when the predicted text has no probability', () => {
    expect(confidenceOf(word('7', 0, { '1': 1 }))).toBe(0)
  })
})

describe('hasBox', () => {
  it.each([
    [{ x: 0, y: 0, w: 0, h: 0 }, false],
    [{ x: 5, y: 5, w: 10, h: 0 }, false],
    [{ x: 5, y: 5, w: 10, h: 20 }, true],
  ])('%o has a box: %s', (box, expected) => {
    expect(hasBox(box)).toBe(expected)
  })
})

describe('boxToPercent', () => {
  it('turns pixel coordinates into percentages of the image', () => {
    expect(boxToPercent({ x: 100, y: 50, w: 200, h: 100 }, { width: 1000, height: 500 })).toEqual({
      left: 10,
      top: 10,
      width: 20,
      height: 20,
    })
  })
})

describe('lineSpans', () => {
  it('spans each line from its highest to its lowest word box, skipping words without one', () => {
    const boxed = (line: number, y: number, h: number): Word => ({
      ...word('w', line),
      box: { x: 0, y, w: 10, h },
    })
    const unboxed = { ...word('?', 2), box: { x: 0, y: 0, w: 0, h: 0 } }

    expect(lineSpans([boxed(0, 10, 20), boxed(0, 5, 10), boxed(1, 50, 20), unboxed])).toEqual([
      { line: 0, top: 5, bottom: 30 },
      { line: 1, top: 50, bottom: 70 },
    ])
  })
})

describe('loupeCrop', () => {
  const page = { width: 1000, height: 1000 }

  it('centres the word with a margin, in the given shape', () => {
    const crop = loupeCrop({ x: 400, y: 400, w: 100, h: 50 }, 3, page)

    expect(crop.w / crop.h).toBeCloseTo(3)
    expect(crop.x + crop.w / 2).toBeCloseTo(450)
    expect(crop.y + crop.h / 2).toBeCloseTo(425)
    expect(crop.h).toBeCloseTo(110)
  })

  it('stays on the page at its edges', () => {
    const crop = loupeCrop({ x: 0, y: 0, w: 40, h: 40 }, 3, page)

    expect(crop.x).toBe(0)
    expect(crop.y).toBe(0)
  })
})
