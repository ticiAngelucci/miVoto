import express from 'express'
import bodyParser from 'body-parser'
import cors from 'cors'
import { nanoid } from 'nanoid'

const PORT = process.env.PORT ? Number(process.env.PORT) : 9999
const INTERNAL_BASE_URL = process.env.BASE_URL || `http://localhost:${PORT}`
const PUBLIC_BASE_URL = process.env.PUBLIC_BASE_URL || INTERNAL_BASE_URL
const CLIENT_ID = process.env.CLIENT_ID || 'stub-client'
const CLIENT_SECRET = process.env.CLIENT_SECRET || 'stub-secret'
const DEFAULT_REDIRECT_URI = process.env.DEFAULT_REDIRECT_URI || 'http://localhost:8080/auth/miargentina/callback'
const BACKEND_BASE_URL = process.env.BACKEND_BASE_URL || 'http://localhost:8080'

const USERS = {
  ciudadano: {
    sub: 'stub-user',
    given_name: 'Ciudadano',
    family_name: 'Prueba',
    email: 'ciudadano@example.com'
  },
  maria: {
    sub: 'maria-garcia',
    given_name: 'María',
    family_name: 'García',
    email: 'maria@example.com'
  },
  roberto: {
    sub: 'roberto-sosa',
    given_name: 'Roberto',
    family_name: 'Sosa',
    email: 'roberto@example.com'
  },
  lucia: {
    sub: 'lucia-paredes',
    given_name: 'Lucía',
    family_name: 'Paredes',
    email: 'lucia@example.com'
  },
  diego: {
    sub: 'diego-flores',
    given_name: 'Diego',
    family_name: 'Flores',
    email: 'diego@example.com'
  },
  juan: {
    sub: 'juan-ibarra',
    given_name: 'Juan',
    family_name: 'Ibarra',
    email: 'juan@example.com'
  },
  carla: {
    sub: 'carla-martinez',
    given_name: 'Carla',
    family_name: 'Martínez',
    email: 'carla@example.com'
  },
  federico: {
    sub: 'federico-paz',
    given_name: 'Federico',
    family_name: 'Paz',
    email: 'federico@example.com'
  },
  valentina: {
    sub: 'valentina-rios',
    given_name: 'Valentina',
    family_name: 'Ríos',
    email: 'valentina@example.com'
  },
  martin: {
    sub: 'martin-acosta',
    given_name: 'Martín',
    family_name: 'Acosta',
    email: 'martin@example.com'
  },
  sofia: {
    sub: 'sofia-penna',
    given_name: 'Sofía',
    family_name: 'Penna',
    email: 'sofia@example.com'
  },
  nicolas: {
    sub: 'nicolas-ferri',
    given_name: 'Nicolás',
    family_name: 'Ferri',
    email: 'nicolas@example.com'
  },
  ana: {
    sub: 'ana-salazar',
    given_name: 'Ana',
    family_name: 'Salazar',
    email: 'ana@example.com'
  },
  gonzalo: {
    sub: 'gonzalo-roldan',
    given_name: 'Gonzalo',
    family_name: 'Roldán',
    email: 'gonzalo@example.com'
  },
  laura: {
    sub: 'laura-carrizo',
    given_name: 'Laura',
    family_name: 'Carrizo',
    email: 'laura@example.com'
  },
  pablo: {
    sub: 'pablo-lagos',
    given_name: 'Pablo',
    family_name: 'Lagos',
    email: 'pablo@example.com'
  }
}

const DEFAULT_USER = 'ciudadano'

const escapeHtml = (value) =>
  String(value).replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const fetchVoteStatuses = async (subjects) => {
  if (!subjects.length) {
    return new Map()
  }
  try {
    const url = new URL('/internal/vote-status', BACKEND_BASE_URL)
    subjects.forEach((subject) => url.searchParams.append('subjects', subject))
    const response = await fetch(url.toString(), { headers: { Accept: 'application/json' } })
    if (!response.ok) {
      throw new Error(`status ${response.status}`)
    }
    const payload = await response.json()
    return new Map(payload.map((entry) => [entry.subject, Boolean(entry.hasVoted)]))
  } catch (err) {
    console.warn('[miargentina-mock] vote status lookup failed:', err.message)
    return new Map()
  }
}

