import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { BarChart3, TrendingUp, Award, Target, AlertCircle, Briefcase, CheckCircle2 } from 'lucide-react';

export interface MissingSkillFrequency {
  skill: string;
  count: number;
}

export interface RoleAnalytics {
  roleTitle: string;
  totalApplications: number;
  interviewCount: number;
  offerCount: number;
  interviewRate: number;
}

export interface AnalyticsData {
  totalApplications: number;
  interviewCount: number;
  offerCount: number;
  interviewRate: number;
  offerRate: number;
  averageMatchScore: number;
  topMissingSkills: MissingSkillFrequency[];
  statusBreakdown: Record<string, number>;
  roleBreakdown: RoleAnalytics[];
}

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');

  const fetchAnalytics = async () => {
    try {
      setLoading(true);
      const response = await api.get<AnalyticsData>('/analytics');
      setAnalytics(response.data);
    } catch {
      setError('Failed to compute analytics from stored data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalytics();
  }, []);

  if (loading) return <div className="text-muted">Calculating real dataset metrics...</div>;

  if (error || !analytics) {
    return (
      <div className="badge badge-danger" style={{ width: '100%', padding: '1rem' }}>
        <AlertCircle size={16} />
        <span>{error || 'No analytics data available.'}</span>
      </div>
    );
  }

  const maxMissingCount = analytics.topMissingSkills.length > 0 ? Math.max(...analytics.topMissingSkills.map((s) => s.count)) : 1;

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Analytics</h2>
          <p className="page-subtitle">Real stored data metrics — computed directly from your tracking history</p>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid-4" style={{ marginBottom: '2rem' }}>
        <div className="card">
          <div className="flex-between">
            <span className="text-muted" style={{ fontSize: '0.85rem' }}>Total Applications</span>
            <Briefcase size={20} color="var(--primary)" />
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.5rem 0 0.25rem', fontFamily: 'var(--font-heading)' }}>
            {analytics.totalApplications}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Stored tracking entries</div>
        </div>

        <div className="card">
          <div className="flex-between">
            <span className="text-muted" style={{ fontSize: '0.85rem' }}>Interview Rate</span>
            <TrendingUp size={20} color="var(--warning)" />
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.5rem 0 0.25rem', fontFamily: 'var(--font-heading)', color: 'var(--warning)' }}>
            {analytics.interviewRate}%
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>{analytics.interviewCount} interview opportunities</div>
        </div>

        <div className="card">
          <div className="flex-between">
            <span className="text-muted" style={{ fontSize: '0.85rem' }}>Offer Rate</span>
            <Award size={20} color="var(--success)" />
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.5rem 0 0.25rem', fontFamily: 'var(--font-heading)', color: 'var(--success)' }}>
            {analytics.offerRate}%
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>{analytics.offerCount} offer decisions</div>
        </div>

        <div className="card">
          <div className="flex-between">
            <span className="text-muted" style={{ fontSize: '0.85rem' }}>Avg Resume Match</span>
            <Target size={20} color="var(--info)" />
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.5rem 0 0.25rem', fontFamily: 'var(--font-heading)', color: 'var(--info)' }}>
            {analytics.averageMatchScore}%
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Across analyzed job postings</div>
        </div>
      </div>

      <div className="grid-2" style={{ marginBottom: '2rem' }}>
        {/* Most Frequently Missing Skills */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', marginBottom: '1.25rem' }}>Most Frequently Missing Skills</h3>
          {analytics.topMissingSkills.length === 0 ? (
            <div className="text-muted" style={{ fontSize: '0.9rem', textAlign: 'center', padding: '2rem 0' }}>
              Run match analyses on target jobs to identify skill gap trends.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {analytics.topMissingSkills.map((item, i) => {
                const percent = Math.round((item.count / maxMissingCount) * 100);
                return (
                  <div key={i}>
                    <div className="flex-between" style={{ fontSize: '0.9rem', marginBottom: '0.35rem' }}>
                      <span style={{ fontWeight: 600 }}>{item.skill}</span>
                      <span className="text-muted" style={{ fontSize: '0.8rem' }}>
                        Missing in {item.count} job(s)
                      </span>
                    </div>
                    <div style={{ height: '8px', background: 'var(--bg-input)', borderRadius: '999px', overflow: 'hidden' }}>
                      <div
                        style={{
                          width: `${percent}%`,
                          height: '100%',
                          background: 'linear-gradient(90deg, var(--danger), var(--warning))',
                          borderRadius: '999px',
                        }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Application Status Pipeline Overview */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', marginBottom: '1.25rem' }}>Application Status Overview</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {Object.entries(analytics.statusBreakdown).map(([status, count]) => {
              const total = analytics.totalApplications || 1;
              const pct = Math.round((count / total) * 100);
              return (
                <div key={status}>
                  <div className="flex-between" style={{ fontSize: '0.9rem', marginBottom: '0.35rem' }}>
                    <span style={{ fontWeight: 600 }}>{status}</span>
                    <span className="text-muted" style={{ fontSize: '0.8rem' }}>
                      {count} ({pct}%)
                    </span>
                  </div>
                  <div style={{ height: '8px', background: 'var(--bg-input)', borderRadius: '999px', overflow: 'hidden' }}>
                    <div
                      style={{
                        width: `${pct}%`,
                        height: '100%',
                        background:
                          status === 'OFFER'
                            ? 'var(--success)'
                            : status === 'INTERVIEW'
                            ? 'var(--warning)'
                            : status === 'APPLIED'
                            ? 'var(--info)'
                            : 'var(--danger)',
                        borderRadius: '999px',
                      }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Role Breakdown Table */}
      <div className="card">
        <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>Performance Breakdown by Role</h3>
        {analytics.roleBreakdown.length === 0 ? (
          <div className="text-muted" style={{ textAlign: 'center', padding: '2rem 0' }}>
            No applications logged for role breakdown.
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '0.75rem' }}>Role Title</th>
                <th style={{ padding: '0.75rem' }}>Applications</th>
                <th style={{ padding: '0.75rem' }}>Interviews</th>
                <th style={{ padding: '0.75rem' }}>Offers</th>
                <th style={{ padding: '0.75rem' }}>Interview Conversion Rate</th>
              </tr>
            </thead>
            <tbody>
              {analytics.roleBreakdown.map((r, i) => (
                <tr key={i} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '0.75rem', fontWeight: 600 }}>{r.roleTitle}</td>
                  <td style={{ padding: '0.75rem' }}>{r.totalApplications}</td>
                  <td style={{ padding: '0.75rem' }}>{r.interviewCount}</td>
                  <td style={{ padding: '0.75rem' }}>{r.offerCount}</td>
                  <td style={{ padding: '0.75rem', color: r.interviewRate > 0 ? 'var(--warning)' : 'var(--text-dim)', fontWeight: 600 }}>
                    {r.interviewRate}%
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
