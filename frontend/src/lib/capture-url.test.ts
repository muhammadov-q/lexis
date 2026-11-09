import { describe, expect, it } from 'vitest'
import { captureUrl } from './capture-url'

const at = (href: string) => {
  const { protocol, hostname, port, origin } = new URL(href)
  return { protocol, hostname, port, origin }
}

describe('captureUrl', () => {
  it('keeps the address when the page is not on localhost', () => {
    expect(captureUrl('abc', at('https://lexis.example.ch/'), '192.168.1.20')).toBe(
      'https://lexis.example.ch/capture/abc'
    )
    expect(captureUrl('abc', at('http://192.168.1.20:3000/'), null)).toBe(
      'http://192.168.1.20:3000/capture/abc'
    )
  })

  it.each(['http://localhost:3000/', 'http://127.0.0.1:3000/', 'http://[::1]:3000/'])(
    'swaps %s for the network address, keeping the port',
    (href) => {
      expect(captureUrl('abc', at(href), '192.168.1.20')).toBe(
        'http://192.168.1.20:3000/capture/abc'
      )
    }
  )

  it('leaves out the port when the page has none', () => {
    expect(captureUrl('abc', at('http://localhost/'), '10.0.0.5')).toBe(
      'http://10.0.0.5/capture/abc'
    )
  })

  it('gives up on localhost without a network address', () => {
    expect(captureUrl('abc', at('http://localhost:3000/'), null)).toBeNull()
  })

  it('escapes the session id', () => {
    expect(captureUrl('a/b', at('https://lexis.example.ch/'), null)).toBe(
      'https://lexis.example.ch/capture/a%2Fb'
    )
  })
})
