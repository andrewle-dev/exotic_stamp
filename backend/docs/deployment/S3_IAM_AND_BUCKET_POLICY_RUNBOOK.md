# S3 IAM + bucket policy runbook (ExoticStamp media)

## Goal

Production uses `STORAGE_PROVIDER=s3` with public read for `public/*` object keys and private objects under `private/*` (presigned GET only).

## Bucket layout

| Prefix | Visibility | Access |
|--------|------------|--------|
| `public/` | public | CloudFront / bucket public-read for GET |
| `private/` | private | App-issued presigned GET after authz |
| `temporary/` staged under `public/temporary/…` | public until orphaned | same as public |

Public URL formula: `STORAGE_PUBLIC_BASE_URL + "/" + object_key` (no trailing slash on the base).

## Lightsail credentials

**Preferred:** attach an IAM instance role (or container task role) and rely on the AWS SDK default credentials provider chain. Do **not** bake static keys into Docker images, Compose YAML, or git.

**Break-glass:** deliver `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` via a secret store into runtime env only.

## IAM policy (app role — sketch)

Allow on the media bucket ARN:

- `s3:PutObject`, `s3:DeleteObject`, `s3:GetObject`, `s3:HeadObject` on `arn:aws:s3:::BUCKET/public/*` and `…/private/*`
- `s3:ListBucket` (optional, scoped) / `s3:HeadBucket` on the bucket for health probes

Deny `s3:PutBucketPolicy`, ACL mutations, and cross-account grants from the app role.

## Bucket policy (public GET for public prefix — sketch)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadPublicPrefix",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::YOUR_BUCKET/public/*"
    }
  ]
}
```

Block public access settings: allow the public-read policy for `public/*` only; keep `private/*` blocked from anonymous access.

## CORS (if browsers load from bucket/CDN directly)

Allow GET from admin/app origins only. Uploads go through the API, not browser→S3 POST.

## App env

```
STORAGE_PROVIDER=s3
AWS_REGION=ap-southeast-1
AWS_S3_BUCKET=exotic-stamp-media
STORAGE_PUBLIC_BASE_URL=https://cdn.example.com
```

Optional LocalStack only: `AWS_S3_ENDPOINT`, `AWS_S3_PATH_STYLE=true`.

## Remaining ops (manual)

- [ ] Create bucket in target region
- [ ] Attach IAM role to Lightsail instance / service
- [ ] Apply bucket policy + Block Public Access exceptions as designed
- [ ] Point CloudFront (or equivalent) origin to the bucket; set `STORAGE_PUBLIC_BASE_URL`
- [ ] Verify PutObject from the app role and anonymous GET for a `public/` key
- [ ] Verify anonymous GET for a `private/` key fails; presign works
