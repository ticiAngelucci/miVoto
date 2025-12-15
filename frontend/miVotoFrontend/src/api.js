import axios from 'axios'

const institutionFallbackImages = [
  'https://images.unsplash.com/photo-1482784160316-6eb046863ece?auto=format&fit=crop&w=400&q=80',
  'https://images.unsplash.com/photo-1489515217757-5fd1be406fef?auto=format&fit=crop&w=400&q=80',
  'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=400&q=80',
]

const candidateFallbackImages = [
  'https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80',
  'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=400&q=80',
  'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=400&q=80',
  'https://images.unsplash.com/photo-1521119989659-a83eee488004?auto=format&fit=crop&w=400&q=80',
  'https://images.unsplash.com/photo-1524502397800-2eeaad7c3fe5?auto=format&fit=crop&w=400&q=80',
]

const hashKey = (value) => {
  const str = String(value ?? '')
  let hash = 0
  for (let i = 0; i < str.length; i += 1) {
    hash = (hash << 5) - hash + str.charCodeAt(i)
    hash |= 0
  }
  return Math.abs(hash)
}

const DEFAULT_API_BASE_URL = 'https://mivoto-backend.onrender.com'
const LOGIN_ENDPOINT = '/api/auth/login'
const VOTE_ENDPOINT = '/api/vote'
const INSTITUTIONS_ENDPOINT = '/api/institutions'
const LOGIN_TIMEOUT_MS = 20000
const VOTE_TIMEOUT_MS = 15000
const INSTITUTIONS_TIMEOUT_MS = 10000
const ELECTIONS_TIMEOUT_MS = 12000
const LOGIN_MAX_RETRIES = 1

const resolveApiBaseUrl = () => {
  try {
    const rawBaseUrl = import.meta.env?.VITE_API_BASE_URL ?? ''
    if (rawBaseUrl.trim()) {
      return rawBaseUrl.trim().replace(/\/+$/, '')
    }
  } catch {
    // Running outside Vite (tests, storybook, etc.)
  }
  return DEFAULT_API_BASE_URL
}

const API_BASE_URL = resolveApiBaseUrl()

const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

if (apiClient?.defaults?.headers?.common?.['X-Requested-With']) {
  delete apiClient.defaults.headers.common['X-Requested-With']
}

const isAbortLikeError = (error) =>
  error?.name === 'AbortError' || error?.name === 'CanceledError' || error?.code === 'ERR_CANCELED'

const isTimeoutError = (error) => error?.code === 'ECONNABORTED'

const buildInstitutionElectionsPath = (institutionId) =>
  `${INSTITUTIONS_ENDPOINT}/${encodeURIComponent(institutionId)}/elections`

const ensureNonEmptyString = (value, fallback) =>
  typeof value === 'string' && value.trim() ? value.trim() : fallback

const ensureNonNegativeNumber = (value, fallback = 0) => {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback
}

const assignFallbackImage = (id, providedUrl, pool) => {
  if (providedUrl) {
    return providedUrl
  }
  const safePool = pool.length
    ? pool
    : ['https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=400&q=80']
  return safePool[hashKey(id) % safePool.length]
}

