export interface ApiKeyStatusResponse {
  configured: boolean;
  message: string;
}

export interface ApiKeyRequest {
  apiKey: string;
}

export interface ErrorResponse {
  code: string;
  message: string;
  status: number;
  timestamp: string;
  details?: Record<string, string>;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
