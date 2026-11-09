import { expect, test } from '@playwright/test'

test.use({ storageState: { cookies: [], origins: [] } })

const PASSWORD = 'correct horse'

test('guests can access the dashboard without signing in', async ({ page }) => {
  await page.goto('/')

  await expect(page).toHaveURL('/dashboard')
  await expect(page.getByRole('heading', { name: 'Read a manuscript page' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Choose image' })).toBeVisible()
  await expect(page.getByRole('link', { name: 'Sign in', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Sign out' })).toHaveCount(0)
})

test('both account pages offer a guest entry', async ({ page }) => {
  for (const path of ['/sign-in', '/sign-up']) {
    await page.goto(path)
    await page.getByRole('link', { name: 'Continue as guest' }).click()

    await expect(page).toHaveURL('/dashboard')
    await expect(page.getByRole('link', { name: 'Sign in', exact: true })).toBeVisible()
  }
})

test('a new user signs up, signs out and signs back in', async ({ page }) => {
  const email = `e2e-${Date.now()}@example.com`

  await page.goto('/dashboard')
  await page.getByRole('link', { name: 'Sign in', exact: true }).click()

  await page.getByRole('link', { name: 'Create an account' }).click()
  await page.getByLabel('First name').fill('Ada')
  await page.getByLabel('Last name').fill('Lovelace')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password', { exact: true }).fill(PASSWORD)
  await page.getByRole('button', { name: 'Create account' }).click()

  await expect(page).toHaveURL('/dashboard')
  await expect(page.getByText('Signed in as Ada Lovelace')).toBeVisible()

  await page.getByRole('button', { name: 'Sign out' }).click()

  await expect(page).toHaveURL('/dashboard')
  await expect(page.getByRole('link', { name: 'Sign in', exact: true })).toBeVisible()
  await expect(page.getByText('Signed in as Ada Lovelace')).toHaveCount(0)

  await page.reload()
  await expect(page.getByRole('link', { name: 'Sign in', exact: true })).toBeVisible()

  await page.getByRole('link', { name: 'Sign in', exact: true }).click()
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password', { exact: true }).fill('wrong horse')
  await page.getByRole('button', { name: 'Sign in', exact: true }).click()

  await expect(
    page.getByRole('alert').filter({ hasText: 'Wrong email or password.' })
  ).toBeVisible()

  await page.getByLabel('Password', { exact: true }).fill(PASSWORD)
  await page.getByRole('button', { name: 'Sign in', exact: true }).click()

  await expect(page).toHaveURL('/dashboard')
  await expect(page.getByText('Signed in as Ada Lovelace')).toBeVisible()
})

test('an ended session allows continued access as a guest', async ({ page, context }) => {
  await context.addCookies([
    { name: 'lexis_session', value: 'ended', domain: 'localhost', path: '/' },
  ])

  await page.goto('/dashboard')

  await expect(page).toHaveURL('/dashboard')
  await expect(page.getByRole('link', { name: 'Sign in', exact: true })).toBeVisible()

  expect((await context.cookies()).map((cookie) => cookie.name)).not.toContain('lexis_session')

  await page.getByRole('link', { name: 'Sign in', exact: true }).click()
  await expect(page).toHaveURL('/sign-in')
})

test('signed-in users skip the sign-in page', async ({ page }) => {
  await page.goto('/sign-up')
  await page.getByLabel('Email').fill(`e2e-skip-${Date.now()}@example.com`)
  await page.getByLabel('Password', { exact: true }).fill(PASSWORD)
  await page.getByRole('button', { name: 'Create account' }).click()

  await expect(page).toHaveURL('/dashboard')
  await expect(page.getByRole('button', { name: 'Sign out' })).toBeVisible()

  await page.goto('/sign-in')
  await expect(page).toHaveURL('/dashboard')
})
