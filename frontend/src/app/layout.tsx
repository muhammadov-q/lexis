import type { Metadata } from 'next'
import { JetBrains_Mono, Noto_Serif, Schibsted_Grotesk } from 'next/font/google'
import { Providers } from '@/components/providers'
import './globals.css'
import 'react-image-crop/dist/ReactCrop.css'

const ui = Schibsted_Grotesk({ variable: '--font-ui', subsets: ['latin'] })
const jetbrainsMono = JetBrains_Mono({ variable: '--font-jetbrains-mono', subsets: ['latin'] })
const greek = Noto_Serif({ variable: '--font-noto-serif', subsets: ['greek', 'greek-ext'] })

export const metadata: Metadata = {
  title: { default: 'Lexis', template: '%s · Lexis' },
  description: 'Recognize, analyze and understand historical manuscripts.',
}

export default function RootLayout({ children }: LayoutProps<'/'>) {
  return (
    <html
      lang="en"
      suppressHydrationWarning
      className={`${ui.variable} ${jetbrainsMono.variable} ${greek.variable}`}
    >
      <body className="flex min-h-svh flex-col antialiased">
        <Providers>{children}</Providers>
      </body>
    </html>
  )
}
