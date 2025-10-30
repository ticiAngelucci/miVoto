import { useState } from 'react'
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import Login from './Login'
import VotingPage from './VotingPage'

const theme = createTheme({
  palette: {
    primary: {
      main: '#003366',
      dark: '#002244',
    },
    secondary: {
      main: '#FFD700',
    },
    success: {
      main: '#4CAF50',
      contrastText: '#ffffff',
    },
    warning: {
      main: '#FFC107',
      light: '#FFF8E1',
      dark: '#FFA000',
    },
  },
  typography: {
    fontFamily: 'Roboto, Arial, sans-serif',
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          border: '1px solid rgba(0, 0, 0, 0.12)',
        },
      },
    },
  },
})

function App() {
  const [user, setUser] = useState(null)

  const handleLoginSuccess = (username) => {
    setUser(username)
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {!user ? (
        <Login onLoginSuccess={handleLoginSuccess} />
      ) : (
        <VotingPage username={user} />
      )}
    </ThemeProvider>
  )
}

export default App
