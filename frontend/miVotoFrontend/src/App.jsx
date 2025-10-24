import { useCallback, useEffect, useMemo, useState } from 'react'
import './App.css'

const formatDateTime = (value) => {
  if (!value) {
    return 'Sin definir'
  }
  try {
    return new Date(value).toLocaleString()
  } catch (err) {
    return String(value)
  }
}

function App() {
  const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  const authorizeBase = import.meta.env.VITE_OAUTH_AUTHORIZE_URL || 'http://localhost:9999/oauth/authorize'
  const clientId = import.meta.env.VITE_MIARG_CLIENT_ID || 'stub-client'
  const redirectUri = import.meta.env.VITE_MIARG_REDIRECT_URI || 'http://localhost:8080/auth/miargentina/callback'

  const [apiStatus, setApiStatus] = useState(null)
  const [apiError, setApiError] = useState(null)
  const [session, setSession] = useState(null)
  const [sessionError, setSessionError] = useState(null)

  const [institutions, setInstitutions] = useState([])
  const [institutionsError, setInstitutionsError] = useState(null)
  const [candidates, setCandidates] = useState([])
  const [candidatesError, setCandidatesError] = useState(null)
  const [ballots, setBallots] = useState([])
  const [ballotsError, setBallotsError] = useState(null)

  const [selectedBallotId, setSelectedBallotId] = useState('')
  const [selectedCandidates, setSelectedCandidates] = useState([])
  const [eligibilityToken, setEligibilityToken] = useState('')
  const [eligibilityExpiresAt, setEligibilityExpiresAt] = useState(null)
  const [eligibilityRequestError, setEligibilityRequestError] = useState(null)
  const [eligibilityRequestLoading, setEligibilityRequestLoading] = useState(false)

  const [voteResult, setVoteResult] = useState(null)
  const [voteError, setVoteError] = useState(null)
  const [voteSubmitting, setVoteSubmitting] = useState(false)

  const [tally, setTally] = useState(null)
  const [tallyError, setTallyError] = useState(null)
  const [tallyLoading, setTallyLoading] = useState(false)

  const [finalResult, setFinalResult] = useState(null)
  const [finalError, setFinalError] = useState(null)
  const [finalizing, setFinalizing] = useState(false)

  const [verifyReceipt, setVerifyReceipt] = useState('')
  const [verifyResult, setVerifyResult] = useState(null)
  const [verifyError, setVerifyError] = useState(null)
  const [verifyLoading, setVerifyLoading] = useState(false)

  const authorizeUrl = useMemo(() => {
    const query = new URLSearchParams({
      response_type: 'code',
      client_id: clientId,
      scope: 'openid profile',
      redirect_uri: redirectUri,
      state: 'demo-state'
    })
    return `${authorizeBase}?${query.toString()}`
  }, [authorizeBase, clientId, redirectUri])

  const fetchJson = useCallback(async (path, options = {}) => {
    const { allowNotFound = false, headers, ...rest } = options
    const response = await fetch(`${apiUrl}${path}`, {
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        ...(rest.body ? { 'Content-Type': 'application/json' } : {}),
        ...headers
      },
      ...rest
    })

    const contentType = response.headers.get('content-type') || ''
    const text = await response.text()

    if (!response.ok) {
      if (allowNotFound && response.status === 404) {
        return null
      }
      let message = `${response.status} ${response.statusText}`
      if (text) {
        if (contentType.includes('application/json')) {
          try {
            const body = JSON.parse(text)
            message = body.detail || body.message || JSON.stringify(body)
          } catch {
            message = text
          }
        } else {
          message = text
        }
      }
      throw new Error(message)
    }

    if (!text) {
      return null
    }
    if (contentType.includes('application/json')) {
      try {
        return JSON.parse(text)
      } catch {
        return text
      }
    }
    return text
  }, [apiUrl])

  useEffect(() => {
    let cancelled = false

    const fetchHealth = async () => {
      try {
        const data = await fetchJson('/actuator/health')
        if (!cancelled) {
          setApiStatus(data?.status || 'UNKNOWN')
          setApiError(null)
        }
      } catch (err) {
        if (!cancelled) {
          setApiStatus('OFFLINE')
          setApiError(err.message)
        }
      }
    }

    fetchHealth()
    const interval = setInterval(fetchHealth, 5000)

    return () => {
      cancelled = true
      clearInterval(interval)
    }
  }, [fetchJson])

  useEffect(() => {
    let cancelled = false
    const loadSession = async () => {
      try {
        const data = await fetchJson('/auth/session')
        if (!cancelled) {
          setSession(data)
          setSessionError(null)
        }
      } catch (err) {
        if (!cancelled) {
          if (err.message.startsWith('401')) {
            setSession(null)
            setSessionError(null)
          } else {
            setSession(null)
            setSessionError(err.message)
          }
        }
      }
    }
    loadSession()
    return () => { cancelled = true }
  }, [fetchJson])

  useEffect(() => {
    if (!session) {
      setEligibilityToken('')
      setEligibilityExpiresAt(null)
      setEligibilityRequestError(null)
    }
  }, [session])

  useEffect(() => {
    let cancelled = false
    const loadInstitutions = async () => {
      try {
        const data = await fetchJson('/institutions')
        if (!cancelled) {
          setInstitutions(Array.isArray(data) ? data : [])
          setInstitutionsError(null)
        }
      } catch (err) {
        if (!cancelled) {
          setInstitutions([])
          setInstitutionsError(err.message)
        }
      }
    }
    loadInstitutions()
    return () => { cancelled = true }
  }, [fetchJson])

  useEffect(() => {
    let cancelled = false
    const loadCandidates = async () => {
      try {
        const data = await fetchJson('/candidates')
        if (!cancelled) {
          setCandidates(Array.isArray(data) ? data : [])
          setCandidatesError(null)
        }
      } catch (err) {
        if (!cancelled) {
          setCandidates([])
          setCandidatesError(err.message)
        }
      }
    }
    loadCandidates()
    return () => { cancelled = true }
  }, [fetchJson])

  useEffect(() => {
    let cancelled = false
    const loadBallots = async () => {
      try {
        const data = await fetchJson('/ballots')
        if (!cancelled) {
          setBallots(Array.isArray(data) ? data : [])
          setBallotsError(null)
        }
      } catch (err) {
        if (!cancelled) {
          setBallots([])
          setBallotsError(err.message)
        }
      }
    }
    loadBallots()
    return () => { cancelled = true }
  }, [fetchJson])

  useEffect(() => {
    if (ballots.length === 0) {
      setSelectedBallotId('')
      return
    }
    if (!ballots.some((ballot) => ballot.id === selectedBallotId)) {
      setSelectedBallotId(ballots[0].id)
    }
  }, [ballots, selectedBallotId])

  const candidateMap = useMemo(() => {
    const map = new Map()
    candidates.forEach((candidate) => {
      if (candidate?.id) {
        map.set(candidate.id, candidate)
      }
    })
    return map
  }, [candidates])

  const selectedBallot = useMemo(
    () => ballots.find((ballot) => ballot.id === selectedBallotId) || null,
    [ballots, selectedBallotId]
  )

  const selectedInstitution = useMemo(() => {
    if (!selectedBallot) {
      return null
    }
    return institutions.find((institution) => institution.id === selectedBallot.institutionId) || null
  }, [institutions, selectedBallot])

  const selectedCandidateDetails = useMemo(() => {
    if (!selectedBallot) {
      return []
    }
    return selectedBallot.candidateIds
      .map((id) => candidateMap.get(id))
      .filter(Boolean)
  }, [selectedBallot, candidateMap])

  useEffect(() => {
    if (!selectedBallot) {
      setSelectedCandidates([])
      return
    }
    setSelectedCandidates((current) =>
      current.filter((candidateId) => selectedBallot.candidateIds.includes(candidateId))
    )
  }, [selectedBallot])

  const refreshTally = useCallback(async (ballotId) => {
    if (!ballotId) {
      setTally(null)
      return
    }
    setTallyLoading(true)
    setTallyError(null)
    try {
      const data = await fetchJson(`/ballots/${ballotId}/tally`)
      setTally(data)
    } catch (err) {
      setTally(null)
      setTallyError(err.message)
    } finally {
      setTallyLoading(false)
    }
  }, [fetchJson])

  useEffect(() => {
    if (!selectedBallotId) {
      setTally(null)
      return
    }
    let cancelled = false
    const load = async () => {
      setTallyLoading(true)
      setTallyError(null)
      try {
        const data = await fetchJson(`/ballots/${selectedBallotId}/tally`)
        if (!cancelled) {
          setTally(data)
        }
      } catch (err) {
        if (!cancelled) {
          setTally(null)
          setTallyError(err.message)
        }
      } finally {
        if (!cancelled) {
          setTallyLoading(false)
        }
      }
    }
    load()
    return () => { cancelled = true }
  }, [selectedBallotId, fetchJson])

  useEffect(() => {
    if (!selectedBallotId) {
      setFinalResult(null)
      setFinalError(null)
      return
    }
    let cancelled = false
    const load = async () => {
      try {
        const data = await fetchJson(`/ballots/${selectedBallotId}/result`, { allowNotFound: true })
        if (!cancelled) {
          setFinalResult(data)
          setFinalError(null)
        }
      } catch (err) {
        if (!cancelled) {
          setFinalResult(null)
          setFinalError(err.message)
        }
      }
    }
    load()
    return () => { cancelled = true }
  }, [selectedBallotId, fetchJson])

  const handleLogout = () => {
    fetchJson('/auth/logout', { method: 'POST' })
      .then(() => {
        setSession(null)
        setEligibilityToken('')
        setEligibilityExpiresAt(null)
        setEligibilityRequestError(null)
      })
      .catch((err) => setSessionError(err.message))
  }

  const handleRequestEligibility = async () => {
    if (!session) {
      setEligibilityRequestError('Necesitás iniciar sesión para solicitar el token.')
      return
    }
    setEligibilityRequestLoading(true)
    setEligibilityRequestError(null)
    try {
      const data = await fetchJson('/eligibility/issue/session', { method: 'POST' })
      if (data) {
        setEligibilityToken(data.eligibilityToken ?? '')
        setEligibilityExpiresAt(data.expiresAt ?? null)
      }
    } catch (err) {
      setEligibilityRequestError(err.message)
    } finally {
      setEligibilityRequestLoading(false)
    }
  }

  const handleCandidateToggle = (candidateId) => {
    if (!selectedBallot) {
      return
    }
    if (selectedBallot.allowMultipleSelection) {
      setSelectedCandidates((current) =>
        current.includes(candidateId)
          ? current.filter((value) => value !== candidateId)
          : [...current, candidateId]
      )
    } else {
      setSelectedCandidates([candidateId])
    }
  }

  const handleVoteSubmit = async (event) => {
    event.preventDefault()
    setVoteError(null)
    setVoteResult(null)

    if (!session) {
      setVoteError('Necesitás iniciar sesión antes de votar.')
      return
    }
    if (!selectedBallot) {
      setVoteError('Seleccioná una boleta.')
      return
    }
    if (selectedCandidates.length === 0) {
      setVoteError('Elegí al menos un candidato.')
      return
    }
    if (!eligibilityToken.trim()) {
      setVoteError('Solicitá un token de elegibilidad o ingresá uno válido antes de votar.')
      return
    }

    const payload = {
      ballotId: selectedBallot.id,
      eligibilityToken: eligibilityToken.trim(),
      selection: {
        institutionId: selectedBallot.institutionId,
        candidateIds: selectedCandidates
      }
    }

    try {
      setVoteSubmitting(true)
      const data = await fetchJson('/votes/cast', {
        method: 'POST',
        body: JSON.stringify(payload)
      })
      setVoteResult(data)
      setEligibilityToken('')
      setEligibilityExpiresAt(null)
      if (!selectedBallot.allowMultipleSelection) {
        setSelectedCandidates([])
      }
      refreshTally(selectedBallot.id)
      setVoteError(null)
    } catch (err) {
      setVoteResult(null)
      setVoteError(err.message)
    } finally {
      setVoteSubmitting(false)
    }
  }

  const handleFinalizeBallot = async () => {
    if (!selectedBallot) {
      setFinalError('Seleccioná una boleta para cerrar el conteo.')
      return
    }
    setFinalizing(true)
    setFinalError(null)
    try {
      const data = await fetchJson(`/ballots/${selectedBallot.id}/finalize`, { method: 'POST' })
      setFinalResult(data)
      refreshTally(selectedBallot.id)
    } catch (err) {
      setFinalError(err.message)
    } finally {
      setFinalizing(false)
    }
  }

  const handleVerifyReceipt = async (event) => {
    event.preventDefault()
    setVerifyError(null)
    setVerifyResult(null)
    const receipt = verifyReceipt.trim()
    if (!receipt) {
      setVerifyError('Ingresá un recibo para verificarlo.')
      return
    }
    try {
      setVerifyLoading(true)
      const data = await fetchJson(`/votes/${encodeURIComponent(receipt)}/verify`)
      setVerifyResult(data)
    } catch (err) {
      setVerifyResult(null)
      setVerifyError(err.message)
    } finally {
      setVerifyLoading(false)
    }
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>miVoto – entorno mock</h1>
        <p className="subtitle">Flujo completo: autenticación, emisión, recuento y cierre.</p>
      </header>

      <section className="panel">
        <h2>Estado del backend</h2>
        <p>
          Salud: <strong>{apiStatus ?? 'consultando...'}</strong>
        </p>
        {apiError && <p className="error">{apiError}</p>}
      </section>

      <section className="panel">
        <h2>Autenticación</h2>
        {session ? (
          <div className="session-block">
            <p>Sesión activa como <strong>{session.displayName}</strong></p>
            {session.email && <p>Correo: {session.email}</p>}
            <button type="button" onClick={handleLogout} className="secondary">
              Cerrar sesión
            </button>
          </div>
        ) : (
          <div>
            <p>No hay sesión iniciada.</p>
            <a className="button" href={authorizeUrl}>
              Iniciar flujo MiArgentina (mock)
            </a>
          </div>
        )}
        {sessionError && <p className="error">{sessionError}</p>}
      </section>

      <section className="panel">
        <h2>Catálogo de instituciones y candidatos</h2>
        <div className="catalog-grid">
          <div>
            <h3>Instituciones</h3>
            {institutionsError && <p className="error">{institutionsError}</p>}
            <ul>
              {institutions.map((institution) => (
                <li key={institution.id}>
                  <strong>{institution.name}</strong>{' '}
                  {!institution.active && <span className="tag">inactiva</span>}
                  <div className="muted">{institution.description || 'Sin descripción'}.</div>
                </li>
              ))}
              {institutions.length === 0 && !institutionsError && (
                <li className="muted">No hay instituciones cargadas.</li>
              )}
            </ul>
          </div>
          <div>
            <h3>Candidatos</h3>
            {candidatesError && <p className="error">{candidatesError}</p>}
            <ul className="candidate-list">
              {candidates.map((candidate) => (
                <li key={candidate.id}>
                  <strong>{candidate.displayName}</strong>{' '}
                  {candidate.listName && <span className="muted">({candidate.listName})</span>}
                  <div className="muted">Institución: {candidate.institutionId}</div>
                  {!candidate.active && <div className="tag">inactivo</div>}
                </li>
              ))}
              {candidates.length === 0 && !candidatesError && (
                <li className="muted">No hay candidatos cargados.</li>
              )}
            </ul>
          </div>
        </div>
      </section>

      <section className="panel">
        <h2>Emitir voto</h2>
        {ballotsError && <p className="error">{ballotsError}</p>}
        {ballots.length === 0 && !ballotsError && (
          <p className="muted">No hay boletas configuradas en este entorno.</p>
        )}
        {ballots.length > 0 && (
          <form onSubmit={handleVoteSubmit} className="vote-form">
            <label className="field">
              <span>Boleta disponible</span>
              <select
                value={selectedBallotId}
                onChange={(event) => setSelectedBallotId(event.target.value)}
              >
                {ballots.map((ballot) => (
                  <option key={ballot.id} value={ballot.id}>
                    {ballot.title} {ballot.open ? '(abierta)' : '(cerrada)'}
                  </option>
                ))}
              </select>
            </label>

            {selectedBallot && (
              <div className="ballot-details">
                <p>
                  <strong>{selectedBallot.title}</strong>
                  {selectedInstitution && (
                    <> — {selectedInstitution.name}</>
                  )}
                </p>
                <p className="muted">
                  Abre: {formatDateTime(selectedBallot.opensAt)} | Cierra: {formatDateTime(selectedBallot.closesAt)}
                </p>
                {selectedBallot.allowMultipleSelection ? (
                  <p className="muted">Podés seleccionar más de un candidato.</p>
                ) : (
                  <p className="muted">Seleccioná un único candidato.</p>
                )}
              </div>
            )}

            {selectedCandidateDetails.length > 0 && (
              <fieldset className="candidate-fieldset">
                <legend>Candidatos</legend>
                {selectedCandidateDetails.map((candidate) => {
                  const isSelected = selectedCandidates.includes(candidate.id)
                  const inputType = selectedBallot?.allowMultipleSelection ? 'checkbox' : 'radio'
                  return (
                    <label key={candidate.id} className={`candidate-option ${isSelected ? 'selected' : ''}`}>
                      <input
                        type={inputType}
                        name="vote-candidate"
                        value={candidate.id}
                        checked={isSelected}
                        onChange={() => handleCandidateToggle(candidate.id)}
                      />
                      <span>
                        <strong>{candidate.displayName}</strong>
                        {candidate.listName && <span className="muted"> — {candidate.listName}</span>}
                      </span>
                    </label>
                  )
                })}
              </fieldset>
            )}

            <div className="eligibility-tools">
              <button
                type="button"
                className="secondary"
                onClick={handleRequestEligibility}
                disabled={eligibilityRequestLoading}
              >
                {eligibilityRequestLoading ? 'Solicitando token…' : 'Obtener token desde la sesión'}
              </button>
              {eligibilityExpiresAt && (
                <span className="muted">Válido hasta: {formatDateTime(eligibilityExpiresAt)}</span>
              )}
            </div>
            {eligibilityRequestError && <p className="error">{eligibilityRequestError}</p>}

            <label className="field">
              <span>Token de elegibilidad</span>
              <input
                type="text"
                value={eligibilityToken}
                onChange={(event) => setEligibilityToken(event.target.value)}
                placeholder="Solicitá el token o pegalo manualmente"
              />
            </label>

            <button type="submit" className="button" disabled={voteSubmitting}>
              {voteSubmitting ? 'Enviando voto…' : 'Emitir voto'}
            </button>
            {voteError && <p className="error">{voteError}</p>}
            {voteResult && (
              <div className="success-box">
                <p>Voto recibido. Guardá estos datos:</p>
                <p>Recibo: <code>{voteResult.receipt}</code></p>
                <p>Tx hash: <code>{voteResult.txHash}</code></p>
              </div>
            )}
          </form>
        )}
      </section>

      <section className="panel">
        <h2>Recuento en tiempo real</h2>
        {selectedBallot ? (
          <>
            <button type="button" className="secondary" onClick={() => refreshTally(selectedBallot.id)} disabled={tallyLoading}>
              {tallyLoading ? 'Actualizando…' : 'Actualizar recuento'}
            </button>
            {tallyError && <p className="error">{tallyError}</p>}
            {tally?.results?.length ? (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Candidato</th>
                    <th>Votos</th>
                  </tr>
                </thead>
                <tbody>
                  {tally.results.map((result) => (
                    <tr key={result.candidateId}>
                      <td>
                        <strong>{result.displayName || result.candidateId}</strong>
                        {result.listName && <span className="muted"> — {result.listName}</span>}
                      </td>
                      <td>{result.votes}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p className="muted">Sin votos registrados todavía.</p>
            )}
            {tally?.computedAt && (
              <p className="muted">Última consulta: {formatDateTime(tally.computedAt)}</p>
            )}
          </>
        ) : (
          <p className="muted">Seleccioná una boleta para ver su recuento.</p>
        )}
      </section>

      <section className="panel">
        <h2>Cierre y publicación de resultados</h2>
        <div className="finalize-controls">
          <button
            type="button"
            className="button"
            onClick={handleFinalizeBallot}
            disabled={finalizing || !selectedBallot}
          >
            {finalizing ? 'Cerrando…' : 'Cerrar boleta y generar resultado final'}
          </button>
          {finalError && <p className="error">{finalError}</p>}
        </div>
        {finalResult ? (
          <div className="result-box">
            <p>
              Resultado final ({formatDateTime(finalResult.computedAt)}) – checksum{' '}
              <code>{finalResult.checksum}</code>
            </p>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Candidato</th>
                  <th>Votos</th>
                </tr>
              </thead>
              <tbody>
                {finalResult.results.map((result) => (
                  <tr key={result.candidateId}>
                    <td>
                      <strong>{result.displayName || result.candidateId}</strong>
                      {result.listName && <span className="muted"> — {result.listName}</span>}
                    </td>
                    <td>{result.votes}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="muted">Todavía no hay un cierre confirmado para esta boleta.</p>
        )}
      </section>

      <section className="panel">
        <h2>Verificar recibo</h2>
        <form onSubmit={handleVerifyReceipt} className="verify-form">
          <label className="field">
            <span>Recibo emitido</span>
            <input
              type="text"
              value={verifyReceipt}
              onChange={(event) => setVerifyReceipt(event.target.value)}
              placeholder="Pega aquí el recibo a verificar"
            />
          </label>
          <button type="submit" className="secondary" disabled={verifyLoading}>
            {verifyLoading ? 'Consultando…' : 'Verificar'}
          </button>
        </form>
        {verifyError && <p className="error">{verifyError}</p>}
        {verifyResult && (
          <div className="result-box">
            <p>Recibo: <code>{verifyResult.receipt}</code></p>
            <p>Boleta: {verifyResult.ballotId}</p>
            <p>Registrado on-chain: {verifyResult.onChain ? 'sí' : 'no'}</p>
            <p>Registrado off-chain: {verifyResult.offChain ? 'sí' : 'no'}</p>
            {verifyResult.txHash && (
              <p>Tx hash: <code>{verifyResult.txHash}</code></p>
            )}
          </div>
        )}
      </section>
    </div>
  )
}

export default App
