import { screen } from '@testing-library/react'
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

    expect(await picker()).toHaveTextContent('Kraken, fine-tuned on Eparchos')
    expect(screen.getByRole('status', { name: 'Selected model' })).toHaveTextContent('kraken')
  })

  it('lists each model with what it does, and selects the one picked', async () => {
    const { user } = setup(vi.fn().mockResolvedValue(Response.json(makeModelList())))

    await user.click(await picker())
    const options = screen.getAllByRole('option')
    expect(options).toHaveLength(3)
    expect(options[0]).toHaveAttribute('aria-selected', 'true')
    expect(options[1]).toHaveTextContent('Kraken, not fine-tuned')
    expect(options[1]).toHaveTextContent('The general PP-OCRv6 model, before we fine-tuned it.')

    await user.click(screen.getByRole('option', { name: /Kraken, not fine-tuned/ }))

    expect(await picker()).toHaveTextContent('Kraken, not fine-tuned')
    expect(screen.getByRole('status', { name: 'Selected model' })).toHaveTextContent('kraken-base')
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

    expect(await picker()).toHaveTextContent('Kraken, fine-tuned on Eparchos')
    expect(fetch).toHaveBeenCalledTimes(2)
  })
})
