# miVoto - proyecto  arquitectura

Documentación de la base de datos híbrida

Esta documentación describe la arquitectura de datos del proyecto, que utiliza Cloud Firestone (Centralizado) para la gestión de identidad y control, y la Blockchain (Desentralizado) para el registro anónimo e inmutable de votos.

1. Arquitectura y principios

   Identidad: Firebase Authentication / Colección votantes: Gestiona el login y el padrón electoral (quién puede votar).

   Anonimato: Blockchain (Contrato inteligente): Garantiza que el voto real (por quién se vota) no pueda vincularse a la identidad del votante.

   Integridad: Colección votos_emitidos / Blockchain: La colección previene el doble voto. La blockchain asegura que el voto no pueda ser alterado.

   Conexión: Campo contrato_address: Une la configuración de la elección centralizada con el Smart Contract descentralizado.

2. Colecciones de Cloud Firestone (Capas centralizadas)
   1. Colección votantes: (padrón e identidad): Almacenar la información personal y el estado de elegibilidad de los votantes.
   2. Colección elecciones: (metadatos y conexión): Definir los parámetros de cada evento de votación y establecer el puente a la Blockchain.
   3. Colección votos_emitidos (control de doble voto): Prevenir el doble voto y registrar la prueba criptográfica de participación, sin almacenar el voto real.


3. Capa descentralizada: Blockchain
   El contrato inteligente (contrato_address) contiene la única fuente de verdad sobre el conteo de votos.
      * Entrada de votos: Solo acepta la opción y la firma de la clave del backend de FastAPI. Nunca acepta la UID del votante
      * Almacenamiento: Mantiene un registro interno para contar el total de votos por opción de forma anónima.
      * Resultados: Expone una función para que cualquier auditor pueda ver los resultados consolidados.
