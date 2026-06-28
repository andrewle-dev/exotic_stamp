import { useAuth } from '../../auth/hooks'
import { Card, CardContent, CardHeader, CardTitle } from '../../../components/ui/Card'
import { PlaceholderPage } from '../../../components/ui/PlaceholderPage'

export function SettingsPage() {
  const { user, profile } = useAuth()

  return (
    <div className="space-y-6">
      <PlaceholderPage
        title="Settings"
        description="Admin profile and frontend preferences. Backend settings API is not available yet."
      />

      <Card>
        <CardHeader>
          <CardTitle>Current Admin Profile</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          <p>
            <span className="text-muted-foreground">Email:</span> {profile?.email ?? user?.email ?? '—'}
          </p>
          <p>
            <span className="text-muted-foreground">Username:</span> {profile?.username ?? user?.username ?? '—'}
          </p>
          <p>
            <span className="text-muted-foreground">Roles:</span>{' '}
            {user?.roles?.length ? user.roles.join(', ') : '—'}
          </p>
        </CardContent>
      </Card>
    </div>
  )
}
