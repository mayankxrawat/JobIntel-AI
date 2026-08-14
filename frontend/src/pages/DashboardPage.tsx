import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { LayoutDashboard, FileText, Briefcase, Target, CheckSquare, BarChart3, Bot, ArrowRight, Sparkles } from 'lucide-react';
import { AnalyticsData } from './AnalyticsPage';

export default function DashboardPage() {
  const [analytics, setAnalytics] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    api.get<AnalyticsData>('/analytics')
      .then((res) => setAnalytics(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const quickActions = [
    { title: 'Upload Resume', desc: 'Extract PDF skills & text', link: '/resumes', icon: FileText, color: '#6366f1' },
    { title: 'Analyze Job', desc: 'Paste target job postings', link: '/jobs', icon: Briefcase, color: '#10b981' },
    { title: 'Match Analysis', desc: 'AI grounded skill scoring', link: '/matches', icon: Target, color: '#3b82f6' },
    { title: 'Track Applications', desc: 'Pipeline & rejection reasons', link: '/applications', icon: CheckSquare, color: '#f59e0b' },
  ];

  return (
    <div>
      <div className="page-header flex-between">
        <div>
          <h2 className="page-title">Executive Dashboard</h2>
          <p className="page-subtitle">AI-powered Job Application Intelligence Overview</p>
        </div>
        <Link to="/assistant" className="btn btn-primary">
          <Bot size={18} />
          <span>Ask AI Assistant</span>
        </Link>
      </div>

      {/* Overview Stat Cards */}
      <div className="grid-4" style={{ marginBottom: '2rem' }}>
        <div className="card">
          <div className="text-muted" style={{ fontSize: '0.85rem' }}>Tracked Applications</div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.35rem 0', fontFamily: 'var(--font-heading)' }}>
            {loading ? '...' : analytics?.totalApplications || 0}
          </div>
          <Link to="/applications" style={{ fontSize: '0.8rem', display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
            <span>Manage Applications</span> <ArrowRight size={12} />
          </Link>
        </div>

        <div className="card">
          <div className="text-muted" style={{ fontSize: '0.85rem' }}>Interview Rate</div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.35rem 0', fontFamily: 'var(--font-heading)', color: 'var(--warning)' }}>
            {loading ? '...' : `${analytics?.interviewRate || 0}%`}
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
            {analytics?.interviewCount || 0} interview opportunities
          </div>
        </div>

        <div className="card">
          <div className="text-muted" style={{ fontSize: '0.85rem' }}>Offer Rate</div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.35rem 0', fontFamily: 'var(--font-heading)', color: 'var(--success)' }}>
            {loading ? '...' : `${analytics?.offerRate || 0}%`}
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
            {analytics?.offerCount || 0} offers received
          </div>
        </div>

        <div className="card">
          <div className="text-muted" style={{ fontSize: '0.85rem' }}>Average Match Score</div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.35rem 0', fontFamily: 'var(--font-heading)', color: 'var(--info)' }}>
            {loading ? '...' : `${analytics?.averageMatchScore || 0}%`}
          </div>
          <Link to="/matches" style={{ fontSize: '0.8rem', display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
            <span>View Match Results</span> <ArrowRight size={12} />
          </Link>
        </div>
      </div>

      {/* Quick Launch Cards */}
      <h3 style={{ fontSize: '1.2rem', marginBottom: '1rem' }}>Platform Features & Workflow</h3>
      <div className="grid-4">
        {quickActions.map((action, i) => {
          const Icon = action.icon;
          return (
            <Link key={i} to={action.link} className="card" style={{ textDecoration: 'none', transition: 'transform 0.2s ease, border-color 0.2s ease' }}>
              <div style={{ padding: '0.75rem', borderRadius: '12px', background: `${action.color}20`, color: action.color, width: 'fit-content', marginBottom: '1rem' }}>
                <Icon size={24} />
              </div>
              <h4 style={{ fontSize: '1.1rem', marginBottom: '0.35rem', color: 'var(--text-main)' }}>{action.title}</h4>
              <p className="text-muted" style={{ fontSize: '0.85rem' }}>{action.desc}</p>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