const renderAuthorizeSelection = (query, statusMap) => {
  const hiddenInputs = Object.entries(query)
    .filter(([key]) => key !== 'user')
    .map(([key, value]) => {
      if (Array.isArray(value)) {
        return value.map((item) => `<input type="hidden" name="${escapeHtml(key)}" value="${escapeHtml(item)}" />`).join('\n')
      }
      return `<input type="hidden" name="${escapeHtml(key)}" value="${escapeHtml(value)}" />`
    })
    .join('\n')

  const buttons = Object.entries(USERS)
    .map(([key, user]) => {
      const voted = statusMap.get(user.sub) === true
      const statusLabel = voted
        ? '<span class="status status--voted">Ya votó</span>'
        : '<span class="status status--pending">Disponible</span>'
      return `
        <button type="submit" name="user" value="${escapeHtml(key)}">
          <span class="user-label">${escapeHtml(user.given_name)} ${escapeHtml(user.family_name)} (${escapeHtml(user.email)})</span>
          ${statusLabel}
        </button>
      `
    })
    .join('<br/>')

  return `<!DOCTYPE html>
  <html lang="es">
  <head>
    <meta charset="utf-8" />
    <title>Elegir identidad - Mock MiArgentina</title>
    <style>
      body { font-family: sans-serif; margin: 2rem; }
      form { display: flex; flex-direction: column; gap: 0.5rem; max-width: 560px; }
      button { padding: 0.75rem 1rem; font-size: 1rem; cursor: pointer; display: flex; justify-content: space-between; align-items: center; border: 1px solid #d1d5db; border-radius: 6px; text-align: left; background: #ffffff; }
      button:hover { border-color: #2563eb; }
      .user-label { display: block; font-weight: 600; }
      .status { font-size: 0.85rem; padding: 0.2rem 0.5rem; border-radius: 999px; }
      .status--voted { background: #fee2e2; color: #991b1b; }
      .status--pending { background: #dcfce7; color: #166534; }
    </style>
  </head>
  <body>
    <h1>Mock MiArgentina</h1>
    <p>Seleccioná una identidad para continuar el flujo OAuth.</p>
    <form method="get" action="/oauth/authorize">
      ${hiddenInputs}
      ${buttons}
    </form>
  </body>
  </html>`
}

const app = express()
app.use(cors())
app.use(bodyParser.urlencoded({ extended: true }))
app.use(bodyParser.json())

const authorizationCodes = new Map()

const configuration = {
  issuer: PUBLIC_BASE_URL,
  authorization_endpoint: `${PUBLIC_BASE_URL}/oauth/authorize`,
  token_endpoint: `${PUBLIC_BASE_URL}/oauth/token`,
  jwks_uri: `${PUBLIC_BASE_URL}/.well-known/jwks.json`,
  response_types_supported: ['code'],
  grant_types_supported: ['authorization_code', 'refresh_token'],
  scopes_supported: ['openid', 'profile'],
  token_endpoint_auth_methods_supported: ['client_secret_basic', 'client_secret_post']
}

const jwks = {
  keys: [
    {
      kty: 'oct',
      k: 'c3R1Yi1qa2V5',
      alg: 'HS256',
      use: 'sig',
      kid: 'stub'
    }
  ]
}

