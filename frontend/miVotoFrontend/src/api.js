const mockCandidates = [
  {
    id: 1,
    name: 'Valentina Ruiz',
    proposal: 'Impulsar programas de mentoría y becas para estudiantes.',
    image:
      'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=150&q=80',
  },
  {
    id: 2,
    name: 'Mateo López',
    proposal: 'Modernizar la infraestructura tecnológica de la comunidad.',
    image:
      'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=150&q=80',
  },
  {
    id: 3,
    name: 'Camila Fernández',
    proposal: 'Promover iniciativas de sostenibilidad y reciclaje.',
    image:
      'https://images.unsplash.com/photo-1520813792240-56fc4a3765a7?auto=format&fit=crop&w=150&q=80',
  },
  {
    id: 4,
    name: 'Diego Herrera',
    proposal: 'Fortalecer espacios culturales y deportivos abiertos para todos.',
    image:
      'https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=150&q=80',
  },
]

export const loginUser = (username) => {
  console.log(`(API Mock) Verificando a: ${username}`)
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (username.toLowerCase() === 'error') {
        reject(new Error('Usuario no encontrado en el padrón'))
      } else {
        resolve({ username, token: 'fake-jwt-token-123' })
      }
    }, 500)
  })
}

export const getCandidates = () => {
  console.log('(API Mock) Obteniendo candidatos...')
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(mockCandidates)
    }, 800)
  })
}

export const submitVote = (username, candidateId) => {
  console.log(`(API Mock) ${username} votó por ${candidateId}`)
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
