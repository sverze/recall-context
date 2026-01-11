import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { meetingService } from '../../services/meetingService';
import { Button } from '../common/Button';
import { LoadingSpinner } from '../common/LoadingSpinner';

export const MeetingUpload: React.FC = () => {
  const navigate = useNavigate();
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [dragActive, setDragActive] = useState(false);

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelection(e.dataTransfer.files[0]);
    }
  };

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleFileSelection(e.target.files[0]);
    }
  };

  const handleFileSelection = (selectedFile: File) => {
    if (!selectedFile.name.endsWith('.txt')) {
      setError('Please select a .txt file');
      return;
    }

    setFile(selectedFile);
    setError('');
  };

  const handleUpload = async () => {
    if (!file) {
      setError('Please select a file');
      return;
    }

    try {
      setLoading(true);
      setError('');

      const content = await file.text();

      const meeting = await meetingService.uploadTranscript({
        filename: file.name,
        content,
      });

      navigate(`/meetings/${meeting.id}`);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Failed to upload transcript';
      setError(errorMessage);

      if (errorMessage.includes('API key')) {
        setTimeout(() => {
          if (confirm('API key not configured. Go to settings?')) {
            navigate('/settings');
          }
        }, 500);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-900 mb-6">Upload Meeting Transcript</h1>

      <div className="bg-white shadow rounded-lg p-6 space-y-6">
        <div>
          <h2 className="text-lg font-semibold text-gray-900 mb-2">File Naming Convention</h2>
          <p className="text-sm text-gray-600 mb-4">
            Use the format: <code className="bg-gray-100 px-2 py-1 rounded">YYYY-MM-DD_HHmm_MeetingType_SeriesName.txt</code>
          </p>
          <p className="text-xs text-gray-500">
            Example: <code className="bg-gray-100 px-1.5 py-0.5 rounded">2026-01-11_1400_OneOnOne_WeeklySync.txt</code>
          </p>
        </div>

        <div
          className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
            dragActive ? 'border-primary-500 bg-primary-50' : 'border-gray-300'
          }`}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
        >
          <input
            type="file"
            id="fileInput"
            accept=".txt"
            onChange={handleFileInput}
            className="hidden"
          />

          {!file ? (
            <div>
              <svg
                className="mx-auto h-12 w-12 text-gray-400"
                stroke="currentColor"
                fill="none"
                viewBox="0 0 48 48"
              >
                <path
                  d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02"
                  strokeWidth={2}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
              <p className="mt-2 text-sm text-gray-600">
                Drag and drop your transcript file here, or
              </p>
              <label htmlFor="fileInput" className="mt-2 inline-block">
                <span className="cursor-pointer text-primary-600 hover:text-primary-700 font-medium">
                  browse
                </span>
              </label>
              <p className="mt-1 text-xs text-gray-500">TXT files only</p>
            </div>
          ) : (
            <div>
              <p className="text-sm font-medium text-gray-900">{file.name}</p>
              <p className="text-xs text-gray-500 mt-1">{(file.size / 1024).toFixed(2)} KB</p>
              <button
                onClick={() => setFile(null)}
                className="mt-2 text-sm text-red-600 hover:text-red-700"
              >
                Remove file
              </button>
            </div>
          )}
        </div>

        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-md">
            <p className="text-sm text-red-800">{error}</p>
          </div>
        )}

        <div>
          <Button
            onClick={handleUpload}
            disabled={!file || loading}
            className="w-full"
          >
            {loading ? 'Uploading and Processing...' : 'Upload & Analyze'}
          </Button>
        </div>

        {loading && (
          <div className="flex justify-center py-4">
            <LoadingSpinner size="md" message="Processing transcript with AI..." />
          </div>
        )}
      </div>
    </div>
  );
};
