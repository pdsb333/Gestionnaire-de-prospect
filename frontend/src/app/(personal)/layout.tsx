import { AppLayout } from '@/components/app-layout'
import '../globals.css'

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <AppLayout>
      {children}
    </AppLayout>
  )
}
