import type { ReactNode } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card'

interface PlaceholderPageProps {
  title: string
  description: string
  children?: ReactNode
}

export function PlaceholderPage({ title, description, children }: PlaceholderPageProps) {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
        <p className="mt-1 text-sm text-muted-foreground">{description}</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Coming in next milestone</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">
            This module route is wired into the admin shell. Data tables, forms, and API integration
            will be implemented in subsequent milestones.
          </p>
          {children}
        </CardContent>
      </Card>
    </div>
  )
}
