import { useState, useRef, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import LoadingSpinner from '../../components/LoadingSpinner'
import {
  RiSendPlane2Line, RiRobot2Line, RiUserLine,
  RiCalendarCheckLine, RiDeleteBinLine,
} from 'react-icons/ri'

const WELCOME = "Hi! I'm your AttendAI assistant. Ask me about your leave balance, attendance records, or anything HR-related. 👋"

export default function AiAssistantPage() {
  const { user } = useAuth()
  const empId = user?.employeeId
  const [messages, setMessages] = useState([{ role: 'bot', text: WELCOME }])
  const [input,    setInput]    = useState('')
  const [loading,  setLoading]  = useState(false)
  const [tab,      setTab]      = useState('chat')
  const [nlInput,  setNlInput]  = useState('')
  const [nlResult, setNlResult] = useState(null)
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  const sendChat = async () => {
    const text = input.trim()
    if (!text) return
    if (!empId) {
      setMessages(m => [...m, { role: 'bot', text: '⚠ No employee record. Ask HR to link your account.' }])
      return
    }
    setInput('')
    setMessages(m => [...m, { role: 'user', text }])
    setLoading(true)
    try {
      const r = await api.post('/ai/chat', { employeeId: empId, message: text })
      setMessages(m => [...m, { role: 'bot', text: r.data.answer ?? r.data.message ?? 'No response.' }])
    } catch (err) {
      setMessages(m => [...m, { role: 'bot', text: err.response?.data?.message || 'Sorry, an error occurred.' }])
    } finally { setLoading(false) }
  }

  const sendNl = async () => {
    if (!nlInput.trim() || !empId) return
    setLoading(true)
    setNlResult(null)
    try {
      const r = await api.post('/ai/attendance/nl', { employeeId: empId, message: nlInput })
      setNlResult(r.data)
    } catch (err) {
      setNlResult({ message: err.response?.data?.message || 'Processing failed' })
    } finally { setLoading(false) }
  }

  return (
    <div className="space-y-4 animate-fade-up">
      <PageHeader title="AI Assistant" subtitle="Powered by AttendAI" />

      {/* Tab switcher */}
      <div className="flex gap-1 bg-bg2 border border-border-subtle p-1 rounded-xl w-fit">
        {[{ k: 'chat', l: 'HR Chatbot' }, { k: 'nl', l: 'NL Attendance' }].map(t => (
          <button
            key={t.k}
            onClick={() => setTab(t.k)}
            className={[
              'px-4 py-2 rounded-lg text-sm font-bold transition-all duration-100',
              tab === t.k
                ? 'bg-primary text-text-inverse shadow-glow-sm'
                : 'text-text-muted hover:text-text hover:bg-bg3',
            ].join(' ')}
          >
            {t.l}
          </button>
        ))}
      </div>

      {/* ── Chat tab ── */}
      {tab === 'chat' && (
        <div className="card flex flex-col" style={{ height: '540px' }}>
          {/* Header */}
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-border-subtle">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-primary flex items-center justify-center shadow-glow-sm">
                <RiRobot2Line size={17} className="text-text-inverse" />
              </div>
              <div>
                <p className="text-sm font-bold text-text">AttendAI Bot</p>
                <p className="text-sm text-accent flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-accent inline-block animate-pulse-soft" />
                  Online
                </p>
              </div>
            </div>
            <button
              onClick={() => setMessages([{ role: 'bot', text: WELCOME }])}
              className="btn-icon"
              title="Clear chat"
            >
              <RiDeleteBinLine size={15} />
            </button>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto space-y-4 pr-0.5 no-scrollbar">
            {messages.map((m, i) => (
              <div key={i} className={`flex gap-2.5 ${m.role === 'user' ? 'flex-row-reverse' : ''}`}>
                <div className={[
                  'w-8 h-8 rounded-xl flex items-center justify-center shrink-0',
                  m.role === 'user' ? 'bg-primary' : 'bg-bg3',
                ].join(' ')}>
                  {m.role === 'user'
                    ? <RiUserLine  size={14} className="text-text-inverse" />
                    : <RiRobot2Line size={14} className="text-text-muted" />
                  }
                </div>
                <div className={[
                  'max-w-xs sm:max-w-sm px-4 py-3 rounded-2xl text-sm whitespace-pre-wrap leading-relaxed',
                  m.role === 'user'
                    ? 'bg-primary text-text-inverse rounded-tr-sm'
                    : 'bg-bg3 text-text rounded-tl-sm border border-border-subtle',
                ].join(' ')}>
                  {m.text}
                </div>
              </div>
            ))}

            {/* Typing indicator */}
            {loading && (
              <div className="flex gap-2.5">
                <div className="w-8 h-8 rounded-xl bg-bg3 flex items-center justify-center">
                  <RiRobot2Line size={14} className="text-text-muted" />
                </div>
                <div className="bg-bg3 border border-border-subtle px-4 py-3 rounded-2xl rounded-tl-sm
                                flex items-center gap-1 h-11">
                  {[0, 1, 2].map(i => (
                    <div
                      key={i}
                      className="w-2 h-2 rounded-full bg-text-faint animate-bounce"
                      style={{ animationDelay: `${i * 0.15}s` }}
                    />
                  ))}
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>

          {/* Input row */}
          <div className="flex gap-2 mt-4 pt-3 border-t border-border-subtle">
            <input
              className="input flex-1"
              placeholder="Ask about leave, attendance, policies…"
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && !e.shiftKey && (e.preventDefault(), sendChat())}
              disabled={loading}
            />
            <button
              onClick={sendChat}
              disabled={loading || !input.trim()}
              className="btn-primary px-3.5"
            >
              <RiSendPlane2Line size={17} />
            </button>
          </div>
        </div>
      )}

      {/* ── NL Attendance tab ── */}
      {tab === 'nl' && (
        <div className="card max-w-lg space-y-4">
          <div className="flex items-start gap-3 p-4 rounded-xl bg-primary-10 border border-primary-20">
            <RiCalendarCheckLine size={19} className="text-primary shrink-0 mt-0.5" />
            <p className="text-sm font-normal text-text">
              Describe your attendance in natural language — I'll interpret and record it.
            </p>
          </div>

          <div className="form-group">
            <label className="label">Your message</label>
            <textarea
              className="input h-28 resize-none"
              placeholder='"I will be 30 minutes late today due to a doctor appointment"'
              value={nlInput}
              onChange={e => setNlInput(e.target.value)}
            />
          </div>

          <button
            onClick={sendNl}
            disabled={loading || !empId || !nlInput.trim()}
            className="btn-primary"
          >
            {loading
              ? <><LoadingSpinner /><span>Processing…</span></>
              : 'Process Message'
            }
          </button>

          {!empId && (
            <p className="text-sm text-primary bg-primary-10 p-3 rounded-xl border border-primary-20">
              ⚠ No employee record linked to your account.
            </p>
          )}

          {nlResult && (
            <div className="bg-bg2 rounded-xl p-4 border border-border-subtle space-y-2">
              <p className="text-sm font-bold text-text-faint uppercase tracking-wider mb-3">Result</p>
              {Object.entries(nlResult)
                .filter(([, v]) => v != null && v !== '')
                .map(([k, v]) => (
                  <div key={k} className="flex justify-between text-sm">
                    <span className="text-text-muted font-normal capitalize">
                      {k.replace(/([A-Z])/g, ' $1').trim()}
                    </span>
                    <span className="font-bold text-text">{String(v)}</span>
                  </div>
                ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
