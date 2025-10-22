import express from 'express';
import bodyParser from 'body-parser';
import cors from 'cors';
import { nanoid } from 'nanoid';

const PORT = process.env.PORT ? Number(process.env.PORT) : 9999;
const INTERNAL_BASE_URL = process.env.BASE_URL || `http://localhost:${PORT}`;
const PUBLIC_BASE_URL = process.env.PUBLIC_BASE_URL || INTERNAL_BASE_URL;
const CLIENT_ID = process.env.CLIENT_ID || 'stub-client';
const CLIENT_SECRET = process.env.CLIENT_SECRET || 'stub-secret';
const DEFAULT_REDIRECT_URI = process.env.DEFAULT_REDIRECT_URI || 'http://localhost:8080/auth/miargentina/callback';

const app = express();
app.use(cors());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

const authorizationCodes = new Map();

const configuration = {
  issuer: PUBLIC_BASE_URL,
  authorization_endpoint: `${PUBLIC_BASE_URL}/oauth/authorize`,
  token_endpoint: `${PUBLIC_BASE_URL}/oauth/token`,
  jwks_uri: `${PUBLIC_BASE_URL}/.well-known/jwks.json`,
  response_types_supported: ['code'],
  grant_types_supported: ['authorization_code', 'refresh_token'],
  scopes_supported: ['openid', 'profile'],
  token_endpoint_auth_methods_supported: ['client_secret_basic', 'client_secret_post']
};

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
};

app.get('/', (_req, res) => {
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
  <ul>
    <li><a href="${PUBLIC_BASE_URL}/.well-known/openid-configuration">/.well-known/openid-configuration</a></li>
    <li><a href="${PUBLIC_BASE_URL}/oauth/authorize?response_type=code&client_id=${CLIENT_ID}&scope=openid+profile&redirect_uri=${encodeURIComponent(DEFAULT_REDIRECT_URI)}">Probar autorización</a></li>
  </ul>
</body>
</html>`);
});

app.get('/.well-known/openid-configuration', (_req, res) => {
  res.json(configuration);
});

app.get('/.well-known/jwks.json', (_req, res) => {
  res.json(jwks);
});

app.get('/oauth/authorize', (req, res) => {
  const { response_type, client_id, redirect_uri, state } = req.query;
  if (response_type !== 'code') {
    return res.status(400).json({ error: 'unsupported_response_type' });
  }
  if (client_id !== CLIENT_ID) {
    return res.status(400).json({ error: 'invalid_client' });
  }
  const targetRedirect = redirect_uri || DEFAULT_REDIRECT_URI;
  const code = nanoid(16);
  authorizationCodes.set(code, {
    redirectUri: targetRedirect,
    createdAt: Date.now()
  });
  const url = new URL(targetRedirect);
  url.searchParams.set('code', code);
  if (state) {
    url.searchParams.set('state', state);
  }
  res.redirect(url.toString());
});

app.post('/oauth/token', (req, res) => {
  const {
    code,
    client_id: clientId,
    client_secret: clientSecret,
    redirect_uri: redirectUri,
    grant_type: grantType
  } = req.body;

  console.log('[/oauth/token] payload', req.body);

  if (grantType !== 'authorization_code') {
    return res.status(400).json({ error: 'unsupported_grant_type' });
  }
  if (clientId !== CLIENT_ID || clientSecret !== CLIENT_SECRET) {
    return res.status(400).json({ error: 'invalid_client' });
  }
  const payload = authorizationCodes.get(code);
  if (!payload) {
    return res.status(400).json({ error: 'invalid_grant' });
  }
  if (redirectUri && redirectUri !== payload.redirectUri) {
    return res.status(400).json({ error: 'redirect_uri_mismatch' });
  }

  authorizationCodes.delete(code);

  res.json({
    access_token: 'mock-access-token',
    token_type: 'Bearer',
    expires_in: 3600,
    refresh_token: 'mock-refresh-token',
    id_token: 'stub-id-token'
  });
});

app.get('/oauth/userinfo', (_req, res) => {
  res.json({
    sub: 'stub-user',
    name: 'Ciudadano Prueba',
    given_name: 'Ciudadano',
    family_name: 'Prueba',
    email: 'ciudadano@example.com'
  });
});

app.listen(PORT, () => {
  console.log(`MiArgentina mock OAuth server listening on ${INTERNAL_BASE_URL}`);
});
