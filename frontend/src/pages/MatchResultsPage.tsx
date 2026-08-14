import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { Target, Sparkles, CheckCircle2, XCircle, AlertTriangle, Lightbulb, Trash2, Eye, FileText, Briefcase, ShieldCheck } from 'lucide-react';
import { Resume } from './ResumeManagerPage';
import { JobDescription } from './JobAnalyzerPage';

export interface MatchResult {
  id: number;
  resumeId: number;
  resumeFileName: string;
  jobId: number;
  companyName: string;
  roleTitle: string;
  score: number;
  writtenExplanation: string;
  strongSkills: string[];
  missingSkills: string[];
  weakAreas: string[];
  improvementSuggestions: string[];
  createdAt: string;
}

export default function MatchResultsPage() {
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [jobs, setJobs] = useState<JobDescription[]>([]);
  const [matches, setMatches] = useState<MatchResult[]>([]);
  const [selectedMatch, setSelectedMatch] = useState<MatchResult | null>(null);

  const [selectedResumeId, setSelectedResumeId] = useState<string>('');
  const [selectedJobId, setSelectedJobId] = useState<string>('');

  const [loading, setLoading] = useState<boolean>(true);
  const [analyzing, setAnalyzing] = useState<boolean>(false);
  const [error, setError] = useState<string>('');

  const fetchData = async () => {
    try {
      setLoading(true);
      const [resumesRes, jobsRes, matchesRes] = await Promise.all([
        api.get<Resume[]>('/resumes'),
        api.get<JobDescription[]>('/jobs'),
        api.get<MatchResult[]>('/matches'),
      ]);

      setResumes(resumesRes.data);
      setJobs(jobsRes.data);
      setMatches(matchesRes.data);

      if (resumesRes.data.length > 0) setSelectedResumeId(resumesRes.data[0].id.toString());
      if (jobsRes.data.length > 0) setSelectedJobId(jobsRes.data[0].id.toString());
      if (matchesRes.data.length > 0) setSelectedMatch(matchesRes.data[0]);
    } catch {
      setError('Failed to load resume or job description options.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleRunAnalysis = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedResumeId || !selectedJobId) {
      setError('Please select both a resume and a target job description.');
      return;
    }

    try {
      setAnalyzing(true);
      setError('');
      const response = await api.post<MatchResult>('/matches/analyze', {
        resumeId: Number(selectedResumeId),
        jobId: Number(selectedJobId),
      });

      setMatches((prev) => [response.data, ...prev]);
      setSelectedMatch(response.data);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to analyze resume vs. job match.');
    } finally {
      setAnalyzing(false);
    }
  };

  const handleDeleteMatch = async (id: number) => {
    if (!window.confirm('Delete this match result?')) return;
    try {
      await api.delete(`/matches/${id}`);
      setMatches((prev) => prev.filter((m) => m.id !== id));
      if (selectedMatch?.id === id) {
        setSelectedMatch(matches.find((m) => m.id !== id) || null);
      }
    } catch {
      setError('Failed to delete match result.');
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'var(--success)';
    if (score >= 60) return 'var(--warning)';
    return 'var(--danger)';
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Match Results</h2>
          <p className="page-subtitle">Grounded AI analysis comparing your resume to target job descriptions</p>
        </div>
      </div>

      {error && (
        <div className="badge badge-danger" style={{ width: '100%', padding: '0.75rem 1rem', marginBottom: '1.25rem', justifyContent: 'flex-start' }}>
          <AlertTriangle size={16} />
          <span>{error}</span>
        </div>
      )}

      {/* Selectors & Trigger */}
      <div className="card" style={{ marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>Run Grounded Match Analysis</h3>
        <form onSubmit={handleRunAnalysis}>
          <div className="grid-2" style={{ gap: '1.5rem', marginBottom: '1rem' }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Select PDF Resume</label>
              <select
                className="form-control"
                value={selectedResumeId}
                onChange={(e) => setSelectedResumeId(e.target.value)}
                disabled={resumes.length === 0}
              >
                {resumes.length === 0 && <option value="">No resumes uploaded</option>}
                {resumes.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.fileName} ({new Date(r.createdAt).toLocaleDateString()})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Select Target Job Description</label>
              <select
                className="form-control"
                value={selectedJobId}
                onChange={(e) => setSelectedJobId(e.target.value)}
                disabled={jobs.length === 0}
              >
                {jobs.length === 0 && <option value="">No jobs saved</option>}
                {jobs.map((j) => (
                  <option key={j.id} value={j.id}>
                    {j.roleTitle} at {j.companyName}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={analyzing || resumes.length === 0 || jobs.length === 0}
            style={{ width: '100%', padding: '0.85rem', marginTop: '0.5rem' }}
          >
            <Sparkles size={18} />
            <span>{analyzing ? 'Evaluating Grounded Resume Match...' : 'Analyze Match & Generate Suggestions'}</span>
          </button>
        </form>
      </div>

      {loading ? (
        <div className="text-muted">Loading match history...</div>
      ) : (
        <div className="grid-2" style={{ gridTemplateColumns: '1fr 2fr', alignItems: 'start' }}>
          {/* History Sidebar */}
          <div>
            <h3 style={{ marginBottom: '1rem', fontSize: '1.1rem' }}>Match History ({matches.length})</h3>
            {matches.length === 0 ? (
              <div className="card text-muted" style={{ textAlign: 'center', padding: '2rem' }}>
                No analyses run yet. Select a resume and job above to run your first match!
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {matches.map((match) => {
                  const isSelected = selectedMatch?.id === match.id;
                  return (
                    <div
                      key={match.id}
                      className="card"
                      style={{
                        padding: '1rem',
                        borderColor: isSelected ? 'var(--primary)' : 'var(--border-color)',
                        backgroundColor: isSelected ? 'rgba(99, 102, 241, 0.08)' : 'var(--bg-card)',
                        cursor: 'pointer',
                      }}
                      onClick={() => setSelectedMatch(match)}
                    >
                      <div className="flex-between">
                        <div>
                          <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>{match.roleTitle}</div>
                          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{match.companyName}</div>
                        </div>
                        <div
                          style={{
                            fontSize: '1.1rem',
                            fontWeight: 800,
                            color: getScoreColor(match.score),
                            fontFamily: 'var(--font-heading)',
                          }}
                        >
                          {match.score}%
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Detailed Match Report Inspector */}
          {selectedMatch && (
            <div className="card" style={{ padding: '2rem' }}>
              <div className="flex-between" style={{ marginBottom: '1.5rem', paddingBottom: '1rem', borderBottom: '1px solid var(--border-color)' }}>
                <div>
                  <h3 style={{ fontSize: '1.3rem' }}>{selectedMatch.roleTitle}</h3>
                  <div className="flex-gap-2 mt-2" style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    <Briefcase size={14} />
                    <span>{selectedMatch.companyName}</span>
                    <span>•</span>
                    <FileText size={14} />
                    <span>{selectedMatch.resumeFileName}</span>
                  </div>
                </div>
                <button
                  className="btn btn-danger"
                  style={{ padding: '0.4rem 0.6rem' }}
                  onClick={() => handleDeleteMatch(selectedMatch.id)}
                >
                  <Trash2 size={16} />
                </button>
              </div>

              {/* Score Display Card */}
              <div
                style={{
                  background: 'rgba(15, 23, 42, 0.6)',
                  border: '1px solid var(--border-color)',
                  borderRadius: 'var(--radius-md)',
                  padding: '1.5rem',
                  marginBottom: '1.5rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '1.5rem',
                }}
              >
                <div
                  style={{
                    width: '90px',
                    height: '90px',
                    borderRadius: '50%',
                    border: `4px solid ${getScoreColor(selectedMatch.score)}`,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontFamily: 'var(--font-heading)',
                    fontSize: '1.75rem',
                    fontWeight: 800,
                    color: getScoreColor(selectedMatch.score),
                    flexShrink: 0,
                  }}
                >
                  {selectedMatch.score}%
                </div>
                <div>
                  <h4 style={{ fontSize: '1.1rem', marginBottom: '0.35rem' }}>Grounded Rationale Explanation</h4>
                  <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', lineHeight: '1.6' }}>
                    {selectedMatch.writtenExplanation}
                  </p>
                </div>
              </div>

              {/* Skills Breakdown Grid */}
              <div className="grid-3" style={{ marginBottom: '1.5rem' }}>
                <div className="card" style={{ padding: '1rem', background: 'rgba(16, 185, 129, 0.05)', borderColor: 'rgba(16, 185, 129, 0.2)' }}>
                  <div className="flex-gap-2" style={{ color: 'var(--success)', fontWeight: 600, marginBottom: '0.5rem', fontSize: '0.9rem' }}>
                    <CheckCircle2 size={16} />
                    <span>Strong Matching Skills</span>
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
                    {selectedMatch.strongSkills.map((s, i) => (
                      <span key={i} className="badge badge-success" style={{ fontSize: '0.7rem' }}>
                        {s}
                      </span>
                    ))}
                    {selectedMatch.strongSkills.length === 0 && (
                      <span className="text-dim" style={{ fontSize: '0.8rem' }}>None identified</span>
                    )}
                  </div>
                </div>

                <div className="card" style={{ padding: '1rem', background: 'rgba(239, 68, 68, 0.05)', borderColor: 'rgba(239, 68, 68, 0.2)' }}>
                  <div className="flex-gap-2" style={{ color: 'var(--danger)', fontWeight: 600, marginBottom: '0.5rem', fontSize: '0.9rem' }}>
                    <XCircle size={16} />
                    <span>Missing Skills</span>
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
                    {selectedMatch.missingSkills.map((s, i) => (
                      <span key={i} className="badge badge-danger" style={{ fontSize: '0.7rem' }}>
                        {s}
                      </span>
                    ))}
                    {selectedMatch.missingSkills.length === 0 && (
                      <span className="text-dim" style={{ fontSize: '0.8rem' }}>None missing</span>
                    )}
                  </div>
                </div>

                <div className="card" style={{ padding: '1rem', background: 'rgba(245, 158, 11, 0.05)', borderColor: 'rgba(245, 158, 11, 0.2)' }}>
                  <div className="flex-gap-2" style={{ color: 'var(--warning)', fontWeight: 600, marginBottom: '0.5rem', fontSize: '0.9rem' }}>
                    <AlertTriangle size={16} />
                    <span>Weak / Partial Areas</span>
                  </div>
                  <ul style={{ paddingLeft: '1rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    {selectedMatch.weakAreas.map((w, i) => (
                      <li key={i} style={{ marginBottom: '0.25rem' }}>{w}</li>
                    ))}
                    {selectedMatch.weakAreas.length === 0 && <li>No major weak areas</li>}
                  </ul>
                </div>
              </div>

              {/* Non-hallucination Resume Improvements */}
              <div className="card" style={{ background: 'rgba(99, 102, 241, 0.05)', borderColor: 'rgba(99, 102, 241, 0.3)' }}>
                <div className="flex-between" style={{ marginBottom: '0.75rem' }}>
                  <div className="flex-gap-2" style={{ fontWeight: 700, color: '#818cf8', fontSize: '1rem' }}>
                    <Lightbulb size={18} />
                    <span>Actionable Resume Improvement Suggestions</span>
                  </div>
                  <div className="flex-gap-2 badge badge-primary" style={{ textTransform: 'none' }}>
                    <ShieldCheck size={14} />
                    <span>Strict Skill Truthfulness Enforced</span>
                  </div>
                </div>
                <ul style={{ paddingLeft: '1.2rem', color: 'var(--text-main)', fontSize: '0.9rem', lineHeight: '1.6' }}>
                  {selectedMatch.improvementSuggestions.map((sug, i) => (
                    <li key={i} style={{ marginBottom: '0.5rem' }}>{sug}</li>
                  ))}
                </ul>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
