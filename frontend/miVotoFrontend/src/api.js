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

export const getInstitutions = () => {
  console.log('(API Mock) Obteniendo instituciones...')
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(
        mockInstitutions.map(({ candidates, ...institution }) => ({
          ...institution,
          logo: assignFallbackImage(institution.id, institution.logo, institutionFallbackImages),
          totalCandidates: candidates.length,
        }))
      )
    }, 600)
  })
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

export const loginUser = (username) => {
  console.log(`(API Mock) Verificando a: ${username}`)
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (username.toLowerCase() === 'error') {
        reject(new Error('Usuario no encontrado en el padron'))
      } else {
        resolve({ username, token: 'fake-jwt-token-123' })
      }
    }, 500)
  })
}

export const getCandidates = (institutionId) => {
  console.log('(API Mock) Obteniendo candidatos...', institutionId)
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const institution = getInstitutionById(institutionId) ?? mockInstitutions[0]

      if (!institution) {
        reject(new Error('No encontramos la institucion solicitada.'))
        return
      }

      resolve(
        institution.candidates.map((candidate) => {
          const fallbackKey = `${institution.id}-${candidate.id ?? candidate.name}`
          return {
            ...candidate,
            image: assignFallbackImage(
              fallbackKey,
              candidate.image,
              candidateFallbackImages
            ),
            institutionId: institution.id,
            institutionName: institution.name,
          }
        })
      )
    }, 700)
  })
}

export const submitVote = (username, candidateId) => {
  console.log(`(API Mock) ${username} voto por ${candidateId}`)
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        success: true,
        voteTxHash: `0x-fake-vote-hash-${Math.random().toString(16).slice(2)}`,
        sbtTxHash: `0x-fake-sbt-constancia-${Math.random().toString(16).slice(2)}`,
      })
    }, 1000)
  })
}
