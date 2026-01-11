import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { meetingService } from '../../services/meetingService';
import { Meeting } from '../../types/meeting';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { format } from 'date-fns';

export const MeetingDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [meeting, setMeeting] = useState<Meeting | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showTranscript, setShowTranscript] = useState(false);

  useEffect(() => {
    loadMeeting();
  }, [id]);

  const loadMeeting = async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await meetingService.getMeetingById(parseInt(id));
      setMeeting(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load meeting');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <LoadingSpinner size="lg" message="Loading meeting..." />
      </div>
    );
  }

  if (error || !meeting) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-800">{error || 'Meeting not found'}</p>
        </div>
      </div>
    );
  }

  const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'PPpp');
  };

  const getSentimentColor = (sentiment?: string) => {
    switch (sentiment) {
      case 'positive': return 'bg-green-100 text-green-800';
      case 'negative': return 'bg-red-100 text-red-800';
      case 'mixed': return 'bg-yellow-100 text-yellow-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'bg-green-100 text-green-800';
      case 'NOT_STARTED': return 'bg-gray-100 text-gray-800';
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800';
      case 'BLOCKED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              {meeting.meetingType}: {meeting.seriesName}
            </h1>
            <p className="text-sm text-gray-600 mt-1">{formatDate(meeting.meetingDate)}</p>
          </div>
          <span className={`px-3 py-1 rounded-full text-sm font-medium ${
            meeting.processingStatus === 'COMPLETED'
              ? 'bg-green-100 text-green-800'
              : meeting.processingStatus === 'FAILED'
              ? 'bg-red-100 text-red-800'
              : 'bg-yellow-100 text-yellow-800'
          }`}>
            {meeting.processingStatus}
          </span>
        </div>

        {meeting.processingError && (
          <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-md">
            <p className="text-sm text-red-800">{meeting.processingError}</p>
          </div>
        )}
      </div>

      {/* Participants */}
      {meeting.participants && meeting.participants.length > 0 && (
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-3">Participants</h2>
          <div className="flex flex-wrap gap-2">
            {meeting.participants.map((p) => (
              <span key={p.id} className="px-3 py-1 bg-gray-100 text-gray-800 rounded-full text-sm">
                {p.name} {p.role && `(${p.role})`}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Summary */}
      {meeting.summary && (
        <div className="bg-white shadow rounded-lg p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Summary</h2>
            <div className="flex gap-2">
              {meeting.summary.sentiment && (
                <span className={`px-3 py-1 rounded-full text-xs font-medium ${getSentimentColor(meeting.summary.sentiment)}`}>
                  {meeting.summary.sentiment}
                </span>
              )}
              {meeting.summary.tone && (
                <span className="px-3 py-1 bg-gray-100 text-gray-800 rounded-full text-xs font-medium">
                  {meeting.summary.tone}
                </span>
              )}
            </div>
          </div>

          <p className="text-gray-700 mb-4">{meeting.summary.summaryText}</p>

          <div className="space-y-4">
            {meeting.summary.keyPoints.length > 0 && (
              <div>
                <h3 className="font-medium text-gray-900 mb-2">Key Points</h3>
                <ul className="list-disc list-inside space-y-1">
                  {meeting.summary.keyPoints.map((point, idx) => (
                    <li key={idx} className="text-gray-700 text-sm">{point}</li>
                  ))}
                </ul>
              </div>
            )}

            {meeting.summary.decisions.length > 0 && (
              <div>
                <h3 className="font-medium text-gray-900 mb-2">Decisions</h3>
                <ul className="list-disc list-inside space-y-1">
                  {meeting.summary.decisions.map((decision, idx) => (
                    <li key={idx} className="text-gray-700 text-sm">{decision}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Action Items */}
      {meeting.actionItems && meeting.actionItems.length > 0 && (
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Action Items</h2>
          <div className="space-y-3">
            {meeting.actionItems.map((action) => (
              <div key={action.id} className="border border-gray-200 rounded-lg p-4">
                <div className="flex justify-between items-start mb-2">
                  <p className="text-gray-900 flex-1">{action.description}</p>
                  <span className={`ml-2 px-2 py-1 rounded text-xs font-medium ${getStatusColor(action.status)}`}>
                    {action.status.replace('_', ' ')}
                  </span>
                </div>
                <div className="flex gap-4 text-sm text-gray-600">
                  {action.assignee && <span>Assignee: {action.assignee}</span>}
                  {action.dueDate && <span>Due: {action.dueDate}</span>}
                  {action.priority && (
                    <span className={`font-medium ${
                      action.priority === 'high' ? 'text-red-600' :
                      action.priority === 'medium' ? 'text-yellow-600' :
                      'text-gray-600'
                    }`}>
                      {action.priority} priority
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Transcript */}
      <div className="bg-white shadow rounded-lg p-6">
        <button
          onClick={() => setShowTranscript(!showTranscript)}
          className="w-full flex justify-between items-center text-left"
        >
          <h2 className="text-lg font-semibold text-gray-900">Transcript</h2>
          <svg
            className={`w-5 h-5 text-gray-500 transition-transform ${showTranscript ? 'transform rotate-180' : ''}`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </button>

        {showTranscript && (
          <div className="mt-4 p-4 bg-gray-50 rounded-lg">
            <pre className="text-sm text-gray-700 whitespace-pre-wrap font-mono">
              {meeting.transcriptContent}
            </pre>
          </div>
        )}
      </div>
    </div>
  );
};
