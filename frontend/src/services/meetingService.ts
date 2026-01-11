import api from './api';
import { Meeting, MeetingUploadRequest, ProcessingStatus } from '../types/meeting';
import { PageResponse } from '../types/api';

export const meetingService = {
  uploadTranscript: async (request: MeetingUploadRequest): Promise<Meeting> => {
    const response = await api.post<Meeting>('/api/v1/meetings', request);
    return response.data;
  },

  getAllMeetings: async (page: number = 0, size: number = 20): Promise<PageResponse<Meeting>> => {
    const response = await api.get<PageResponse<Meeting>>('/api/v1/meetings', {
      params: { page, size },
    });
    return response.data;
  },

  getMeetingById: async (id: number): Promise<Meeting> => {
    const response = await api.get<Meeting>(`/api/v1/meetings/${id}`);
    return response.data;
  },

  getProcessingStatus: async (id: number): Promise<ProcessingStatus> => {
    const response = await api.get<ProcessingStatus>(`/api/v1/meetings/${id}/processing-status`);
    return response.data;
  },

  deleteMeeting: async (id: number): Promise<void> => {
    await api.delete(`/api/v1/meetings/${id}`);
  },
};
