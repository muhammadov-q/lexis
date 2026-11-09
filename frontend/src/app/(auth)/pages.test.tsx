import { screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '@/test/render'
import AuthLayout from './layout'
import SignInPage, { metadata as signInMetadata } from './sign-in/page'
import SignUpPage, { metadata as signUpMetadata } from './sign-up/page'

vi.mock('next/navigation', () => ({ useRouter: () => ({ replace: vi.fn() }) }))

describe('auth pages', () => {
  it('shows the sign-in form under the Λέξις name', () => {
    renderWithProviders(
      <AuthLayout>
        <SignInPage />
      </AuthLayout>
    )
    expect(screen.getByRole('main')).toHaveTextContent(/^Λέξις/)
    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeInTheDocument()
    expect(signInMetadata.title).toBe('Sign in')
  })

  it('shows the sign-up form', () => {
    renderWithProviders(<SignUpPage />)
    expect(screen.getByRole('heading', { name: 'Create an account' })).toBeInTheDocument()
    expect(signUpMetadata.title).toBe('Create an account')
  })
})
