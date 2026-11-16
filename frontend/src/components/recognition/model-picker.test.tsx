import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { useModelChoice } from '@/hooks/use-models'
import { makeModelList } from '@/test/models'
import { renderWithProviders } from '@/test/render'
import { ModelPicker } from './model-picker'

afterEach(() => {
  vi.unstubAllGlobals()
})

function Picker({ disabled }: { disabled?: boolean }) {
  const choice = useModelChoice()
  return (
    <>
      <ModelPicker choice={choice} disabled={disabled} />
      <output aria-label="Selected model">{choice.selected}</output>
    </>
  )
}

function setup(fetch: ReturnType<typeof vi.fn>, props: { disabled?: boolean } = {}) {
  vi.stubGlobal('fetch', fetch)
  renderWithProviders(<Picker {...props} />)
  return { user: userEvent.setup() }
}

const picker = () => screen.findByRole('combobox', { name: 'Model' })

describe('ModelPicker', () => {
  it('shows a placeholder shaped like the dropdown while the models load', () => {
    setup(vi.fn(() => new Promise(() => {})))

    expect(screen.getByText('Model')).toBeInTheDocument()
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument()
  })

  it("shows the backend's default model before anything is chosen", async () => {
    setup(vi.fn().mockResolvedValue(Response.json(makeModelList())))

    const select = await picker()
    expect(select).toHaveTextContent('Greek handwriting')
    expect(select).toHaveAccessibleDescription(
      'Reads the page in about 10 seconds and puts a box around every word.'
    )
    expect(screen.getByRole('status', { name: 'Selected model' })).toHaveTextContent('kraken')
  })

  it('lists the models by name only, and selects the one picked', async () => {
    const { user } = setup(vi.fn().mockResolvedValue(Response.json(makeModelList())))

    await user.click(await picker())
    const options = screen.getAllByRole('option')
    expect(options.map((option) => option.textContent)).toEqual([
      'Greek handwriting',
      'Untrained reader, for comparison',
      'Test output',
    ])
    expect(options[0]).toHaveAttribute('aria-selected', 'true')

    await user.click(screen.getByRole('option', { name: 'Untrained reader, for comparison' }))

    expect(await picker()).toHaveTextContent('Untrained reader, for comparison')
    expect(screen.getByRole('status', { name: 'Selected model' })).toHaveTextContent('kraken-base')
  })

  it('explains the chosen model behind the info button', async () => {
    const { user } = setup(vi.fn().mockResolvedValue(Response.json(makeModelList())))

    await user.click(await screen.findByRole('button', { name: 'About Greek handwriting' }))

    const details = await screen.findByRole('dialog')
    expect(details).toHaveTextContent('Greek handwriting')
    expect(details).toHaveTextContent(
      'Reads the page in about 10 seconds and puts a box around every word.'
    )
    expect(details).not.toHaveTextContent('Demo')
  })

  it('says when the chosen model is a demo', async () => {
    const { user } = setup(vi.fn().mockResolvedValue(Response.json(makeModelList())))

    await user.click(await picker())
    await user.click(screen.getByRole('option', { name: 'Test output' }))
    await user.click(screen.getByRole('button', { name: 'About Test output' }))

    const details = await screen.findByRole('dialog')
    expect(details).toHaveTextContent('Always returns the same six digits, whatever you upload.')
    expect(details).toHaveTextContent("Demo: it doesn't read your page.")
  })

  it('keeps the demos apart from the models that read your page', async () => {
    const { user } = setup(vi.fn().mockResolvedValue(Response.json(makeModelList())))

    await user.click(await picker())

    const readers = screen.getByRole('group', { name: 'Read your page' })
    const demos = screen.getByRole('group', { name: 'Demos' })
    expect(
      within(readers)
        .getAllByRole('option')
        .map((o) => o.textContent)
    ).toEqual([
      expect.stringContaining('Greek handwriting'),
      expect.stringContaining('Untrained reader, for comparison'),
    ])
    expect(within(demos).getByRole('option')).toHaveTextContent('Test output')
  })

  it('shows no demo group when the backend has no demos', async () => {
    const list = makeModelList()
    const readersOnly = makeModelList({ models: list.models.filter((model) => !model.demo) })
    const { user } = setup(vi.fn().mockResolvedValue(Response.json(readersOnly)))

    await user.click(await picker())

    expect(screen.getByRole('group', { name: 'Read your page' })).toBeInTheDocument()
    expect(screen.queryByRole('group', { name: 'Demos' })).not.toBeInTheDocument()
  })

  it('can be locked while a page is being read', async () => {
    setup(vi.fn().mockResolvedValue(Response.json(makeModelList())), { disabled: true })

    expect(await picker()).toBeDisabled()
  })

  it('says the default model reads the page when the list fails, and can reload it', async () => {
    const fetch = vi
      .fn()
      .mockResolvedValueOnce(Response.json({ detail: 'Not reachable' }, { status: 502 }))
      .mockResolvedValueOnce(Response.json(makeModelList()))
    const { user } = setup(fetch)

    expect(await screen.findByText(/Couldn't load the models/)).toHaveTextContent(
      'The default model reads the page.'
    )

    await user.click(screen.getByRole('button', { name: 'Reload' }))

    expect(await picker()).toHaveTextContent('Greek handwriting')
    expect(fetch).toHaveBeenCalledTimes(2)
  })
})
