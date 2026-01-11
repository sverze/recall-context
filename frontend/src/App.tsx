import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout } from './components/layout/Layout';
import { MeetingList } from './components/meetings/MeetingList';
import { MeetingDetail } from './components/meetings/MeetingDetail';
import { MeetingUpload } from './components/meetings/MeetingUpload';
import { SettingsPage } from './components/settings/SettingsPage';

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<MeetingList />} />
          <Route path="/meetings/:id" element={<MeetingDetail />} />
          <Route path="/upload" element={<MeetingUpload />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;
