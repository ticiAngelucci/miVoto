# miVoto - proyecto arquitectura

## Requisitos previos

- Docker 20.10+ y Docker Compose Plugin.
- (Opcional) Node.js 20+ y Maven 3.9+ para ejecutar sin contenedores.
- Nodo EVM opcional (Hardhat/Anvil) si querés probar contra una red real; por defecto usamos clientes mock en memoria.

## Variables de entorno clave

El backend necesita los siguientes valores (ver `docker/env/backend.env`):

- Credenciales OAuth2 de MiArgentina (`MIARG_*`).
- Proyecto de Firebase (`FIREBASE_PROJECT_ID`).
- Endpoint RPC de blockchain (`WEB3_RPC_URL`, `WEB3_PRIVATE_KEY`, `VOTE_CONTRACT_ADDR`). Usa `WEB3_MOCK_ENABLED=true` para modo simulador local.
- Peppers para hashing (`ELIGIBILITY_*`).
- Configuración JWT (`JWT_*`).

Por defecto el archivo apunta a emuladores alojados en `host.docker.internal`. Ajusta los valores según tu entorno.

## Levantar con Docker

1. Edita los valores de `docker/env/backend.env` si querés cambiar tokens/peppers.
2. Construye y levanta los servicios:

   ```bash
   docker compose up --build
   ```

El compose arranca tres contenedores: backend (Spring Boot), frontend (Vite build) y `miargentina-mock` (servidor OAuth de prueba).

4. Backend disponible en `http://localhost:8080`, frontend en `http://localhost:3000`.

> **Nota:** para salir del modo mock deberás proporcionar Firestore real y un nodo EVM actualizando los valores `FIREBASE_*`, `WEB3_*` y `MIARG_*`.

## Levantar sin Docker

```bash
# Backend
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run

# Frontend
cd frontend/miVotoFrontend
npm install
npm run dev
```

## Tests

```bash
mvn test
```
