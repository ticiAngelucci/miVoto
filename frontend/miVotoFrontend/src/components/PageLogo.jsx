import { Avatar, Box } from '@mui/material'
import logoImage from '../assets/voto.jpg'

function PageLogo({ size = 110 }) {
  return (
    <Box
      sx={{
        width: '100%',
        display: 'flex',
        justifyContent: 'center',
        mb: { xs: 3, md: 4 },
      }}
    >
      <Avatar
        variant='rounded'
        sx={{
          width: size,
          height: size,
          borderRadius: 4,
          border: '2px solid rgba(255,255,255,0.4)',
          bgcolor: 'rgba(5, 13, 42, 0.6)',
          boxShadow: '0 25px 55px rgba(0,0,0,0.35)',
          overflow: 'hidden',
        }}
      >
        <img
          src={logoImage}
          alt='MiVoto Logo'
          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
        />
      </Avatar>
    </Box>
  )
}

export default PageLogo
