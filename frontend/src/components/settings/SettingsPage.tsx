import React, { useState, useEffect } from 'react';
import { settingsService } from '../../services/settingsService';
import { Button } from '../common/Button';
import { LoadingSpinner } from '../common/LoadingSpinner';

export const SettingsPage: React.FC = () => {
  const [apiKey, setApiKey] = useState('');
  const [isConfigured, setIsConfigured] = useState(false);
  const [loading, setLoading] = useState(false);
  const [checking, setChecking] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    checkApiKeyStatus();
  }, []);

  const checkApiKeyStatus = async () => {
    try {
      setChecking(true);
      const response = await settingsService.getApiKeyStatus();
      setIsConfigured(response.configured);
    } catch (err) {
      console.error('Error checking API key status:', err);
    } finally {
      setChecking(false);
    }
  };

  const handleSave = async () => {
    if (!apiKey.trim()) {
      setError('Please enter an API key');
      return;
    }

    try {
      setLoading(true);
      setError('');
      setMessage('');

      await settingsService.saveApiKey(apiKey);

      setMessage('API key saved successfully!');
      setApiKey('');
      setIsConfigured(true);

      setTimeout(() => setMessage(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save API key');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete your API key?')) {
      return;
    }

    try {
      setLoading(true);
      setError('');

      await settingsService.deleteApiKey();

      setMessage('API key deleted successfully');
      setIsConfigured(false);

      setTimeout(() => setMessage(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete API key');
    } finally {
      setLoading(false);
    }
  };

  if (checking) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <LoadingSpinner size="lg" message="Loading settings..." />
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-900 mb-6">Settings</h1>

      <div className="bg-white shadow rounded-lg p-6 space-y-6">
        <div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Anthropic API Key</h2>
          <p className="text-sm text-gray-600 mb-4">
            Your API key is stored encrypted and never exposed. Get your API key from{' '}
            <a
              href="https://console.anthropic.com/"
              target="_blank"
              rel="noopener noreferrer"
              className="text-primary-600 hover:underline"
            >
              Anthropic Console
            </a>
            .
          </p>

          {isConfigured && (
            <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-md">
              <p className="text-sm text-green-800">✓ API key is configured</p>
            </div>
          )}

          <div className="space-y-4">
            <div>
              <label htmlFor="apiKey" className="block text-sm font-medium text-gray-700 mb-1">
                API Key
              </label>
              <input
                id="apiKey"
                type="password"
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                placeholder="sk-ant-..."
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                disabled={loading}
              />
            </div>

            {message && (
              <div className="p-3 bg-green-50 border border-green-200 rounded-md">
                <p className="text-sm text-green-800">{message}</p>
              </div>
            )}

            {error && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-md">
                <p className="text-sm text-red-800">{error}</p>
              </div>
            )}

            <div className="flex gap-3">
              <Button onClick={handleSave} disabled={loading}>
                {loading ? 'Saving...' : 'Save API Key'}
              </Button>

              {isConfigured && (
                <Button onClick={handleDelete} variant="danger" disabled={loading}>
                  Delete API Key
                </Button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
