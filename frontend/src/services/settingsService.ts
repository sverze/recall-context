import api from './api';
import { ApiKeyStatusResponse, ApiKeyRequest } from '../types/api';

export const settingsService = {
  saveApiKey: async (apiKey: string): Promise<ApiKeyStatusResponse> => {
    const request: ApiKeyRequest = { apiKey };
    const response = await api.post<ApiKeyStatusResponse>('/api/v1/settings/api-key', request);
    return response.data;
  },

  getApiKeyStatus: async (): Promise<ApiKeyStatusResponse> => {
    const response = await api.get<ApiKeyStatusResponse>('/api/v1/settings/api-key/status');
    return response.data;
  },

  deleteApiKey: async (): Promise<ApiKeyStatusResponse> => {
    const response = await api.delete<ApiKeyStatusResponse>('/api/v1/settings/api-key');
    return response.data;
  },
};
