import { useEffect, useMemo, useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

function App() {
  const [count, setCount] = useState(0)
  const [apiStatus, setApiStatus] = useState(null)
  const [apiError, setApiError] = useState(null)
  const [session, setSession] = useState(null)
  const [sessionError, setSessionError] = useState(null)
  const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  const authorizeBase = import.meta.env.VITE_OAUTH_AUTHORIZE_URL || 'http://localhost:9999/oauth/authorize'
  const clientId = import.meta.env.VITE_MIARG_CLIENT_ID || 'stub-client'
  const redirectUri = import.meta.env.VITE_MIARG_REDIRECT_URI || 'http://localhost:8080/auth/miargentina/callback'

  const authorizeUrl = useMemo(() => {
    const query = new URLSearchParams({
      response_type: 'code',
      client_id: clientId,
      scope: 'openid profile',
      redirect_uri: redirectUri,
      state: 'demo-state'
    });
    return `${authorizeBase}?${query.toString()}`;
  }, [authorizeBase, clientId, redirectUri])

  useEffect(() => {
    let cancelled = false;

    const fetchHealth = () => {
      fetch(`${apiUrl}/actuator/health`, { credentials: 'include' })
        .then((res) => (res.ok ? res.json() : Promise.reject(res.statusText)))
        .then((body) => {
          if (!cancelled) {
            setApiStatus(body.status || 'UNKNOWN');
            setApiError(null);
          }
        })
        .catch((err) => {
          if (!cancelled) {
            setApiStatus('OFFLINE');
            setApiError(String(err));
          }
        });
    };

    fetchHealth();
    const interval = setInterval(fetchHealth, 5000);

    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, [apiUrl])

  useEffect(() => {
    fetch(`${apiUrl}/auth/session`, { credentials: 'include' })
      .then((res) => {
        if (res.status === 401) {
          setSession(null);
          setSessionError(null);
          return null;
        }
        if (!res.ok) {
          return Promise.reject(res.statusText);
        }
        return res.json();
      })
      .then((body) => {
        if (body) {
          setSession(body);
          setSessionError(null);
        }
      })
      .catch((err) => {
        setSession(null);
        setSessionError(String(err));
      });
  }, [apiUrl])

  const handleLogout = () => {
    fetch(`${apiUrl}/auth/logout`, { method: 'POST', credentials: 'include' })
      .then(() => setSession(null))
      .catch((err) => setSessionError(String(err)));
  };

  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Vite + React</h1>
      <div className="card">
        <button onClick={() => setCount((value) => value + 1)}>
          Contador de prueba: {count}
        </button>
        <p>Backend health: <strong>{apiStatus ?? 'consultando...'}</strong></p>
        {apiError && (<small style={{ color: '#ff8a80' }}>Error: {apiError}</small>)}
        <hr />
        {session ? (
          <div className="session-info">
            <p>Sesión activa: <strong>{session.displayName}</strong></p>
            {session.email && <p>Correo: {session.email}</p>}
            <button onClick={handleLogout}>Cerrar sesión</button>
          </div>
        ) : (
          <div className="session-info">
            <p>No has iniciado sesión.</p>
            <p>
              <a href={authorizeUrl}>Iniciar flujo MiArgentina (mock)</a>
            </p>
          </div>
        )}
        {sessionError && (<small style={{ color: '#ff8a80' }}>Sesión: {sessionError}</small>)}
      </div>
    </>
  )
}

export default App
