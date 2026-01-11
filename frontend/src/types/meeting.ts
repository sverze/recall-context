export interface Meeting {
  id: number;
  meetingDate: string;
  meetingType: string;
  seriesName: string;
  originalFilename: string;
  processingStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  processingError?: string;
  createdAt: string;
  summary?: Summary;
  participants?: Participant[];
  actionItems?: ActionItem[];
  transcriptContent?: string;
}

export interface Summary {
  keyPoints: string[];
  decisions: string[];
  summaryText: string;
  sentiment?: string;
  tone?: string;
}

export interface Participant {
  id: number;
  name: string;
  role?: string;
}

export interface ActionItem {
  id: number;
  description: string;
  assignee?: string;
  dueDate?: string;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
  priority?: 'high' | 'medium' | 'low';
}

export interface MeetingUploadRequest {
  filename: string;
  content: string;
}

export interface ProcessingStatus {
  meetingId: number;
  status: string;
  error?: string;
  progress?: number;
}
