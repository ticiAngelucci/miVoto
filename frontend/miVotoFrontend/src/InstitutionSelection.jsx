import { useEffect, useState } from 'react'
import {
  Container,
  Typography,
  Box,
  Grid,
  Card,
  CardActionArea,
  CardContent,
  Avatar,
  Chip,
} from '@mui/material'
import { getInstitutions } from './api'
import AppBackground from './components/AppBackground'
import PageLogo from './components/PageLogo'

function InstitutionSelection({ user, onSelectInstitution }) {
  const [institutions, setInstitutions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const displayName = user?.displayName || user?.username || user || 'votante'

  useEffect(() => {
    const fetchInstitutions = async () => {
      setLoading(true)
      setError(null)
      try {
        const data = await getInstitutions()
        setInstitutions(data)
      } catch (err) {
        console.error('Error fetching institutions:', err)
        setError('No pudimos cargar las instituciones. Intenta nuevamente.')
      } finally {
        setLoading(false)
      }
    }

    fetchInstitutions()
  }, [])

  const handleSelect = (institution) => {
    if (onSelectInstitution) {
      onSelectInstitution(institution)
    }
  }

  const renderStatus = (title, subtitle, color = '#ffffff') => (
    <AppBackground>
      <Box sx={{ mt: 10, textAlign: 'center', color }}>
        <Typography variant="h5">{title}</Typography>
        {subtitle ? (
          <Typography variant="body1" sx={{ mt: 1, opacity: 0.8 }}>
            {subtitle}
          </Typography>
        ) : null}
      </Box>
    </AppBackground>
  )

  if (loading) {
    return renderStatus('Cargando instituciones...', 'Estamos preparando tus opciones.')
  }

  if (error) {
    return renderStatus(error, 'Reintentaremos en unos segundos.', '#ffe0e0')
  }

  return (
    <AppBackground>
      <Container
        component="main"
        maxWidth="lg"
        sx={{ position: 'relative', zIndex: 1, color: '#ffffff' }}
      >
        <Box
          sx={{
            marginTop: { xs: 2, md: 4 },
            padding: { xs: 4, md: 6 },
            borderRadius: 3,
            bgcolor: 'rgba(12, 22, 56, 0.55)',
            boxShadow: '0 24px 60px rgba(9, 18, 54, 0.35)',
            backdropFilter: 'blur(18px)',
            width: '100%',
            maxWidth: 1160,
            mx: 'auto',
            pb: { xs: 5, md: 7 },
          }}
        >
          <PageLogo />
          <Box
            sx={{
              textAlign: 'center',
              mb: 4,
              bgcolor: 'rgba(0, 51, 102, 0.85)',
              borderRadius: 2,
              p: { xs: 3, md: 4 },
              boxShadow: '0 18px 40px rgba(0, 18, 58, 0.45)',
            }}
          >
            <Typography component="h1" variant="h5" sx={{ fontWeight: 'bold' }}>
              Hola {displayName}, elegi la institucion en la que queres votar
            </Typography>
            <Typography variant="subtitle1" sx={{ mt: 1, opacity: 0.8 }}>
              Cada organismo tiene su propio cuerpo de representantes. Podes explorar las
              propuestas antes de emitir tu voto.
            </Typography>
          </Box>

          <Grid
            container
            spacing={{ xs: 3, md: 4 }}
            justifyContent="center"
            alignItems="stretch"
          >
            {institutions.map((institution) => (
              <Grid item xs={12} sm={6} md={4} key={institution.id}>
                <Card
                  sx={{
                    height: '100%',
                    borderRadius: 3,
                    background:
                      'linear-gradient(180deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.04) 100%)',
                    border: '1px solid rgba(255,255,255,0.15)',
                    boxShadow: '0 18px 38px rgba(6, 18, 54, 0.25)',
                    transition:
                      'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                    '&:hover': {
                      transform: 'translateY(-6px)',
                      boxShadow: '0 24px 52px rgba(6, 18, 54, 0.35)',
                    },
                  }}
                >
                  <CardActionArea
                    sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}
                    onClick={() => handleSelect(institution)}
                  >
                    <Box
                      sx={{
                        width: '100%',
                        display: 'flex',
                        justifyContent: 'center',
                        pt: 4,
                      }}
                    >
                      <Avatar
                        alt={institution.name}
                        src={institution.logo}
                        sx={{
                          width: 90,
                          height: 90,
                          borderRadius: 2,
                          border: '2px solid rgba(255,255,255,0.4)',
                          boxShadow: '0 14px 30px rgba(0,0,0,0.35)',
                        }}
                      />
                    </Box>
                    <CardContent
                      sx={{
                        flexGrow: 1,
                        textAlign: 'center',
                        color: '#ffffff',
                        px: { xs: 3, md: 4 },
                        pb: 4,
                      }}
                    >
                      <Typography variant="h6" sx={{ fontWeight: 600 }}>
                        {institution.name}
                      </Typography>
                      <Typography variant="body2" sx={{ mt: 1.5, opacity: 0.85 }}>
                        {institution.description}
                      </Typography>
                      <Box
                        sx={{
                          mt: 2.5,
                          display: 'flex',
                          flexWrap: 'wrap',
                          gap: 1,
                          justifyContent: 'center',
                        }}
                      >
                        <Chip
                          size="small"
                          label={institution.scope}
                          sx={{ bgcolor: 'rgba(255,255,255,0.08)', color: '#ffffff' }}
                        />
                        <Chip
                          size="small"
                          label={`${institution.totalCandidates} candidatos`}
                          sx={{ bgcolor: 'rgba(255,255,255,0.08)', color: '#ffffff' }}
                        />
                        <Chip
                          size="small"
                          label={institution.location}
                          sx={{ bgcolor: 'rgba(255,255,255,0.08)', color: '#ffffff' }}
                        />
                      </Box>
                      <Box
                        sx={{
                          mt: 3,
                          fontWeight: 600,
                          px: 3,
                          py: 1,
                          borderRadius: 999,
                          bgcolor: 'rgba(255,215,0,0.15)',
                          color: '#FFD700',
                          display: 'inline-block',
                        }}
                      >
                        Ver postulantes
                      </Box>
                    </CardContent>
                  </CardActionArea>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>
      </Container>
    </AppBackground>
  )
}

export default InstitutionSelection
