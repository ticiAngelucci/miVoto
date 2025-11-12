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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Link,
  Chip,
} from '@mui/material'
import WarningIcon from '@mui/icons-material/Warning'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import SwapHorizIcon from '@mui/icons-material/SwapHoriz'
import { getCandidates, submitVote } from './api'
import AppBackground from './components/AppBackground'
import PageLogo from './components/PageLogo'
import './App.css'

function VotingPage({ user, institution, onChangeInstitution }) {
  const [candidates, setCandidates] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [openConfirmDialog, setOpenConfirmDialog] = useState(false)
  const [selectedCandidate, setSelectedCandidate] = useState(null)
  const [voted, setVoted] = useState(false)
  const [openSuccessDialog, setOpenSuccessDialog] = useState(false)
  const [voteHash, setVoteHash] = useState(null)
  const [sbtHash, setSbtHash] = useState(null)

  const institutionId = institution?.id ?? null
  const userId = user?.id ?? null
  const userDisplayName = user?.displayName || user?.username || 'votante'

  useEffect(() => {
    if (!institutionId) {
      setError('Selecciona una institucion para ver los candidatos.')
      setLoading(false)
      setCandidates([])
      return
    }

    const fetchCandidates = async () => {
      setLoading(true)
      setError(null)
      setSelectedCandidate(null)
      setVoted(false)
      setOpenSuccessDialog(false)
      setVoteHash(null)
      setSbtHash(null)

      try {
        const data = await getCandidates(institutionId, institution)
        setCandidates(data)
      } catch (err) {
        console.error('Error fetching candidates:', err)
        setError('Error al cargar los candidatos. Intenta nuevamente en unos minutos.')
      } finally {
        setLoading(false)
      }
    }

    fetchCandidates()
  }, [institutionId])

  const handleOpenConfirmDialog = (candidate) => {
    if (!voted) {
      setSelectedCandidate(candidate)
      setOpenConfirmDialog(true)
    }
  }

  const handleCloseConfirmDialog = () => {
    setOpenConfirmDialog(false)
    setSelectedCandidate(null)
  }

  const handleCloseSuccessDialog = () => {
    setOpenSuccessDialog(false)
  }

  const handleConfirmVote = async () => {
    if (!selectedCandidate) {
      return
    }

    try {
      if (!userId) {
        throw new Error('No pudimos identificar al votante. Volve a iniciar sesion.')
      }
      const response = await submitVote(userId, selectedCandidate.id, {
        electionId: selectedCandidate?.electionId ?? institutionId ?? undefined,
      })
      setVoteHash(response.voteTxHash ?? null)
      setSbtHash(response.sbtTxHash ?? null)
      setVoted(true)
      handleCloseConfirmDialog()
      setOpenSuccessDialog(true)
    } catch (err) {
      console.error('Error submitting vote:', err)
      const message =
        err instanceof Error
          ? err.message
          : 'Hubo un problema al registrar tu voto. Intenta nuevamente.'
      alert(message)
      handleCloseConfirmDialog()
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

  if (!institutionId) {
    return renderStatus(
      'Selecciona una institucion para continuar.',
      'Volveras a esta pantalla cuando elijas una opcion.'
    )
  }

  if (loading) {
    return renderStatus('Cargando candidatos...', 'Buscando representantes disponibles...')
  }

  if (error) {
    return renderStatus(error, 'Intentaremos nuevamente en unos segundos.', '#ffe0e0')
  }

  return (
    <AppBackground>
      <Container
        component="main"
        maxWidth="lg"
        sx={{
          position: 'relative',
          zIndex: 1,
          color: '#ffffff',
        }}
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

          {onChangeInstitution && (
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
              <Button
                onClick={onChangeInstitution}
                startIcon={<SwapHorizIcon />}
                sx={{
                  px: 3,
                  py: 1.5,
                  fontWeight: 600,
                  borderRadius: 999,
                  color: '#0A1931',
                  background:
                    'linear-gradient(120deg, rgba(255,255,255,0.92) 0%, rgba(255,224,130,0.85) 100%)',
                  boxShadow: '0 12px 28px rgba(0,0,0,0.25)',
                  textTransform: 'none',
                  '&:hover': {
                    background:
                      'linear-gradient(120deg, rgba(255,255,255,1) 0%, rgba(255,214,64,0.95) 100%)',
                    boxShadow: '0 16px 36px rgba(0,0,0,0.35)',
                  },
                }}
              >
                Cambiar institucion
              </Button>
            </Box>
          )}

          <Box
            sx={{
              bgcolor: 'rgba(0, 51, 102, 0.85)',
              borderRadius: 2,
              p: { xs: 3, md: 4 },
              textAlign: 'center',
              mb: 4,
              boxShadow: '0 18px 40px rgba(0, 18, 58, 0.45)',
            }}
          >
            <Typography component="h1" variant="h5" sx={{ fontWeight: 'bold' }}>
              Elegi a la persona que queres para {institution?.name}
            </Typography>
            <Typography variant="subtitle1" sx={{ mt: 1, opacity: 0.8 }}>
              {institution?.description ||
                'Explora cada perfil y confirma tu voto cuando estes seguro.'}
            </Typography>
            <Box
              sx={{
                mt: 3,
                display: 'flex',
                flexWrap: 'wrap',
                gap: 1.5,
                justifyContent: 'center',
              }}
            >
              {institution?.scope ? (
                <Chip
                  label={institution.scope}
                  sx={{ bgcolor: 'rgba(255,255,255,0.08)', color: '#ffffff' }}
                />
              ) : null}
              {institution?.location ? (
                <Chip
                  label={institution.location}
                  sx={{ bgcolor: 'rgba(255,255,255,0.08)', color: '#ffffff' }}
                />
              ) : null}
              {institution?.electionDate ? (
                <Chip
                  label={`Jornada: ${institution.electionDate}`}
                  sx={{ bgcolor: 'rgba(255,255,255,0.08)', color: '#ffffff' }}
                />
              ) : null}
            </Box>
          </Box>

          {voted && (
            <Box
              sx={{
                textAlign: 'center',
                my: 4,
                p: 3,
                borderRadius: 2,
                bgcolor: 'rgba(76, 175, 80, 0.15)',
                border: '1px solid rgba(76, 175, 80, 0.35)',
                color: '#d8ffe1',
              }}
            >
              <CheckCircleIcon sx={{ fontSize: 42, mb: 1 }} />
              <Typography variant="h6">Tu voto fue registrado exitosamente.</Typography>
              <Typography variant="body1" sx={{ opacity: 0.9 }}>
            Gracias por participar, {userDisplayName}.
              </Typography>
            </Box>
          )}

          <Grid
            container
            spacing={{ xs: 3, md: 4 }}
            justifyContent="center"
            alignItems="stretch"
            sx={{ maxWidth: 1040, mx: 'auto' }}
          >
            {candidates.map((candidate) => (
              <Grid
                item
                key={candidate.id}
                xs={12}
                sm={6}
                md={6}
                sx={{ display: 'flex', justifyContent: 'center' }}
              >
                <Box
                  sx={{
                    width: '100%',
                    maxWidth: 360,
                    flex: '1 1 320px',
                  }}
                >
                  <Card
                    sx={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      width: '100%',
                      aspectRatio: '1 / 1',
                      p: { xs: 3, md: 4 },
                      borderRadius: 3,
                      background:
                        'linear-gradient(180deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.06) 100%)',
                      boxShadow: '0 18px 38px rgba(6, 18, 54, 0.25)',
                      border: '1px solid rgba(255, 255, 255, 0.18)',
                      transition:
                        'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                      '&:hover': !voted
                        ? {
                            transform: 'translateY(-6px)',
                            boxShadow: '0 24px 52px rgba(6, 18, 54, 0.35)',
                          }
                        : {},
                      opacity: voted ? 0.65 : 1,
                      cursor: voted ? 'not-allowed' : 'pointer',
                    }}
                  >
                    <CardActionArea
                      onClick={() => handleOpenConfirmDialog(candidate)}
                      disabled={voted}
                      sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        width: '100%',
                        height: '100%',
                        flexGrow: 1,
                        py: { xs: 4, md: 5 },
                        px: { xs: 1, md: 2 },
                      }}
                    >
                      <Avatar
                        alt={candidate.name}
                        src={candidate.image}
                        sx={{
                          width: 128,
                          height: 128,
                          mb: 3,
                          border: '3px solid #FFD700',
                          boxShadow: '0 12px 24px rgba(0, 0, 0, 0.35)',
                        }}
                      />
                      <CardContent
                        sx={{
                          textAlign: 'center',
                          p: 0,
                          width: '100%',
                          px: { xs: 1, md: 2 },
                        }}
                      >
                        <Typography
                          gutterBottom
                          variant="h6"
                          component="h2"
                          sx={{ fontWeight: 'bold' }}
                        >
                          {candidate.name}
                        </Typography>
                        <Typography
                          variant="body2"
                          sx={{
                            opacity: 0.85,
                            mt: 1.5,
                            color: '#f4f7ff',
                            lineHeight: 1.7,
                          }}
                        >
                          {candidate.proposal}
                        </Typography>
                      </CardContent>
                    </CardActionArea>
                  </Card>
                </Box>
              </Grid>
            ))}
          </Grid>
        </Box>
      </Container>

      <Dialog
        open={openConfirmDialog}
        onClose={handleCloseConfirmDialog}
        aria-labelledby="confirm-vote-dialog-title"
        aria-describedby="confirm-vote-dialog-description"
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle
          id="confirm-vote-dialog-title"
          sx={{
            textAlign: 'center',
            p: 3,
            bgcolor: 'warning.light',
            color: 'warning.dark',
          }}
        >
          <WarningIcon sx={{ fontSize: 60, color: 'warning.main', mb: 1 }} />
          <Typography variant="h5" component="div" sx={{ fontWeight: 'bold' }}>
            Confirmacion de voto
          </Typography>
        </DialogTitle>
        <DialogContent sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="body1" id="confirm-vote-dialog-description">
            Estas seguro de que queres votar por{' '}
            <Typography
              component="span"
              sx={{ fontWeight: 'bold', color: 'primary.main' }}
            >
              {selectedCandidate?.name}
            </Typography>
            ?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Recorda que tu voto es anonimo e inmutable.
          </Typography>
        </DialogContent>
        <DialogActions sx={{ p: 3, justifyContent: 'space-evenly' }}>
          <Button
            onClick={handleCloseConfirmDialog}
            variant="outlined"
            size="large"
            sx={{
              minWidth: '120px',
              borderColor: '#E0E0E0',
              color: '#424242',
              '&:hover': {
                borderColor: '#B0B0B0',
              },
            }}
          >
            Cancelar
          </Button>
          <Button
            onClick={handleConfirmVote}
            variant="contained"
            color="success"
            size="large"
            sx={{ minWidth: '120px' }}
            disabled={voted}
          >
            Confirmar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={openSuccessDialog}
        onClose={handleCloseSuccessDialog}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle
          id="success-dialog-title"
          sx={{
            textAlign: 'center',
            p: 3,
            bgcolor: 'success.light',
            color: 'success.dark',
          }}
        >
          <CheckCircleIcon sx={{ fontSize: 60, color: 'success.main', mb: 1 }} />
          <Typography variant="h5" component="div" sx={{ fontWeight: 'bold' }}>
            Voto registrado
          </Typography>
        </DialogTitle>
        <DialogContent sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="body1">
            Gracias, {userDisplayName}. Tu voto quedo emitido con exito.
          </Typography>
          <Typography variant="body1" sx={{ mt: 2 }}>
            Generamos la constancia de participacion (SBT).
          </Typography>
          <Box
            sx={{
              mt: 3,
              p: 2,
              bgcolor: '#f5f5f5',
              borderRadius: 1,
              overflowWrap: 'break-word',
            }}
          >
            <Typography variant="caption" color="text.secondary">
              Hash de transaccion (voto):
            </Typography>
            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
              {voteHash ?? 'No disponible'}
            </Typography>
            <Link
              href="#"
              onClick={(event) => event.preventDefault()}
              sx={{ display: 'inline-block', mt: 1, fontSize: '0.9rem' }}
            >
              Ver en el explorador
            </Link>
          </Box>
          <Box
            sx={{
              mt: 2,
              p: 2,
              bgcolor: '#f5f5f5',
              borderRadius: 1,
              overflowWrap: 'break-word',
            }}
          >
            <Typography variant="caption" color="text.secondary">
              Hash de SBT:
            </Typography>
            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
              {sbtHash ?? 'No disponible'}
            </Typography>
            <Link
              href="#"
              onClick={(event) => event.preventDefault()}
              sx={{ display: 'inline-block', mt: 1, fontSize: '0.9rem' }}
            >
              Ver constancia en el explorador
            </Link>
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 3, justifyContent: 'center' }}>
          <Button
            onClick={handleCloseSuccessDialog}
            variant="contained"
            color="primary"
            size="large"
          >
            Entendido
          </Button>
        </DialogActions>
      </Dialog>
    </AppBackground>
  )
}

export default VotingPage
