import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { meetingService } from '../../services/meetingService';
import { Meeting } from '../../types/meeting';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { format } from 'date-fns';

export const MeetingList: React.FC = () => {
  const [meetings, setMeetings] = useState<Meeting[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadMeetings();
  }, []);

  const loadMeetings = async () => {
    try {
      setLoading(true);
      const response = await meetingService.getAllMeetings(0, 20);
      setMeetings(response.content);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load meetings');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <LoadingSpinner size="lg" message="Loading meetings..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-800">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Meetings</h1>
        <Link
          to="/upload"
          className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
        >
          Upload New
        </Link>
      </div>

      {meetings.length === 0 ? (
        <div className="bg-white shadow rounded-lg p-8 text-center">
          <p className="text-gray-600 mb-4">No meetings yet</p>
          <Link
            to="/upload"
            className="text-primary-600 hover:text-primary-700 font-medium"
          >
            Upload your first transcript
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {meetings.map((meeting) => (
            <Link
              key={meeting.id}
              to={`/meetings/${meeting.id}`}
              className="block bg-white shadow rounded-lg p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex justify-between items-start mb-2">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">
                    {meeting.meetingType}: {meeting.seriesName}
                  </h3>
                  <p className="text-sm text-gray-600">
                    {format(new Date(meeting.meetingDate), 'PPpp')}
                  </p>
                </div>
                <span
                  className={`px-3 py-1 rounded-full text-xs font-medium ${
                    meeting.processingStatus === 'COMPLETED'
                      ? 'bg-green-100 text-green-800'
                      : meeting.processingStatus === 'FAILED'
                      ? 'bg-red-100 text-red-800'
                      : 'bg-yellow-100 text-yellow-800'
                  }`}
                >
                  {meeting.processingStatus}
                </span>
              </div>

              {meeting.summary && (
                <p className="text-sm text-gray-700 mt-2 line-clamp-2">
                  {meeting.summary.summaryText}
                </p>
              )}

              {meeting.actionItems && meeting.actionItems.length > 0 && (
                <p className="text-xs text-gray-500 mt-2">
                  {meeting.actionItems.length} action item{meeting.actionItems.length !== 1 ? 's' : ''}
                </p>
              )}
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};
