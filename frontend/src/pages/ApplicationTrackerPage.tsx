import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { CheckSquare, Plus, Trash2, Edit2, ShieldAlert, Building2, Calendar, FileText, X } from 'lucide-react';

export type ApplicationStatus = 'APPLIED' | 'INTERVIEW' | 'REJECTED' | 'OFFER';
export type RejectionReasonSource = 'EMPLOYER_STATED' | 'PATTERN_INFERRED' | 'AI_SPECULATED' | 'NONE';

export interface Application {
  id: number;
  jobId?: number;
  companyName: string;
  roleTitle: string;
  status: ApplicationStatus;
  rejectionReasonSource: RejectionReasonSource;
  rejectionDetails?: string;
  appliedDate: string;
  notes?: string;
  createdAt: string;
}

export default function ApplicationTrackerPage() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const [showModal, setShowModal] = useState<boolean>(false);
  const [editingApp, setEditingApp] = useState<Application | null>(null);

  // Form State
  const [companyName, setCompanyName] = useState('');
  const [roleTitle, setRoleTitle] = useState('');
  const [status, setStatus] = useState<ApplicationStatus>('APPLIED');
  const [rejectionReasonSource, setRejectionReasonSource] = useState<RejectionReasonSource>('NONE');
  const [rejectionDetails, setRejectionDetails] = useState('');
  const [appliedDate, setAppliedDate] = useState(new Date().toISOString().split('T')[0]);
  const [notes, setNotes] = useState('');

  const fetchApplications = async () => {
    try {
      setLoading(true);
      const response = await api.get<Application[]>('/applications');
      setApplications(response.data);
    } catch {
      setError('Failed to load tracked applications.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApplications();
  }, []);

  const openCreateModal = () => {
    setEditingApp(null);
    setCompanyName('');
    setRoleTitle('');
    setStatus('APPLIED');
    setRejectionReasonSource('NONE');
    setRejectionDetails('');
    setAppliedDate(new Date().toISOString().split('T')[0]);
    setNotes('');
    setShowModal(true);
  };

  const openEditModal = (app: Application) => {
    setEditingApp(app);
    setCompanyName(app.companyName);
    setRoleTitle(app.roleTitle);
    setStatus(app.status);
    setRejectionReasonSource(app.rejectionReasonSource);
    setRejectionDetails(app.rejectionDetails || '');
    setAppliedDate(app.appliedDate);
    setNotes(app.notes || '');
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingApp) {
        const response = await api.put<Application>(`/applications/${editingApp.id}`, {
          companyName,
          roleTitle,
          status,
          rejectionReasonSource,
          rejectionDetails,
          appliedDate,
          notes,
        });
        setApplications((prev) => prev.map((a) => (a.id === editingApp.id ? response.data : a)));
      } else {
        const response = await api.post<Application>('/applications', {
          companyName,
          roleTitle,
          status,
          rejectionReasonSource,
          rejectionDetails,
          appliedDate,
          notes,
        });
        setApplications((prev) => [response.data, ...prev]);
      }
      setShowModal(false);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to save application');
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Delete this tracked application?')) return;
    try {
      await api.delete(`/applications/${id}`);
      setApplications((prev) => prev.filter((a) => a.id !== id));
    } catch {
      setError('Failed to delete application');
    }
  };

  const handleStatusChange = async (app: Application, newStatus: ApplicationStatus) => {
    try {
      const newSource = newStatus === 'REJECTED' ? app.rejectionReasonSource : 'NONE';
      const response = await api.put<Application>(`/applications/${app.id}`, {
        status: newStatus,
        rejectionReasonSource: newSource,
      });
      setApplications((prev) => prev.map((a) => (a.id === app.id ? response.data : a)));
    } catch {
      setError('Failed to update application status.');
    }
  };

  const columns: { status: ApplicationStatus; label: string; badgeClass: string }[] = [
    { status: 'APPLIED', label: 'Applied', badgeClass: 'badge-info' },
    { status: 'INTERVIEW', label: 'Interviewing', badgeClass: 'badge-warning' },
    { status: 'OFFER', label: 'Offer Received', badgeClass: 'badge-success' },
    { status: 'REJECTED', label: 'Rejected', badgeClass: 'badge-danger' },
  ];

  const getSourceBadge = (source: RejectionReasonSource) => {
    switch (source) {
      case 'EMPLOYER_STATED':
        return <span className="badge badge-success" style={{ fontSize: '0.65rem' }}>Verified Employer Stated</span>;
      case 'PATTERN_INFERRED':
        return <span className="badge badge-warning" style={{ fontSize: '0.65rem' }}>Pattern Inferred</span>;
      case 'AI_SPECULATED':
        return <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>AI Speculated</span>;
      default:
        return <span className="badge badge-secondary" style={{ fontSize: '0.65rem' }}>Source Unverified</span>;
    }
  };

  return (
    <div>
      <div className="page-header flex-between">
        <div>
          <h2 className="page-title">Application Tracker</h2>
          <p className="page-subtitle">Track status & objective rejection reason attribution</p>
        </div>
        <button onClick={openCreateModal} className="btn btn-primary">
          <Plus size={18} />
          <span>Track New Application</span>
        </button>
      </div>

      {error && (
        <div className="badge badge-danger" style={{ width: '100%', padding: '0.75rem 1rem', marginBottom: '1.25rem', justifyContent: 'flex-start' }}>
          <ShieldAlert size={16} />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="text-muted">Loading applications...</div>
      ) : (
        <div className="grid-4" style={{ alignItems: 'start' }}>
          {columns.map((col) => {
            const appsInCol = applications.filter((a) => a.status === col.status);
            return (
              <div
                key={col.status}
                style={{
                  background: 'rgba(30, 41, 59, 0.5)',
                  border: '1px solid var(--border-color)',
                  borderRadius: 'var(--radius-md)',
                  padding: '1rem',
                  minHeight: '500px',
                }}
              >
                <div className="flex-between" style={{ marginBottom: '1rem', paddingBottom: '0.5rem', borderBottom: '1px solid var(--border-color)' }}>
                  <span className={`badge ${col.badgeClass}`} style={{ fontSize: '0.8rem' }}>
                    {col.label}
                  </span>
                  <span style={{ fontWeight: 700, fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    {appsInCol.length}
                  </span>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                  {appsInCol.map((app) => (
                    <div key={app.id} className="card" style={{ padding: '1rem' }}>
                      <div className="flex-between">
                        <div style={{ fontWeight: 700, fontSize: '0.95rem', color: 'var(--text-main)' }}>{app.roleTitle}</div>
                        <div className="flex-gap-2">
                          <button onClick={() => openEditModal(app)} className="btn btn-secondary" style={{ padding: '0.25rem 0.4rem' }}>
                            <Edit2 size={12} />
                          </button>
                          <button onClick={() => handleDelete(app.id)} className="btn btn-danger" style={{ padding: '0.25rem 0.4rem' }}>
                            <Trash2 size={12} />
                          </button>
                        </div>
                      </div>

                      <div className="flex-gap-2 mt-2" style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        <Building2 size={12} color="var(--primary)" />
                        <span>{app.companyName}</span>
                      </div>

                      <div className="flex-gap-2 mt-2" style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                        <Calendar size={12} />
                        <span>Applied: {app.appliedDate}</span>
                      </div>

                      {app.status === 'REJECTED' && (
                        <div style={{ marginTop: '0.75rem', paddingTop: '0.5rem', borderTop: '1px dashed var(--border-color)' }}>
                          <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>Rejection Source:</div>
                          {getSourceBadge(app.rejectionReasonSource)}
                          {app.rejectionDetails && (
                            <p style={{ fontSize: '0.75rem', color: 'var(--text-dim)', marginTop: '0.35rem', fontStyle: 'italic' }}>
                              "{app.rejectionDetails}"
                            </p>
                          )}
                        </div>
                      )}

                      {app.notes && (
                        <div style={{ marginTop: '0.5rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          Note: {app.notes}
                        </div>
                      )}

                      <div style={{ marginTop: '0.85rem' }}>
                        <select
                          className="form-control"
                          style={{ padding: '0.3rem 0.5rem', fontSize: '0.75rem' }}
                          value={app.status}
                          onChange={(e) => handleStatusChange(app, e.target.value as ApplicationStatus)}
                        >
                          <option value="APPLIED">Move to Applied</option>
                          <option value="INTERVIEW">Move to Interview</option>
                          <option value="OFFER">Move to Offer</option>
                          <option value="REJECTED">Move to Rejected</option>
                        </select>
                      </div>
                    </div>
                  ))}

                  {appsInCol.length === 0 && (
                    <div style={{ textAlign: 'center', color: 'var(--text-dim)', fontSize: '0.8rem', padding: '2rem 0' }}>
                      No applications
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Create / Edit Modal */}
      {showModal && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.75)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 1000,
          }}
        >
          <div className="card" style={{ width: '100%', maxWidth: '500px', padding: '2rem' }}>
            <div className="flex-between" style={{ marginBottom: '1.25rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
              <h3 style={{ fontSize: '1.2rem' }}>{editingApp ? 'Edit Application' : 'Track New Application'}</h3>
              <button onClick={() => setShowModal(false)} className="btn btn-secondary" style={{ padding: '0.3rem' }}>
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Company Name</label>
                <input
                  type="text"
                  className="form-control"
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
                  value={roleTitle}
                  onChange={(e) => setRoleTitle(e.target.value)}
                  required
                />
              </div>

              <div className="grid-2" style={{ gap: '1rem', marginBottom: 0 }}>
                <div className="form-group">
                  <label className="form-label">Status</label>
                  <select
                    className="form-control"
                    value={status}
                    onChange={(e) => setStatus(e.target.value as ApplicationStatus)}
                  >
                    <option value="APPLIED">Applied</option>
                    <option value="INTERVIEW">Interview</option>
                    <option value="OFFER">Offer</option>
                    <option value="REJECTED">Rejected</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Applied Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={appliedDate}
                    onChange={(e) => setAppliedDate(e.target.value)}
                  />
                </div>
              </div>

              {status === 'REJECTED' && (
                <div style={{ background: 'rgba(239, 68, 68, 0.05)', border: '1px solid rgba(239, 68, 68, 0.2)', padding: '1rem', borderRadius: 'var(--radius-sm)', marginBottom: '1.25rem' }}>
                  <div className="form-group">
                    <label className="form-label" style={{ color: 'var(--danger)' }}>Rejection Reason Source (Objective Attribution)</label>
                    <select
                      className="form-control"
                      value={rejectionReasonSource}
                      onChange={(e) => setRejectionReasonSource(e.target.value as RejectionReasonSource)}
                    >
                      <option value="EMPLOYER_STATED">EMPLOYER_STATED (Explicit feedback from company)</option>
                      <option value="PATTERN_INFERRED">PATTERN_INFERRED (Inferred from repeated dataset patterns)</option>
                      <option value="AI_SPECULATED">AI_SPECULATED (Hypothesized based on job text gap)</option>
                      <option value="NONE">NONE (No reason provided)</option>
                    </select>
                  </div>

                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label className="form-label">Rejection Details / Feedback</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="e.g. 'Selected candidate with 5+ years Kubernetes experience'"
                      value={rejectionDetails}
                      onChange={(e) => setRejectionDetails(e.target.value)}
                    />
                  </div>
                </div>
              )}

              <div className="form-group">
                <label className="form-label">Notes</label>
                <textarea
                  className="form-control"
                  style={{ minHeight: '80px' }}
                  placeholder="Referral name, salary range, recruiter contact..."
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                />
              </div>

              <div className="flex-gap-2" style={{ justifyContent: 'flex-end', marginTop: '1.5rem' }}>
                <button type="button" onClick={() => setShowModal(false)} className="btn btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  {editingApp ? 'Save Changes' : 'Track Application'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