const mockInstitutions = [
  {
    id: 'inst-tec',
    name: 'Instituto Tecnologico Federal',
    description: 'Elegimos representantes para definir el plan estrategico digital 2026.',
    logo:
      'https://images.unsplash.com/photo-1487715433499-93bcf0e1c2b6?auto=format&fit=crop&w=160&q=80',
    scope: 'Consejo Superior',
    location: 'Ciudad Autonoma de Buenos Aires',
    electionDate: '10 Nov',
    candidates: [
      {
        id: 'inst-tec-1',
        name: 'Valentina Ruiz',
        proposal: 'Impulsar programas de mentoria y becas para estudiantes.',
        image:
          'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=300&q=80',
      },
      {
        id: 'inst-tec-2',
        name: 'Mateo Lopez',
        proposal: 'Modernizar la infraestructura tecnologica de la comunidad.',
        image:
          'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=300&q=80',
      },
      {
        id: 'inst-tec-3',
        name: 'Camila Fernandez',
        proposal: 'Promover iniciativas de sostenibilidad y reciclaje.',
        image:
          'https://images.unsplash.com/photo-1520813792240-56fc4a3765a7?auto=format&fit=crop&w=300&q=80',
      },
    ],
  },
  {
    id: 'inst-salud',
    name: 'Cooperativa de Salud Del Sur',
    description: 'Seleccionamos delegados para mejorar turnos y equipamiento medico.',
    logo:
      'https://images.unsplash.com/photo-1526256262350-7da7584cf5eb?auto=format&fit=crop&w=160&q=80',
    scope: 'Asamblea General',
    location: 'Cordoba',
    electionDate: '15 Nov',
    candidates: [
      {
        id: 'inst-salud-1',
        name: 'Sofia Aguilar',
        proposal: 'Crear un sistema unificado de turnos prioritarios.',
        image:
          'https://images.unsplash.com/photo-1521119989659-a83eee488004?auto=format&fit=crop&w=300&q=80',
      },
      {
        id: 'inst-salud-2',
        name: 'Ignacio Carballo',
        proposal: 'Invertir en nuevos equipos de diagnostico temprano.',
        image:
          'https://images.unsplash.com/photo-1502764613149-7f1d229e230f?auto=format&fit=crop&w=300&q=80',
      },
      {
        id: 'inst-salud-3',
        name: 'Helena Peralta',
        proposal: 'Fortalecer la red de atencion domiciliaria.',
        image:
          'https://images.unsplash.com/photo-1524502397800-2eeaad7c3fe5?auto=format&fit=crop&w=300&q=80',
      },
    ],
  },
  {
    id: 'inst-cultural',
    name: 'Red Cultural Barrio Abierto',
    description: 'Definimos las autoridades que administran talleres y festivales.',
    logo:
      'https://images.unsplash.com/photo-1470723710355-95304d8aece4?auto=format&fit=crop&w=160&q=80',
    scope: 'Comision Directiva',
    location: 'Rosario',
    electionDate: '18 Nov',
    candidates: [
      {
        id: 'inst-cultural-1',
        name: 'Diego Herrera',
        proposal: 'Fortalecer espacios culturales y deportivos abiertos a todos.',
        image:
          'https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=300&q=80',
      },
      {
        id: 'inst-cultural-2',
        name: 'Lucia Penon',
        proposal: 'Abrir residencias creativas y becas para artistas jovenes.',
        image:
          'https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=300&q=80',
      },
      {
        id: 'inst-cultural-3',
        name: 'Franco Quiroga',
        proposal: 'Llevar los festivales itinerantes a todos los barrios.',
        image:
          'https://images.unsplash.com/photo-1520340356584-8f7c1411407a?auto=format&fit=crop&w=300&q=80',
      },
    ],
  },
]

const normalizeInstitution = (institution, index = 0) => {
  const safeId = ensureNonEmptyString(institution?.id, `institution-${index}`)
  const logoSource =
    institution?.logo || institution?.logoUrl || institution?.image || institution?.imageUrl

  const candidateCountSource =
    institution?.totalCandidates ??
    institution?.membersCount ??
    institution?.candidatesCount ??
    (Array.isArray(institution?.candidates) ? institution.candidates.length : undefined)

  return {
    id: safeId,
    name: ensureNonEmptyString(institution?.name, 'Institucion sin nombre'),
    description: ensureNonEmptyString(
      institution?.description,
      'Sin descripcion disponible por el momento.'
    ),
    scope: ensureNonEmptyString(
      institution?.scope || institution?.type || institution?.bodyType,
      'Consejo Directivo'
    ),
    location: ensureNonEmptyString(
      institution?.location || institution?.city || institution?.region,
      'Ubicacion no informada'
    ),
    totalCandidates: ensureNonNegativeNumber(candidateCountSource, 0),
    logo: assignFallbackImage(safeId, logoSource, institutionFallbackImages),
  }
}

const getMockInstitutions = () =>
  mockInstitutions.map(({ candidates, ...institution }) => ({
    ...institution,
    logo: assignFallbackImage(institution.id, institution.logo, institutionFallbackImages),
    totalCandidates: candidates.length,
  }))

export const getInstitutions = async () => {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), INSTITUTIONS_TIMEOUT_MS)

  try {
    const response = await apiClient.get(INSTITUTIONS_ENDPOINT, {
      signal: controller.signal,
      timeout: INSTITUTIONS_TIMEOUT_MS,
    })

    const responseBody = parseResponseData(response.data)

    const institutionsPayload = Array.isArray(responseBody)
      ? responseBody
      : Array.isArray(responseBody?.data)
        ? responseBody.data
        : Array.isArray(responseBody?.content)
          ? responseBody.content
          : []

    if (!institutionsPayload.length) {
      console.warn('[api] El backend devolvio una lista vacia de instituciones. Usando mock.')
      return getMockInstitutions()
    }

    return institutionsPayload.map((institution, index) =>
      normalizeInstitution(institution, index)
    )
  } catch (error) {
    if (isAbortLikeError(error) || isTimeoutError(error)) {
      console.warn('[api] Timeout al consultar instituciones. Usando mock.')
    } else {
      console.warn('[api] Error al consultar instituciones reales. Usando mock.', error)
    }
    return getMockInstitutions()
  } finally {
    clearTimeout(timeoutId)
  }
}

