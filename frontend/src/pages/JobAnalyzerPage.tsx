import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { Briefcase, Plus, Trash2, Eye, Cpu, AlertCircle, CheckCircle, Building2 } from 'lucide-react';

export interface JobDescription {
  id: number;
  companyName: string;
  roleTitle: string;
  rawText: string;
  requiredSkills: string[];
  createdAt: string;
}

export default function JobAnalyzerPage() {
  const [jobs, setJobs] = useState<JobDescription[]>([]);
  const [selectedJob, setSelectedJob] = useState<JobDescription | null>(null);
  const [companyName, setCompanyName] = useState('');
  const [roleTitle, setRoleTitle] = useState('');
  const [rawText, setRawText] = useState('');

  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');

  const fetchJobs = async () => {
    try {
      setLoading(true);
      const response = await api.get<JobDescription[]>('/jobs');
      setJobs(response.data);
      if (response.data.length > 0 && !selectedJob) {
        setSelectedJob(response.data[0]);
      }
    } catch {
      setError('Failed to fetch job descriptions.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobs();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSubmitting(true);

    try {
      const response = await api.post<JobDescription>('/jobs', {
        companyName,
        roleTitle,
        rawText,
      });
      setSuccess(`Added job description for ${companyName} (${roleTitle})`);
      setJobs((prev) => [response.data, ...prev]);
      setSelectedJob(response.data);
      setCompanyName('');
      setRoleTitle('');
      setRawText('');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to create job description.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this job description?')) return;

    try {
      await api.delete(`/jobs/${id}`);
      setJobs((prev) => prev.filter((j) => j.id !== id));
      if (selectedJob?.id === id) {
        setSelectedJob(jobs.find((j) => j.id !== id) || null);
      }
      setSuccess('Job description deleted successfully.');
    } catch {
      setError('Failed to delete job description.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Job Analyzer</h2>
          <p className="page-subtitle">Paste target job postings to analyze requirements and drive AI matching</p>
        </div>
      </div>

      {error && (
        <div className="badge badge-danger" style={{ width: '100%', padding: '0.75rem 1rem', marginBottom: '1.25rem', justifyContent: 'flex-start' }}>
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="badge badge-success" style={{ width: '100%', padding: '0.75rem 1rem', marginBottom: '1.25rem', justifyContent: 'flex-start' }}>
          <CheckCircle size={16} />
          <span>{success}</span>
        </div>
      )}

      <div className="grid-2">
        {/* Form to paste job description */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
            <Plus size={20} color="var(--primary)" />
            <h3 style={{ fontSize: '1.1rem' }}>Add Target Job Posting</h3>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="grid-2" style={{ gap: '1rem', marginBottom: 0 }}>
              <div className="form-group">
                <label className="form-label">Company Name</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="e.g. Google, Amazon, Acme Corp"
                  value={companyName}
                  onChange={(e) => setCompanyName(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Role Title</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="e.g. Backend Software Engineer"
                  value={roleTitle}
                  onChange={(e) => setRoleTitle(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Job Description Text</label>
              <textarea
                className="form-control"
                placeholder="Paste full job description responsibilities and requirements here..."
                style={{ minHeight: '180px' }}
                value={rawText}
                onChange={(e) => setRawText(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={submitting}>
              <Briefcase size={18} />
              <span>{submitting ? 'Parsing Job Requirements...' : 'Save Job Description'}</span>
            </button>
          </form>
        </div>

        {/* List & Preview */}
        <div>
          <h3 style={{ marginBottom: '1rem', fontSize: '1.1rem' }}>Saved Job Postings ({jobs.length})</h3>

          {loading ? (
            <div className="text-muted">Loading target jobs...</div>
          ) : jobs.length === 0 ? (
            <div className="card text-muted" style={{ textAlign: 'center', padding: '3rem' }}>
              No job descriptions saved yet. Paste your first job description on the left to start.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {jobs.map((job) => {
                const isSelected = selectedJob?.id === job.id;
                return (
                  <div
                    key={job.id}
                    className="card"
                    style={{
                      borderColor: isSelected ? 'var(--primary)' : 'var(--border-color)',
                      backgroundColor: isSelected ? 'rgba(99, 102, 241, 0.08)' : 'var(--bg-card)',
                      cursor: 'pointer',
                    }}
                    onClick={() => setSelectedJob(job)}
                  >
                    <div className="flex-between">
                      <div>
                        <div style={{ fontWeight: 700, fontSize: '1.05rem', color: 'var(--text-main)' }}>{job.roleTitle}</div>
                        <div className="flex-gap-2 mt-2" style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                          <Building2 size={14} color="var(--primary)" />
                          <span>{job.companyName}</span>
                          <span>•</span>
                          <span>{new Date(job.createdAt).toLocaleDateString()}</span>
                        </div>
                      </div>

                      <div className="flex-gap-2">
                        <button
                          className="btn btn-secondary"
                          style={{ padding: '0.4rem 0.6rem' }}
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedJob(job);
                          }}
                        >
                          <Eye size={14} />
                        </button>
                        <button
                          className="btn btn-danger"
                          style={{ padding: '0.4rem 0.6rem' }}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDelete(job.id);
                          }}
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </div>

                    <div style={{ marginTop: '0.85rem' }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                        Required Skill Keywords ({job.requiredSkills.length}):
                      </div>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
                        {job.requiredSkills.slice(0, 8).map((skill, i) => (
                          <span key={i} className="badge badge-info" style={{ fontSize: '0.7rem' }}>
                            {skill}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Selected Job Text Inspector Modal / View */}
      {selectedJob && (
        <div className="card mt-4">
          <div className="flex-between" style={{ marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
            <div>
              <h3 style={{ fontSize: '1.2rem' }}>{selectedJob.roleTitle} at {selectedJob.companyName}</h3>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Added {new Date(selectedJob.createdAt).toLocaleString()}
              </div>
            </div>
            <span className="badge badge-primary">Job Inspector</span>
          </div>

          <div style={{ marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem', fontWeight: 600, fontSize: '0.9rem' }}>
              <Cpu size={16} color="var(--primary)" />
              <span>Extracted Requirements ({selectedJob.requiredSkills.length})</span>
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}>
              {selectedJob.requiredSkills.map((skill, i) => (
                <span key={i} className="badge badge-info">
                  {skill}
                </span>
              ))}
              {selectedJob.requiredSkills.length === 0 && (
                <span className="text-muted" style={{ fontSize: '0.85rem' }}>No standard technical keywords extracted.</span>
              )}
            </div>
          </div>

          <div>
            <label className="form-label">Job Posting Text</label>
            <div
              style={{
                backgroundColor: 'var(--bg-input)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-sm)',
                padding: '1rem',
                maxHeight: '300px',
                overflowY: 'auto',
                fontSize: '0.9rem',
                whiteSpace: 'pre-wrap',
                color: 'var(--text-muted)',
              }}
            >
              {selectedJob.rawText}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
