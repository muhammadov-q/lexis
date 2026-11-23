import { expect, test } from '@playwright/test'

const PAGE_PNG = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
  'base64'
)

test('notes a word in the word panel, finds it in the vocabulary and removes it', async ({
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
  await word.getByLabel('Note').fill('First word of the sample page')
  await word.getByRole('button', { name: 'Save word' }).click()
  await expect(word.getByText('Saved')).toBeVisible()
  await word.getByRole('link', { name: 'In vocabulary' }).click()

  await expect(page.getByRole('heading', { level: 1, name: text })).toBeVisible()
  await expect(page.getByLabel('Note')).toHaveValue('First word of the sample page')
  await page.getByLabel('Note').fill('First word of the page')
  await page.getByRole('button', { name: 'Save note' }).click()
  await expect(page.getByText('Saved')).toBeVisible()
  await page.reload()
  await expect(page.getByLabel('Note')).toHaveValue('First word of the page')

  await page.getByRole('link', { name: /, line 1$/ }).click()
  await expect(page).toHaveURL(/\/dashboard\/history\/\d+\?word=0$/)
  await expect(page.getByRole('region', { name: 'Selected word' }).getByLabel('Note')).toHaveValue(
    'First word of the page'
  )

  await page
    .getByRole('navigation', { name: 'Dashboard navigation' })
    .getByRole('link', { name: 'Vocabulary' })
    .click()
  await page.getByRole('list', { name: 'Saved words' }).getByRole('link', { name: text }).click()
  await page.getByRole('button', { name: 'Remove word' }).click()
  await page.getByRole('button', { name: 'Remove', exact: true }).click()

  await expect(page).toHaveURL('/dashboard/vocabulary')
  await expect(page.getByText('No words yet')).toBeVisible()
})