export const getInstitutionById = (institutionId) => {
  const institution = mockInstitutions.find((item) => item.id === institutionId)
  if (!institution) {
    return null
  }

  return {
    ...institution,
    logo: assignFallbackImage(
      institution.id,
      institution.logo,
      institutionFallbackImages
    ),
  }
}

const buildMockCandidates = (institutionId) => {
  const institution = getInstitutionById(institutionId) ?? mockInstitutions[0]

  if (!institution) {
    return null
  }

  return institution.candidates.map((candidate) => {
    const fallbackKey = `${institution.id}-${candidate.id ?? candidate.name}`
    return {
      ...candidate,
      image: assignFallbackImage(fallbackKey, candidate.image, candidateFallbackImages),
      institutionId: institution.id,
      institutionName: institution.name,
      electionId: institution.id,
      electionName: institution.scope ?? 'Proceso electoral',
    }
  })
}

const getMockCandidates = (institutionId) =>
  new Promise((resolve, reject) => {
    setTimeout(() => {
      const candidates = buildMockCandidates(institutionId)
      if (!candidates) {
        reject(new Error('No encontramos la institucion solicitada.'))
        return
      }
      resolve(candidates)
    }, 500)
  })

const normalizeElectionCandidates = (
  elections,
  institutionContext,
  institutionIdForHash = 'institution'
) => {
  const resolvedInstitution = institutionContext ?? {
    id: institutionIdForHash,
    name: 'Institucion participante',
  }

  const normalized = []

  elections.forEach((election, electionIndex) => {
    const safeElectionId = ensureNonEmptyString(
      election?.id,
      `${resolvedInstitution.id}-election-${electionIndex + 1}`
    )
    const electionName = ensureNonEmptyString(
      election?.name || election?.title,
      'Proceso electoral'
    )

    const candidatesList = Array.isArray(election?.candidates) ? election.candidates : []

    candidatesList.forEach((rawCandidate, candidateIndex) => {
      const candidateObject =
        typeof rawCandidate === 'string' ? { name: rawCandidate } : rawCandidate ?? {}

      const safeCandidateId = ensureNonEmptyString(
        candidateObject.id,
        `${safeElectionId}-candidate-${candidateIndex + 1}`
      )
      const candidateName = ensureNonEmptyString(
        candidateObject.name,
        `Candidato ${candidateIndex + 1}`
      )

      const proposalFallback = `Propuesta presentada en ${electionName}.`

      normalized.push({
        id: safeCandidateId,
        name: candidateName,
        proposal: ensureNonEmptyString(
          candidateObject.proposal ||
            candidateObject.plan ||
            candidateObject.manifesto ||
            candidateObject.description,
          proposalFallback
        ),
        image: assignFallbackImage(
          `${resolvedInstitution.id}-${safeCandidateId}`,
          candidateObject.image || candidateObject.imageUrl,
          candidateFallbackImages
        ),
        electionId: safeElectionId,
        electionName,
        institutionId: resolvedInstitution.id,
        institutionName: ensureNonEmptyString(
          resolvedInstitution.name,
          'Institucion participante'
        ),
      })
    })
  })

  return normalized
}

const safeParseJson = (text) => {
  if (!text) {
    return null
  }
  try {
    return JSON.parse(text)
  } catch (error) {
    console.warn('[api] No se pudo parsear la respuesta JSON del backend.', error)
    return null
  }
}

const parseResponseData = (payload) => {
  if (typeof payload === 'string') {
    return safeParseJson(payload)
  }
  return payload ?? null
}

