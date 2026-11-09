import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { SiteHeader } from './site-header'

describe('SiteHeader', () => {
  it('links home and switches the theme', () => {
    render(<SiteHeader />)

    expect(screen.getByRole('link', { name: 'Λέξις' })).toHaveAttribute('href', '/')
    expect(screen.getByText('Λέξις')).toHaveAttribute('lang', 'el')
    expect(screen.getByRole('button', { name: 'Switch theme' })).toBeInTheDocument()
  })

  it('leaves the project links to the sidebar', () => {
    render(<SiteHeader />)

    expect(screen.getAllByRole('link')).toHaveLength(1)
  })
})
