const DEMO_USERS = [
  { id: '1', name: 'Juan Pérez' },
  { id: '2', name: 'María González' },
  { id: '3', name: 'Lucía Romero' },
]

function normalizeName(value) {
  return value.trim().toLowerCase()
}

function findLocalUserByName(name) {
  const normalizedQuery = normalizeName(name)
  return DEMO_USERS.find((user) => normalizeName(user.name) === normalizedQuery)
}

export async function findUserByName(name) {
  // Simulamos latencia para imitar la consulta al backend/Firebase.
  await new Promise((resolve) => {
    setTimeout(resolve, 600)
  })

  return findLocalUserByName(name)
}

export function isValidName(value) {
  return Boolean(value?.trim())
}
