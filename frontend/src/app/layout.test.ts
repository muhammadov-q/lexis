import { describe, expect, it, vi } from 'vitest'
import { metadata } from './layout'

vi.mock('next/font/google', () => {
  const font = () => ({ variable: '' })
  return { JetBrains_Mono: font, Noto_Serif: font, Schibsted_Grotesk: font }
})

describe('root layout', () => {
  it('names the browser tab Lexis in Latin letters', () => {
    expect(metadata.title).toEqual({ default: 'Lexis', template: '%s · Lexis' })
  })
})
