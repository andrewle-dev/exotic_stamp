/**
 * @deprecated Prefer `AssetImageFieldCard` for new drawers.
 * Thin compatibility wrapper preserving the previous `label` prop name.
 */
import {
  AssetImageFieldCard,
  type AssetImageFieldCardProps,
  type AssetImageObjectFit,
  type AssetImagePreviewSize,
} from './AssetImageFieldCard'

export type { AssetImageObjectFit, AssetImagePreviewSize }

interface PublicAssetUploadFieldProps extends Omit<AssetImageFieldCardProps, 'title'> {
  label: string
}

export function PublicAssetUploadField({ label, ...rest }: PublicAssetUploadFieldProps) {
  return <AssetImageFieldCard title={label} {...rest} />
}
