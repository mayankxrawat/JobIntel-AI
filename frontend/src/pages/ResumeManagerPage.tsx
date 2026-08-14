import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { Upload, FileText, Trash2, Eye, Cpu, AlertCircle, CheckCircle } from 'lucide-react';

export interface Resume {
  id: number;
  fileName: string;
  rawText: string;
  extractedSkills: string[];
  createdAt: string;
}

export default function ResumeManagerPage() {
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [selectedResume, setSelectedResume] = useState<Resume | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [uploading, setUploading] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');

  const fetchResumes = async () => {
    try {
      setLoading(true);
      const response = await api.get<Resume[]>('/resumes');
      setResumes(response.data);
      if (response.data.length > 0 && !selectedResume) {
        setSelectedResume(response.data[0]);
      }
    } catch (err: any) {
      setError('Failed to fetch uploaded resumes.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResumes();
  }, []);

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    const file = files[0];
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      setError('Please select a valid PDF file.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
      setUploading(true);
      setError('');
      setSuccess('');
      const response = await api.post<Resume>('/resumes/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setSuccess(`Successfully extracted text from ${file.name}!`);
      setResumes((prev) => [response.data, ...prev]);
      setSelectedResume(response.data);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to parse and upload PDF resume.');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this resume?')) return;

    try {
      await api.delete(`/resumes/${id}`);
      setResumes((prev) => prev.filter((r) => r.id !== id));
      if (selectedResume?.id === id) {
        setSelectedResume(resumes.find((r) => r.id !== id) || null);
      }
      setSuccess('Resume deleted successfully.');
    } catch {
      setError('Failed to delete resume.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Resume Manager</h2>
          <p className="page-subtitle">Upload PDF resumes and inspect real-time extracted skills & text</p>
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

      {/* Upload Zone */}
      <div className="card" style={{ marginBottom: '2rem', textAlign: 'center', padding: '2.5rem 1.5rem', borderStyle: 'dashed' }}>
        <input
          type="file"
          id="pdf-upload"
          accept=".pdf"
          style={{ display: 'none' }}
          onChange={handleFileUpload}
          disabled={uploading}
        />
        <label htmlFor="pdf-upload" style={{ cursor: uploading ? 'not-allowed' : 'pointer', display: 'inline-block' }}>
          <div style={{ padding: '1rem', background: 'var(--primary-light)', borderRadius: '50%', color: 'var(--primary)', width: '64px', height: '64px', margin: '0 auto 1rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Upload size={32} />
          </div>
          <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem' }}>
            {uploading ? 'Parsing PDF via Apache PDFBox...' : 'Click to Upload PDF Resume'}
          </h3>
          <p className="text-muted" style={{ fontSize: '0.875rem' }}>
            Only standard readable PDF documents are supported (Max 10MB)
          </p>
        </label>
      </div>

      {/* Resumes Content Grid */}
      {loading ? (
        <div className="text-muted">Loading your resumes...</div>
      ) : resumes.length === 0 ? (
        <div className="card text-muted" style={{ textAlign: 'center', padding: '3rem' }}>
          No resumes uploaded yet. Upload your first PDF resume above to start AI matching.
        </div>
      ) : (
        <div className="grid-2">
          {/* List of uploaded resumes */}
          <div>
            <h3 style={{ marginBottom: '1rem', fontSize: '1.1rem' }}>Your Stored Resumes ({resumes.length})</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {resumes.map((resume) => {
                const isSelected = selectedResume?.id === resume.id;
                return (
                  <div
                    key={resume.id}
                    className="card"
                    style={{
                      borderColor: isSelected ? 'var(--primary)' : 'var(--border-color)',
                      backgroundColor: isSelected ? 'rgba(99, 102, 241, 0.08)' : 'var(--bg-card)',
                      cursor: 'pointer',
                    }}
                    onClick={() => setSelectedResume(resume)}
                  >
                    <div className="flex-between">
                      <div className="flex-gap-2">
                        <FileText size={20} color="var(--primary)" />
                        <div>
                          <div style={{ fontWeight: 600 }}>{resume.fileName}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                            Uploaded {new Date(resume.createdAt).toLocaleDateString()}
                          </div>
                        </div>
                      </div>
                      <div className="flex-gap-2">
                        <button
                          className="btn btn-secondary"
                          style={{ padding: '0.4rem 0.6rem' }}
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedResume(resume);
                          }}
                        >
                          <Eye size={14} />
                        </button>
                        <button
                          className="btn btn-danger"
                          style={{ padding: '0.4rem 0.6rem' }}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDelete(resume.id);
                          }}
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </div>

                    <div style={{ marginTop: '0.85rem' }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                        Detected Skills ({resume.extractedSkills.length}):
                      </div>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
                        {resume.extractedSkills.slice(0, 6).map((skill, i) => (
                          <span key={i} className="badge badge-primary" style={{ fontSize: '0.7rem' }}>
                            {skill}
                          </span>
                        ))}
                        {resume.extractedSkills.length > 6 && (
                          <span className="badge badge-secondary" style={{ fontSize: '0.7rem' }}>
                            +{resume.extractedSkills.length - 6} more
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Extracted Text Inspector */}
          {selectedResume && (
            <div className="card">
              <div className="flex-between" style={{ marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
                <div>
                  <h3 style={{ fontSize: '1.1rem' }}>Extracted Text & Skills</h3>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{selectedResume.fileName}</div>
                </div>
                <span className="badge badge-success">Apache PDFBox</span>
              </div>

              <div style={{ marginBottom: '1.25rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem', fontWeight: 600, fontSize: '0.9rem' }}>
                  <Cpu size={16} color="var(--primary)" />
                  <span>Detected Technical Keywords</span>
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}>
                  {selectedResume.extractedSkills.map((skill, i) => (
                    <span key={i} className="badge badge-info">
                      {skill}
                    </span>
                  ))}
                  {selectedResume.extractedSkills.length === 0 && (
                    <span className="text-muted" style={{ fontSize: '0.85rem' }}>No standard keywords auto-detected in text.</span>
                  )}
                </div>
              </div>

              <div>
                <label className="form-label" style={{ display: 'block', marginBottom: '0.5rem' }}>
                  Parsed Resume Content
                </label>
                <div
                  style={{
                    backgroundColor: 'var(--bg-input)',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-sm)',
                    padding: '1rem',
                    maxHeight: '350px',
                    overflowY: 'auto',
                    fontSize: '0.85rem',
                    whiteSpace: 'pre-wrap',
                    fontFamily: 'monospace',
                    color: 'var(--text-muted)',
                  }}
                >
                  {selectedResume.rawText}
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
