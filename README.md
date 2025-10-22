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

## Paso 2 – Gestión de instituciones, candidatos y flujo de voto

- **Instituciones y candidatos:**
  - CRUD disponible en `POST/GET/PUT/DELETE /institutions/{id}` y `POST/GET/PUT/DELETE /candidates/{id}`.
  - `GET /candidates` acepta `?institutionId=` para filtrar por institución.
- **Voto real:**
  - `POST /votes/cast` ahora espera un cuerpo con selección explícita de institución y candidatos:

    ```json
    {
      "ballotId": "1",
      "eligibilityToken": "...",
      "selection": {
        "institutionId": "inst-1",
        "candidateIds": ["cand-1"]
      }
    }
    ```

  - La selección se valida contra la boleta y los candidatos activos de la institución.
- **Recuento:**
  - `GET /ballots/{id}/tally` devuelve resultados normalizados por candidato incluyendo nombre/lista y momento del cálculo.

## Paso 3 – Cierre y publicación de resultados

- **Finalización de boleta:**
  - `POST /ballots/{id}/finalize` sólo permite cerrar boletas cuyo período ya terminó. Guarda el snapshot con hash de verificación y emite evento de auditoría.
  - `GET /ballots/{id}/result` recupera el snapshot final (404 si aún no se cerró).
- **Integridad:**
  - El backend almacena un `checksum` (`HashingService.hashTally`) derivado de los votos y la marca de tiempo. Ese valor se devuelve al frontend para validar en reportes externos.

## Paso 4 – Frontend operativo

- Nueva UI en Vite/React que consume los endpoints mock del backend:
  - Lista instituciones, candidatos y boletas mediante `GET /institutions`, `/candidates` y `/ballots`.
  - Permite emitir votos reales (`POST /votes/cast`) seleccionando candidatos y pegando el token de elegibilidad.
  - Visualiza recuento en vivo (`GET /ballots/{id}/tally`), cierre definitivo (`POST /ballots/{id}/finalize`, `GET /ballots/{id}/result`) y verifica recibos (`GET /votes/{receipt}/verify`).
- El build se valida con `npm run build` dentro de `frontend/miVotoFrontend`.
- En Firestore, las colecciones utilizadas por el backend llevan nombres en español: `instituciones`, `candidatos`, `boletas`, `votos` y `resultadosBoleta`.
