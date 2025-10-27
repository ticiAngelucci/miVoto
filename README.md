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
  - Si tenés una sesión iniciada podés generar el token con `POST /eligibility/issue/session` (la UI expone el botón "Obtener token desde la sesión").
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
  - Permite emitir votos reales (`POST /votes/cast`) seleccionando candidatos y obteniendo el token de elegibilidad desde la sesión.
  - Visualiza recuento en vivo (`GET /ballots/{id}/tally`), cierre definitivo (`POST /ballots/{id}/finalize`, `GET /ballots/{id}/result`) y verifica recibos (`GET /votes/{receipt}/verify`).
- El build se valida con `npm run build` dentro de `frontend/miVotoFrontend`.
- En Firestore, las colecciones utilizadas por el backend llevan nombres en español: `instituciones`, `candidatos`, `boletas`, `votos` y `resultadosBoleta`.
- Para precargar datos demo habilitá `SEED_ENABLED=true` y ejecutá `POST /seed/default` (crea institución, candidatos y boleta si no existen).

## Automatización de seed y voto

Para acelerar las pruebas manuales del flujo end-to-end agregamos scripts en `scripts/` y un `Makefile` con comandos listos para usar. Todos asumen que el backend está escuchando en `http://localhost:8080` y que el mock de MiArgentina corre en `http://localhost:9999`.

1. `make login` inicia el flujo OAuth contra el mock y guarda la cookie de sesión en `.tmp/cookies/session.jar`. Devuelve los datos del usuario autenticado.
2. `make seed` ejecuta `POST /seed/default` reutilizando la sesión previa.
3. `make vote` solicita un token con `POST /eligibility/issue/session` y emite un voto contra la boleta configurada (por defecto `ballotId=1`, `cand-1`).
4. `make tally` consulta `GET /ballots/{id}/tally` para revisar el recuento.
5. `make token` obtiene sólo el token de elegibilidad en texto plano, mientras que `make token-json` devuelve el JSON completo.
6. `make seed-close` fuerza el cierre inmediato de la boleta indicada (por defecto `1`).

Variables útiles que podés sobreescribir al invocar `make`:

- `BASE_URL`, `OAUTH_AUTHORIZE_URL` y `COOKIE_JAR` para apuntar a otros entornos o cambiar la ubicación del archivo de cookies.
- `BALLOT_ID`, `INSTITUTION_ID` y `CANDIDATE_IDS` para votar sobre otra boleta o lista (por ejemplo `make vote CANDIDATE_IDS=cand-2`).
- `WALLET_ADDRESS` define la cuenta a la que se emitirá el SBT cuando se registra el voto. Por defecto apunta a la primera cuenta de Hardhat/Anvil (`0xf39f…`).

Si querés descartar la sesión almacenada, corré `make clean-cookies`. Todos los scripts requieren `python3` y `curl`, ambos disponibles por defecto en macOS/Linux.

## Usuarios del mock MiArgentina

El servidor OAuth de prueba (`docker compose` levanta `miargentina-mock`) permite elegir identidades diferentes al iniciar sesión. Al acceder a `http://localhost:9999/` verás la lista de cuentas demo (`ciudadano`, `maria`, `roberto`). Cada vez que se dispara el flujo OAuth se muestra una pantalla para seleccionar el usuario antes de redirigir al backend. El `id_token` generado incluye los datos básicos (sub, nombre, apellido, email) para que el backend distinga a cada ciudadano.

## Integración con blockchain real

1. Desplegá `MiVotoSoulboundToken` y `MiVotoElection` (ver `contracts/MiVoto.sol`). El constructor del SBT recibe la dirección que actuará como minter inicial; luego ejecutá `setMinter(<address del contrato de voto>)`. Podés usar el script de ejemplo `contracts/deploy.sample.js` con Hardhat (`npx hardhat run --network <net> contracts/deploy.sample.js`).
2. Configurá la app con variables reales:
   - `WEB3_RPC_URL`, `WEB3_CHAIN_ID`, `WEB3_GAS_PRICE`, `WEB3_GAS_LIMIT` según tu nodo.
   - `WEB3_PRIVATE_KEY` con la clave del backend que firmará las transacciones.
   - `VOTE_CONTRACT_ADDR` con la dirección del contrato `MiVotoElection` desplegado.
   - `WEB3_MOCK_ENABLED=false` para habilitar el envío real de transacciones.
3. Reiniciá el backend y asegurate de que `SEED_ENABLED=false` en producción para evitar datos demo.
4. Desde el frontend, cada votante debe cargar una dirección de wallet válida (formato `0x...`) antes de solicitar el token de elegibilidad; ese address será el destinatario del SBT cuando se emita el voto.

Para probar en redes como Sepolia/Amoy podés usar Hardhat/Anvil para desplegar los contratos y luego apuntar la app a ese RPC. La respuesta de `POST /votes/cast` ahora incluye `sbtTokenId` y el frontend muestra el ID acuñado para facilitar la validación con block explorers.
