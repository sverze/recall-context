import React from 'react';
import { Link, useLocation } from 'react-router-dom';

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const location = useLocation();

  const isActive = (path: string) => {
    return location.pathname === path || location.pathname.startsWith(path + '/');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <Link to="/" className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-lg">C</span>
              </div>
              <span className="text-xl font-bold text-gray-900">ContextChain</span>
            </Link>

            <nav className="flex space-x-6">
              <Link
                to="/"
                className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                  isActive('/') && !location.pathname.startsWith('/meetings/')
                    ? 'text-primary-600 bg-primary-50'
                    : 'text-gray-700 hover:text-primary-600'
                }`}
              >
                Meetings
              </Link>
              <Link
                to="/upload"
                className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                  isActive('/upload')
                    ? 'text-primary-600 bg-primary-50'
                    : 'text-gray-700 hover:text-primary-600'
                }`}
              >
                Upload
              </Link>
              <Link
                to="/settings"
                className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                  isActive('/settings')
                    ? 'text-primary-600 bg-primary-50'
                    : 'text-gray-700 hover:text-primary-600'
                }`}
              >
                Settings
              </Link>
            </nav>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
    </div>
  );
};
