import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { DashboardMenu } from './dashboard-menu'

const navigation = vi.hoisted(() => ({ pathname: '/dashboard' }))
vi.mock('next/navigation', () => ({ usePathname: () => navigation.pathname }))

vi.mock('@/components/auth/account-card', () => ({
  AccountCard: ({ expanded }: { expanded: boolean }) => (
    <span>Account card {expanded ? 'expanded' : 'collapsed'}</span>
  ),
}))

vi.mock('@/components/theme-toggle', () => ({
  ThemeToggle: () => <button>Switch theme</button>,
}))

beforeEach(() => {
  navigation.pathname = '/dashboard'
})

const PROJECT_LINKS = [
  ['Docs', 'https://github.com/muhammadov-q/lexis/tree/develop/docs'],
  ['Source', 'https://github.com/muhammadov-q/lexis'],
]

describe('DashboardMenu', () => {
  it('starts expanded with the account card', () => {
    render(<DashboardMenu />)

    expect(screen.getByText('Account card expanded')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Λέξις' })).toHaveAttribute('href', '/dashboard')
    expect(screen.getByRole('button', { name: 'Collapse navigation' })).toHaveAttribute(
      'aria-expanded',
      'true'
    )
  })

  it('collapses to icons while keeping navigation accessible', async () => {
    const user = userEvent.setup()
    render(<DashboardMenu />)

    await user.click(screen.getByRole('button', { name: 'Collapse navigation' }))

    expect(screen.getByText('Account card collapsed')).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Λέξις' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Docs/ })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Read' })).toBeVisible()
    expect(screen.getByRole('button', { name: 'Expand navigation' })).toHaveAttribute(
      'aria-expanded',
      'false'
    )
    expect(screen.getByRole('button', { name: 'Expand navigation' })).toHaveTextContent('Λ')

    await user.click(screen.getByRole('button', { name: 'Expand navigation' }))

    expect(screen.getByText('Account card expanded')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Λέξις' })).toBeVisible()
  })

  it('names a page on hover when collapsed', async () => {
    const user = userEvent.setup()
    render(<DashboardMenu />)

    await user.click(screen.getByRole('button', { name: 'Collapse navigation' }))
    await user.hover(screen.getByRole('link', { name: 'History' }))

    expect(await screen.findByRole('tooltip')).toHaveTextContent('History')
  })

  it('does not expand the collapsed sidebar on hover', () => {
    render(<DashboardMenu />)

    fireEvent.click(screen.getByRole('button', { name: 'Collapse navigation' }))

    fireEvent.mouseEnter(screen.getByRole('complementary'))
    fireEvent.mouseLeave(screen.getByRole('complementary'))

    expect(screen.getByRole('button', { name: 'Expand navigation' })).toHaveAttribute(
      'aria-expanded',
      'false'
    )
  })

  it('supports keyboard toggling', async () => {
    const user = userEvent.setup()
    render(<DashboardMenu />)

    await user.tab()
    expect(screen.getByRole('link', { name: 'Λέξις' })).toHaveFocus()
    await user.tab()
    expect(screen.getByRole('button', { name: 'Collapse navigation' })).toHaveFocus()

    await user.keyboard('{Enter}')
    expect(screen.getByRole('button', { name: 'Expand navigation' })).toHaveFocus()

    await user.keyboard(' ')
    expect(screen.getByRole('button', { name: 'Collapse navigation' })).toHaveAttribute(
      'aria-expanded',
      'true'
    )
  })

  it('keeps Docs and Source outside the page navigation', () => {
    render(<DashboardMenu />)

    const nav = screen.getByRole('navigation', { name: 'Dashboard navigation' })

    for (const [name, href] of PROJECT_LINKS) {
      const link = screen.getByRole('link', { name: `${name} (opens in a new tab)` })

      expect(link).toHaveAttribute('href', href)
      expect(link).toHaveAttribute('target', '_blank')
      expect(link).toHaveAttribute('rel', 'noopener noreferrer')
      expect(nav).not.toContainElement(link)
    }

    expect(screen.getByRole('button', { name: 'Switch theme' })).toBeVisible()
  })

  it('marks Read as the page on the dashboard', () => {
    render(<DashboardMenu />)

    expect(screen.getByRole('link', { name: 'Read' })).toHaveAttribute('href', '/dashboard')
    expect(screen.getByRole('link', { name: 'Read' })).toHaveAttribute('aria-current', 'page')
    expect(screen.getByRole('link', { name: 'History' })).toHaveAttribute(
      'href',
      '/dashboard/history'
    )
    expect(screen.getByRole('link', { name: 'History' })).not.toHaveAttribute('aria-current')
  })

  it.each(['/dashboard/history', '/dashboard/history/3'])(
    'marks History as the page on %s',
    (path) => {
      navigation.pathname = path
      render(<DashboardMenu />)

      expect(screen.getByRole('link', { name: 'History' })).toHaveAttribute('aria-current', 'page')
      expect(screen.getByRole('link', { name: 'Read' })).not.toHaveAttribute('aria-current')
    }
  )
})