export const loginUser = async (username, attempt = 0) => {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), LOGIN_TIMEOUT_MS)

  try {
    const response = await apiClient.post(
      LOGIN_ENDPOINT,
      { displayName: username },
      {
        signal: controller.signal,
        timeout: LOGIN_TIMEOUT_MS,
        headers: {
          'Content-Type': 'application/json',
        },
      }
    )

    const responseBody = parseResponseData(response.data)

    const normalizedName = responseBody?.displayName || responseBody?.username || username

    return {
      ...responseBody,
      username: normalizedName,
    }
  } catch (error) {
    if (isAbortLikeError(error) || isTimeoutError(error)) {
      if (attempt < LOGIN_MAX_RETRIES) {
        console.warn(
          `[api] Login timeout (intento ${attempt + 1}). Reintentando hasta ${LOGIN_MAX_RETRIES} vez/veces.`
        )
        return loginUser(username, attempt + 1)
      }
      throw new Error(
        'El servicio de autenticacion tardo demasiado en responder. Volve a intentarlo.'
      )
    }

    if (error?.response) {
      const responseBody = parseResponseData(error.response.data)

      const backendMessage =
        responseBody?.message ||
        responseBody?.error ||
        responseBody?.details ||
        responseBody?.status

      const isNameError =
        error.response.status === 400 || error.response.status === 404
      const fallbackMessage = isNameError
        ? 'No encontramos un votante con ese nombre. Verifica los datos e intentalo nuevamente.'
        : 'No pudimos validar tus datos en este momento. Intentalo de nuevo en unos minutos.'

      const enrichedError = new Error(backendMessage || fallbackMessage)
      enrichedError.status = error.response.status
      enrichedError.details = responseBody
      throw enrichedError
    }

    if (error instanceof Error) {
      throw error
    }

    throw new Error('Ocurrio un error inesperado al iniciar sesion.')
  } finally {
    clearTimeout(timeoutId)
  }
}

export const getCandidates = async (institutionId, institutionContext = null) => {
  if (!institutionId) {
    return []
  }

  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), ELECTIONS_TIMEOUT_MS)

  const resolvedInstitution =
    institutionContext ??
    getInstitutionById(institutionId) ?? {
      id: institutionId,
      name: 'Institucion participante',
    }

  try {
    const response = await apiClient.get(buildInstitutionElectionsPath(institutionId), {
      signal: controller.signal,
      timeout: ELECTIONS_TIMEOUT_MS,
    })

    const responseBody = parseResponseData(response.data)

    const electionsPayload = Array.isArray(responseBody)
      ? responseBody
      : Array.isArray(responseBody?.data)
        ? responseBody.data
        : Array.isArray(responseBody?.content)
          ? responseBody.content
          : []

    const candidates = normalizeElectionCandidates(
      electionsPayload,
      resolvedInstitution,
      institutionId
    )

    if (!candidates.length) {
      console.warn('[api] La institucion no tiene candidatos visibles. Usando mock.')
      return getMockCandidates(institutionId)
    }

    return candidates
  } catch (error) {
    if (isAbortLikeError(error) || isTimeoutError(error)) {
      console.warn('[api] Timeout al consultar elecciones. Usando mock.')
    } else {
      console.warn('[api] Error al obtener elecciones reales. Usando mock.', error)
    }
    return getMockCandidates(institutionId)
  } finally {
    clearTimeout(timeoutId)
  }
}

const normalizeVoteResponse = (payload, fallbackVoteHash = null, fallbackSbtHash = null) => ({
  ...payload,
  voteTxHash:
    payload?.voteTxHash ||
    payload?.voteHash ||
    payload?.transactionHash ||
    payload?.txHash ||
    fallbackVoteHash,
  sbtTxHash:
    payload?.sbtTxHash ||
    payload?.sbtHash ||
    payload?.sbtTransactionHash ||
    payload?.sbtTx ||
    fallbackSbtHash,
})

export const submitVote = async (userId, candidateId, { electionId } = {}) => {
  if (!userId || !candidateId) {
    throw new Error('Faltan datos para emitir el voto.')
  }

  const requestPayload = {
    userId,
    candidateId,
    electionId,
  }

  const sanitizedPayload = Object.fromEntries(
    Object.entries(requestPayload).filter(
      ([, value]) => value !== undefined && value !== null && value !== ''
    )
  )

  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), VOTE_TIMEOUT_MS)

  try {
    const response = await apiClient.post(VOTE_ENDPOINT, sanitizedPayload, {
      signal: controller.signal,
      timeout: VOTE_TIMEOUT_MS,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    const responseBody = parseResponseData(response.data) ?? {}

    return normalizeVoteResponse(responseBody)
  } catch (error) {
    if (isAbortLikeError(error) || isTimeoutError(error)) {
      throw new Error(
        'El servicio de votos tardo demasiado en responder. Reintentemos en unos segundos.'
      )
    }

    if (error?.response) {
      const responseBody =
        parseResponseData(error.response.data) ?? { raw: error.response.data }
      const backendMessage =
        responseBody?.message ||
        responseBody?.error ||
        responseBody?.details ||
        responseBody?.status

      const enrichedError = new Error(
        backendMessage ||
          'No pudimos registrar tu voto en este momento. Por favor, intentalo de nuevo.'
      )
      enrichedError.status = error.response.status
      enrichedError.details = responseBody
      throw enrichedError
    }

    if (error instanceof Error) {
      throw error
    }

    throw new Error('Ocurrio un error inesperado al registrar el voto.')
  } finally {
    clearTimeout(timeoutId)
  }
}
