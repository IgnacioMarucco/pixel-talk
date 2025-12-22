export interface MediaUploadResponse {
  id: number;
  originalFilename: string;
  storedFilename: string;
  url: string;
  fileSize: number;
  mimeType: string;
  createdAt: string;
}

export interface MediaResponse {
  id: number;
  originalFilename: string;
  storedFilename: string;
  mimeType: string;
  fileSize: number;
  bucketName: string;
  uploaderUserId: number;
  url: string;
  createdAt: string;
}

export interface PresignedUploadRequest {
  originalFilename: string;
  mimeType?: string;
  fileSize?: number;
}

export interface PresignedUploadResponse {
  uploadUrl: string;
  bucketName: string;
  objectKey: string;
  expiresInSeconds: number;
  objectUrl: string;
}

export interface PresignedConfirmRequest {
  storedFilename: string;
  originalFilename: string;
  mimeType?: string;
  fileSize?: number;
}

export interface PresignedDownloadResponse {
  downloadUrl: string;
  expiresInSeconds: number;
}
