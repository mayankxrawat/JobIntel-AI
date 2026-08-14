import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { Bot, Send, ShieldCheck, Sparkles, User, AlertCircle, HelpCircle } from 'lucide-react';

export interface AssistantQuery {
  id: number;
  prompt: String;
  response: String;
  groundingDataJson: String;
  createdAt: string;
}

export default function AssistantPage() {
  const [history, setHistory] = useState<AssistantQuery[]>([]);
  const [prompt, setPrompt] = useState('');
  const [loading, setLoading] = useState<boolean>(true);
  const [asking, setAsking] = useState<boolean>(false);
  const [error, setError] = useState<string>('');

  const fetchHistory = async () => {
    try {
      setLoading(true);
      const response = await api.get<AssistantQuery[]>('/assistant/history');
      setHistory(response.data);
    } catch {
      setError('Failed to fetch AI assistant conversation history.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const handleAsk = async (questionText: string) => {
    if (!questionText.trim()) return;

    try {
      setAsking(true);
      setError('');
      const response = await api.post<AssistantQuery>('/assistant/ask', {
        prompt: questionText.trim(),
      });
      setHistory((prev) => [response.data, ...prev]);
      setPrompt('');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to query AI Assistant.');
    } finally {
      setAsking(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleAsk(prompt);
  };

  const quickPrompts = [
    'Which skills am I missing most?',
    'Why is my interview rate low?',
    'Summarize my job application progress',
    'Which roles yield my highest match scores?',
  ];

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
      <div className="page-header flex-between">
        <div>
          <h2 className="page-title">AI Career Assistant</h2>
          <p className="page-subtitle">Data-grounded career advisor — queries your stored records before answering</p>
        </div>
        <div className="badge badge-success flex-gap-2" style={{ padding: '0.5rem 0.85rem' }}>
          <ShieldCheck size={16} />
          <span>Anti-Hallucination Guardrails Active</span>
        </div>
      </div>

      {error && (
        <div className="badge badge-danger" style={{ width: '100%', padding: '0.75rem 1rem', marginBottom: '1.25rem', justifyContent: 'flex-start' }}>
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      {/* Suggested Prompts Bar */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem 1.25rem' }}>
        <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
          <HelpCircle size={14} color="var(--primary)" />
          <span>Recommended Data-Grounded Questions:</span>
        </div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
          {quickPrompts.map((qp, i) => (
            <button
              key={i}
              className="btn btn-secondary"
              style={{ fontSize: '0.8rem', padding: '0.4rem 0.75rem', borderRadius: '999px' }}
              onClick={() => {
                setPrompt(qp);
                handleAsk(qp);
              }}
              disabled={asking}
            >
              <Sparkles size={12} color="var(--primary)" />
              <span>{qp}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Ask Input Card */}
      <div className="card" style={{ marginBottom: '2rem' }}>
        <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '0.75rem' }}>
          <input
            type="text"
            className="form-control"
            placeholder="Ask a question about your job applications, match scores, or missing skills..."
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            disabled={asking}
            required
            style={{ flex: 1 }}
          />
          <button type="submit" className="btn btn-primary" disabled={asking || !prompt.trim()}>
            <Send size={18} />
            <span>{asking ? 'Analyzing Data...' : 'Ask Assistant'}</span>
          </button>
        </form>
      </div>

      {/* Conversation Thread */}
      {loading ? (
        <div className="text-muted">Loading assistant thread...</div>
      ) : history.length === 0 ? (
        <div className="card text-muted" style={{ textAlign: 'center', padding: '3rem' }}>
          No questions asked yet. Choose a suggested question above or type your own to query your stored application data.
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {history.map((q) => (
            <div key={q.id} className="card" style={{ padding: '1.5rem' }}>
              {/* Question bubble */}
              <div className="flex-gap-2" style={{ marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
                <div style={{ background: 'var(--primary-light)', color: 'var(--primary)', padding: '0.5rem', borderRadius: '50%', display: 'flex' }}>
                  <User size={16} />
                </div>
                <div>
                  <div style={{ fontWeight: 700, fontSize: '0.95rem', color: 'var(--text-main)' }}>{q.prompt}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                    Asked {new Date(q.createdAt).toLocaleString()}
                  </div>
                </div>
              </div>

              {/* Answer bubble */}
              <div className="flex-gap-2" style={{ alignItems: 'flex-start' }}>
                <div style={{ background: 'rgba(16, 185, 129, 0.15)', color: 'var(--success)', padding: '0.5rem', borderRadius: '50%', display: 'flex', marginTop: '0.2rem' }}>
                  <Bot size={16} />
                </div>
                <div style={{ flex: 1 }}>
                  <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                    <span style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--success)' }}>JobIntel AI Assistant</span>
                    <span className="badge badge-success" style={{ fontSize: '0.65rem' }}>Grounded Answer</span>
                  </div>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', lineHeight: '1.6', whiteSpace: 'pre-wrap' }}>
                    {q.response}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
