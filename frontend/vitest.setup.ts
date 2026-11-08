import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { createElement } from 'react'
import { afterEach, vi } from 'vitest'

afterEach(cleanup)

vi.mock('thinking-orbs', () => ({
  ThinkingOrb: (props: { 'aria-label'?: string; 'aria-hidden'?: boolean }) =>
    createElement('span', {
      'data-testid': 'thinking-orb',
      role: 'img',
      'aria-label': props['aria-label'],
      'aria-hidden': props['aria-hidden'],
    }),
}))

if (typeof URL.createObjectURL !== 'function') {
  URL.createObjectURL = () => 'blob:preview'
  URL.revokeObjectURL = () => {}
}

if (typeof globalThis.ResizeObserver !== 'function') {
  globalThis.ResizeObserver = class {
    observe() {}
    unobserve() {}
    disconnect() {}
  }
}

if (typeof Element !== 'undefined') {
  Element.prototype.hasPointerCapture ??= () => false
  Element.prototype.releasePointerCapture ??= () => {}
  Element.prototype.scrollIntoView ??= () => {}
}
