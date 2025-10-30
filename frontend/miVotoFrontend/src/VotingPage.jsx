import { useEffect, useMemo, useState } from 'react'
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
} from '@mui/material'
import WarningIcon from '@mui/icons-material/Warning'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import { getCandidates, submitVote } from './api'
import logoImage from './assets/voto.jpg'
import './App.css'

const FooterLogo = () => (
  <Box
    sx={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      mt: 6,
      mb: { xs: 0, md: 2 },
      color: '#ffffff',
    }}
  >
    <Box
      sx={{
        position: 'relative',
        width: '100%',
        maxWidth: 340,
        aspectRatio: '2 / 1',
        borderBottomLeftRadius: 200,
        borderBottomRightRadius: 200,
        background:
          'radial-gradient(circle at 50% 0%, rgba(255,255,255,0.22) 0%, rgba(0,35,102,0.78) 35%, rgba(0,35,102,0.95) 100%)',
        boxShadow: '0 26px 60px rgba(4, 20, 62, 0.45)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        overflow: 'hidden',
      }}
    >
      <Avatar
        sx={{ width: 70, height: 70, bgcolor: 'transparent' }}
        variant="circular"
      >
        <img
          src={logoImage}
          alt="MiVoto Logo"
          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
        />
      </Avatar>
    </Box>
    <Typography
      variant="subtitle1"
      sx={{ fontWeight: 'bold', letterSpacing: '0.28em', mt: 2 }}
    >
      MIVOTO
    </Typography>
  </Box>
)

const useBackgroundLayers = () =>
  useMemo(
    () => (
      <>
        <div className="background-gradients">
          <div className="gradient gradient--one" />
          <div className="gradient gradient--two" />
          <div className="gradient gradient--three" />
        </div>
        <div className="login-bottom-arc" />
      </>
    ),
    []
  )

function VotingPage({ username }) {
  const [candidates, setCandidates] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [openConfirmDialog, setOpenConfirmDialog] = useState(false)
  const [selectedCandidate, setSelectedCandidate] = useState(null)
  const [voted, setVoted] = useState(false)
  const [openSuccessDialog, setOpenSuccessDialog] = useState(false)
  const [voteHash, setVoteHash] = useState(null)
  const [sbtHash, setSbtHash] = useState(null)

  const backgroundLayers = useBackgroundLayers()

  useEffect(() => {
    const fetchCandidates = async () => {
      try {
        const data = await getCandidates()
        setCandidates(data)
      } catch (err) {
        console.error('Error fetching candidates:', err)
        setError('Error al cargar los candidatos. Volvé a intentar en unos minutos.')
      } finally {
        setLoading(false)
      }
    }

    fetchCandidates()
  }, [])

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
      const response = await submitVote(username, selectedCandidate.id)
      setVoteHash(response.voteTxHash)
      setSbtHash(response.sbtTxHash)
      setVoted(true)
      handleCloseConfirmDialog()
      setOpenSuccessDialog(true)
    } catch (err) {
      console.error('Error submitting vote:', err)
      alert('Hubo un problema al registrar tu voto. Intentá nuevamente.')
      handleCloseConfirmDialog()
    }
  }

  const renderWithBackground = (content) => (
    <div className="app-background">
      {backgroundLayers}
      {content}
    </div>
  )

  if (loading) {
    return renderWithBackground(
      <Box sx={{ mt: 10, textAlign: 'center', color: '#ffffff' }}>
        <Typography variant="h5">Cargando candidatos…</Typography>
      </Box>
    )
  }

  if (error) {
    return renderWithBackground(
      <Box sx={{ mt: 10, textAlign: 'center', color: '#ffe0e0' }}>
        <Typography variant="h6">{error}</Typography>
      </Box>
    )
  }

  return renderWithBackground(
    <>
      <Container
        component="main"
        maxWidth="md"
        sx={{
          position: 'relative',
          zIndex: 1,
          color: '#ffffff',
        }}
      >
        <Box
          sx={{
            marginTop: { xs: 2, md: 4 },
            padding: { xs: 3, md: 4 },
            borderRadius: 3,
            bgcolor: 'rgba(12, 22, 56, 0.55)',
            boxShadow: '0 24px 60px rgba(9, 18, 54, 0.35)',
            backdropFilter: 'blur(18px)',
          }}
        >
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
              Elegí a la persona que querés que represente a nuestra comunidad.
            </Typography>
            <Typography variant="subtitle1" sx={{ mt: 1, opacity: 0.8 }}>
              Explorá cada perfil y confirmá tu voto cuando estés seguro.
            </Typography>
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
              <Typography variant="h6">¡Tu voto fue registrado exitosamente!</Typography>
              <Typography variant="body1" sx={{ opacity: 0.9 }}>
                Gracias por participar, {username}.
              </Typography>
            </Box>
          )}

          <Grid container spacing={3} justifyContent="center">
            {candidates.map((candidate) => (
              <Grid item key={candidate.id} xs={12} sm={6}>
                <Card
                  sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    p: 2,
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
                      py: 3,
                    }}
                  >
                    <Avatar
                      alt={candidate.name}
                      src={candidate.image}
                      sx={{
                        width: 108,
                        height: 108,
                        mb: 2,
                        border: '3px solid #FFD700',
                        boxShadow: '0 12px 24px rgba(0, 0, 0, 0.35)',
                      }}
                    />
                    <CardContent sx={{ textAlign: 'center', p: 0 }}>
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
                        sx={{ opacity: 0.8, mt: 1, color: '#f4f7ff' }}
                      >
                        {candidate.proposal}
                      </Typography>
                    </CardContent>
                  </CardActionArea>
                </Card>
              </Grid>
            ))}
          </Grid>

          <FooterLogo />
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
            Confirmación de voto
          </Typography>
        </DialogTitle>
        <DialogContent sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="body1" id="confirm-vote-dialog-description">
            ¿Estás seguro de que querés votar por{' '}
            <Typography
              component="span"
              sx={{ fontWeight: 'bold', color: 'primary.main' }}
            >
              {selectedCandidate?.name}
            </Typography>
            ?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Recordá que tu voto es anónimo e inmutable.
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
            ¡Voto registrado!
          </Typography>
        </DialogTitle>
        <DialogContent sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="body1">
            Gracias, {username}. Tu voto quedó emitido con éxito.
          </Typography>
          <Typography variant="body1" sx={{ mt: 2 }}>
            Generamos la constancia de participación (SBT).
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
              Hash de transacción (voto):
            </Typography>
            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
              {voteHash}
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
              {sbtHash}
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
    </>
  )
}

export default VotingPage
