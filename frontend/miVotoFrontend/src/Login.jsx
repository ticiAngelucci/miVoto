import { useState } from 'react'
import MiVotoLogo from './components/MiVotoLogo'
import { loginUser } from './api'
import './App.css'

const FEEDBACK_MESSAGES = {
  empty: 'Ingresá tu nombre para continuar.',
  error: 'Ocurrió un problema al validar tu nombre. Intentá otra vez en unos minutos.',
  notFound:
    'No encontramos un votante con ese nombre. Verificá los datos e intentá nuevamente.',
}

function Login({ onLoginSuccess }) {
  const [name, setName] = useState('')
  const [loading, setLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [showTutorial, setShowTutorial] = useState(false)

  const resetFeedback = () => setFeedback({ type: '', message: '' })

  const handleSubmit = async (event) => {
    event.preventDefault()
    resetFeedback()

    if (!name.trim()) {
      setFeedback({ type: 'error', message: FEEDBACK_MESSAGES.empty })
      return
    }

    setLoading(true)

    try {
      const response = await loginUser(name.trim())
      onLoginSuccess(response)
    } catch (error) {
      console.error(error)
      const message =
        error instanceof Error && error.message
          ? error.message
          : FEEDBACK_MESSAGES.error
      setFeedback({ type: 'error', message })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app-background app-background--center">
      <div className="background-gradients">
        <div className="gradient gradient--one" />
        <div className="gradient gradient--two" />
        <div className="gradient gradient--three" />
      </div>
      <div className="login-bottom-arc" />

      <main className="login-card">
        <MiVotoLogo className="login-card__logo" />

        <form className="login-form" onSubmit={handleSubmit}>
          <label className="login-form__label" htmlFor="voterName">
            Ingresá tu nombre completo
          </label>
          <input
            id="voterName"
            name="voterName"
            type="text"
            placeholder="Ej: Juan Pérez"
            autoComplete="name"
            value={name}
            onChange={(event) => {
              setName(event.target.value)
              if (feedback.type) {
                resetFeedback()
              }
            }}
            disabled={loading}
            className="login-form__input"
          />

          <button type="submit" className="login-form__button" disabled={loading}>
            {loading ? 'Verificando…' : 'Ingresar para votar'}
          </button>
        </form>

        {feedback.message && (
          <p className={`login-feedback login-feedback--${feedback.type}`}>
            {feedback.message}
          </p>
        )}

        <button type="button" className="login-tutorial" onClick={() => setShowTutorial(true)}>
          ¿No sabés cómo votar? Ver tutorial
        </button>
      </main>

      {showTutorial && (
        <div className="tutorial-modal" role="dialog" aria-modal="true">
          <div
            className="tutorial-modal__backdrop"
            onClick={() => setShowTutorial(false)}
            aria-hidden="true"
          />
          <div className="tutorial-modal__content">
            <button
              type="button"
              className="tutorial-modal__close"
              onClick={() => setShowTutorial(false)}
            >
              Cerrar
            </button>
            <video
              className="tutorial-modal__video"
              src="/videos/Tutorial.mp4"
              controls
              autoPlay
              playsInline
            />
          </div>
        </div>
      )}
    </div>
  )
}

export default Login