app.get('/', async (_req, res) => {
  const subjects = Object.values(USERS).map((user) => user.sub)
  const statusMap = await fetchVoteStatuses(subjects)
  const accountsList = Object.entries(USERS)
    .map(([key, user]) => {
      const voted = statusMap.get(user.sub) === true
      const badge = voted ? ' <strong>(ya votó)</strong>' : ''
      return `<li><strong>${escapeHtml(user.given_name)} ${escapeHtml(user.family_name)}</strong> — ${escapeHtml(user.email)} (<code>${escapeHtml(key)}</code>)${badge}</li>`
    })
    .join('\n')

  res.send(`<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <title>Mock MiArgentina OAuth</title>
  <style>
    body { font-family: sans-serif; margin: 2rem; }
    code { background: #f1f1f1; padding: 0.1rem 0.2rem; }
  </style>
</head>
<body>
  <h1>Mock OAuth MiArgentina</h1>
  <p>Servidor de prueba escuchando en <code>${PUBLIC_BASE_URL}</code>.</p>
  <p>Usuarios disponibles:</p>
  <ul>
    ${accountsList}
  </ul>
  <p>
    <a href="${PUBLIC_BASE_URL}/oauth/authorize?response_type=code&client_id=${CLIENT_ID}&scope=openid+profile&redirect_uri=${encodeURIComponent(DEFAULT_REDIRECT_URI)}">
      Iniciar sesión (seleccionar identidad)
    </a>
  </p>
  <p><a href="${PUBLIC_BASE_URL}/.well-known/openid-configuration">/.well-known/openid-configuration</a></p>
</body>
</html>`)
})

app.get('/.well-known/openid-configuration', (_req, res) => {
  res.json(configuration)
})

app.get('/.well-known/jwks.json', (_req, res) => {
  res.json(jwks)
})

app.get('/oauth/authorize', async (req, res) => {
  const { response_type, client_id, redirect_uri, state } = req.query
  if (response_type !== 'code') {
    return res.status(400).json({ error: 'unsupported_response_type' })
  }
  if (client_id !== CLIENT_ID) {
    return res.status(400).json({ error: 'invalid_client' })
  }

  const userKeyRaw = req.query.user
  const userKey = Array.isArray(userKeyRaw) ? userKeyRaw[0] : userKeyRaw
  const selectedUser = userKey ? USERS[userKey] : undefined
  if (!selectedUser) {
    const subjects = Object.values(USERS).map((user) => user.sub)
    const statusMap = await fetchVoteStatuses(subjects)
    return res.send(renderAuthorizeSelection(req.query, statusMap))
  }

  const targetRedirect = redirect_uri || DEFAULT_REDIRECT_URI
  const code = nanoid(16)
  authorizationCodes.set(code, {
    redirectUri: targetRedirect,
    createdAt: Date.now(),
    user: selectedUser
  })
  const url = new URL(targetRedirect)
  url.searchParams.set('code', code)
  if (state) {
    url.searchParams.set('state', state)
  }
  res.redirect(url.toString())
})

app.post('/oauth/token', (req, res) => {
  const {
    code,
    client_id: clientId,
    client_secret: clientSecret,
    redirect_uri: redirectUri,
    grant_type: grantType
  } = req.body

  console.log('[/oauth/token] payload', req.body)

  if (grantType !== 'authorization_code') {
    return res.status(400).json({ error: 'unsupported_grant_type' })
  }
  if (clientId !== CLIENT_ID || clientSecret !== CLIENT_SECRET) {
    return res.status(400).json({ error: 'invalid_client' })
  }
  const payload = authorizationCodes.get(code)
  if (!payload) {
    return res.status(400).json({ error: 'invalid_grant' })
  }
  if (redirectUri && redirectUri !== payload.redirectUri) {
    return res.status(400).json({ error: 'redirect_uri_mismatch' })
  }

  authorizationCodes.delete(code)

  const user = payload.user || USERS[DEFAULT_USER]
  const idTokenPayload = Buffer.from(JSON.stringify(user)).toString('base64url')

  res.json({
    access_token: 'mock-access-token',
    token_type: 'Bearer',
    expires_in: 3600,
    refresh_token: 'mock-refresh-token',
    id_token: `stub-id-token.${idTokenPayload}`
  })
})

app.get('/oauth/userinfo', (_req, res) => {
  res.json(USERS[DEFAULT_USER])
})

app.listen(PORT, () => {
  console.log(`MiArgentina mock OAuth server listening on ${INTERNAL_BASE_URL}`)
})
