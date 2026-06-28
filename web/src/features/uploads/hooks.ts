import { useMutation } from '@tanstack/react-query'
import { uploadPublicAsset } from '../../lib/api/uploads.api'

export function useUploadPublicAsset() {
  return useMutation({
    mutationFn: (file: File) => uploadPublicAsset(file),
  })
}
