import React, { useEffect, useState } from 'react';
import { Routes, Route, Link, useLocation, useNavigate } from 'react-router-dom';
import { 
  LayoutDashboard, 
  FileText, 
  Briefcase, 
  Target, 
  CheckSquare, 
  BarChart3, 
  Bot, 
  Activity,
  Sparkles,
  LogOut,
  User as UserIcon
} from 'lucide-react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { PrivateRoute } from './components/PrivateRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import ResumeManagerPage from './pages/ResumeManagerPage';
import JobAnalyzerPage from './pages/JobAnalyzerPage';
import MatchResultsPage from './pages/MatchResultsPage';
import ApplicationTrackerPage from './pages/ApplicationTrackerPage';
import AnalyticsPage from './pages/AnalyticsPage';
import AssistantPage from './pages/AssistantPage';
import { checkHealth, HealthResponse } from './services/api';

function LandingPage() {
  const { isAuthenticated } = useAuth();
  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ textAlign: 'center', margin: '4rem 0' }}>
        <h1 style={{ fontSize: '3rem', marginBottom: '1rem', color: 'var(--text-main)' }}>
          AI Job Application <span style={{ color: 'var(--primary)' }}>Intelligence Platform</span>
        </h1>
        <p style={{ fontSize: '1.25rem', color: 'var(--text-muted)', maxWidth: '700px', margin: '0 auto 2rem' }}>
          Data-grounded AI resume matching, application tracking with objective rejection attribution, and real-time career analytics.
        </p>
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
          {isAuthenticated ? (
            <Link to="/dashboard" className="btn btn-primary" style={{ padding: '0.85rem 2rem', fontSize: '1rem' }}>
              Go to Dashboard
            </Link>
          ) : (
            <>
              <Link to="/register" className="btn btn-primary" style={{ padding: '0.85rem 2rem', fontSize: '1rem' }}>
                Create Free Account
              </Link>
              <Link to="/login" className="btn btn-secondary" style={{ padding: '0.85rem 2rem', fontSize: '1rem' }}>
                Sign In
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

function StubPage({ title, description }: { title: string; description: string }) {
  return (
    <div className="card mt-4">
      <div className="page-header">
        <div>
          <h2 className="page-title">{title}</h2>
          <p className="page-subtitle">{description}</p>
        </div>
        <span className="badge badge-success">Auth Protected</span>
      </div>
      <p className="text-muted">Module configured and accessible to authenticated user.</p>
    </div>
  );
}

function MainLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [healthStatus, setHealthStatus] = useState<'loading' | 'online' | 'offline'>('loading');

  useEffect(() => {
    checkHealth()
      .then(() => setHealthStatus('online'))
      .catch(() => setHealthStatus('offline'));
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/resumes', label: 'Resume Manager', icon: FileText },
    { path: '/jobs', label: 'Job Analyzer', icon: Briefcase },
    { path: '/matches', label: 'Match Results', icon: Target },
    { path: '/applications', label: 'Application Tracker', icon: CheckSquare },
    { path: '/analytics', label: 'Analytics', icon: BarChart3 },
    { path: '/assistant', label: 'AI Assistant', icon: Bot },
  ];

  const isPublicPage = location.pathname === '/login' || location.pathname === '/register' || location.pathname === '/';

  if (isPublicPage) {
    return (
      <main style={{ width: '100%' }}>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Routes>
      </main>
    );
  }

  return (
    <div className="app-container">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <Sparkles size={24} />
          <span>JobIntel AI</span>
        </div>

        {user && (
          <div style={{ padding: '0.75rem', background: 'rgba(255,255,255,0.03)', borderRadius: 'var(--radius-sm)', marginBottom: '1.25rem', border: '1px solid var(--border-color)' }}>
            <div className="flex-gap-2">
              <UserIcon size={16} color="var(--primary)" />
              <span style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--text-main)' }}>{user.fullName}</span>
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {user.email}
            </div>
          </div>
        )}

        <nav className="nav-menu">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`nav-item ${isActive ? 'active' : ''}`}
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div style={{ marginTop: 'auto', paddingTop: '1rem', borderTop: '1px solid var(--border-color)', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <button onClick={handleLogout} className="btn btn-secondary" style={{ width: '100%', justifyContent: 'flex-start' }}>
            <LogOut size={16} />
            <span>Sign Out</span>
          </button>
          
          <div className="flex-between" style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            <div className="flex-gap-2">
              <Activity size={14} color={healthStatus === 'online' ? '#10b981' : '#ef4444'} />
              <span>Backend API:</span>
            </div>
            <span className={`badge ${healthStatus === 'online' ? 'badge-success' : 'badge-danger'}`}>
              {healthStatus.toUpperCase()}
            </span>
          </div>
        </div>
      </aside>

      <main className="main-content">
        <Routes>
          <Route element={<PrivateRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/resumes" element={<ResumeManagerPage />} />
            <Route path="/jobs" element={<JobAnalyzerPage />} />
            <Route path="/matches" element={<MatchResultsPage />} />
            <Route path="/applications" element={<ApplicationTrackerPage />} />
            <Route path="/analytics" element={<AnalyticsPage />} />
            <Route path="/assistant" element={<AssistantPage />} />
          </Route>
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <MainLayout />
    </AuthProvider>
  );
}
