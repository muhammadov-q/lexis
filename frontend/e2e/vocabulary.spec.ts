import { expect, test } from '@playwright/test'

const PAGE_PNG = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
  'base64'
)

test('adds a word from a read page to the vocabulary, notes it and removes it', async ({
  page,
}) => {
  await page.goto('/dashboard')
  await page.getByLabel('Page image').setInputFiles({
    name: `vocabulary-${Date.now()}.png`,
    mimeType: 'image/png',
    buffer: PAGE_PNG,
  })
  await page.getByLabel('Model').click()
  await page.getByRole('option', { name: /Sample page/ }).click()
  await page.getByRole('button', { name: 'Recognize page' }).click()

  const firstWord = page
    .getByRole('region', { name: 'Transcript' })
    .getByRole('listitem')
    .first()
    .getByRole('button')
    .first()
  const text = (await firstWord.textContent()) ?? ''
  await firstWord.click()
  const word = page.getByRole('region', { name: 'Selected word' })
  await word.getByRole('button', { name: 'Add to vocabulary' }).click()
  await word.getByRole('link', { name: 'Open it' }).click()

  await expect(page.getByRole('heading', { level: 1, name: text })).toBeVisible()
  await page.getByLabel('Your note').fill('First word of the sample page')
  await page.getByRole('button', { name: 'Save note' }).click()
  await expect(page.getByText('Note saved')).toBeVisible()
  await page.reload()
  await expect(page.getByLabel('Your note')).toHaveValue('First word of the sample page')

  await page.getByRole('link', { name: 'Show on the page' }).click()
  await expect(page).toHaveURL(/\/dashboard\/history\/\d+\?word=0$/)
  await expect(page.getByRole('region', { name: 'Selected word' })).toContainText(text)

  await page
    .getByRole('navigation', { name: 'Dashboard navigation' })
    .getByRole('link', { name: 'Vocabulary' })
    .click()
  await page.getByRole('list', { name: 'Saved words' }).getByRole('link', { name: text }).click()
  await page.getByRole('button', { name: 'Remove from vocabulary' }).click()
  await page.getByRole('button', { name: 'Remove', exact: true }).click()

  await expect(page).toHaveURL('/dashboard/vocabulary')
  await expect(page.getByText('No words yet')).toBeVisible()
})
