# Full Platform Roadmap

## After Web Admin Staging / First Release

### Code Work

- Complete S3 production storage path and cleanup jobs
- Add web code-splitting and performance budgets
- Add dedicated web and mobile CI pipelines in the active release repo
- Harden mobile release flavor/build configuration
- Remove Android cleartext production posture
- Finalize mobile production API environment strategy beyond debug overrides

### Cloud Work

- Formalize staging and production Lightsail topology
- Formalize S3 bucket policy, public URL/CDN strategy, and lifecycle
- Add managed monitoring/alerting for backend health and logs
- Add backup schedule and restore drill evidence

### Account / Legal / Identity Work

- Confirm AWS account ownership and operator runbook
- Prepare Google Play Console access and signing ownership
- Prepare Apple Developer / App Store Connect ownership, certificates, and capabilities
- Confirm mail-sending domain/account governance if production emails are used

### Store Listing / Content Work

- Android icon/screenshot/listing/privacy content
- iOS icon/screenshot/listing/privacy content
- App privacy disclosures and support URLs

### Operational Work

- Staging smoke automation
- Load/performance test of backend and upload paths
- Security scan / dependency scan / pentest
- Pilot rollout plan
- Production cutover checklist

## Mobile-Specific Sequence

1. Android internal signed build
2. Google Play internal testing readiness
3. iOS signing and TestFlight readiness
4. Mobile production API cutover rehearsal
5. Monitoring and crash reporting validation
6. Pilot rollout
7. Full production rollout
