import { useState } from 'react'
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import Login from './Login'
import InstitutionSelection from './InstitutionSelection'
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
  const [selectedInstitution, setSelectedInstitution] = useState(null)

  const handleLoginSuccess = (userPayload) => {
    setUser(userPayload)
    setSelectedInstitution(null)
  }

  const handleInstitutionSelect = (institution) => {
    setSelectedInstitution(institution)
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {!user ? (
        <Login onLoginSuccess={handleLoginSuccess} />
      ) : !selectedInstitution ? (
        <InstitutionSelection
          user={user}
          onSelectInstitution={handleInstitutionSelect}
        />
      ) : (
        <VotingPage
          user={user}
          institution={selectedInstitution}
          onChangeInstitution={() => setSelectedInstitution(null)}
        />
      )}
    </ThemeProvider>
  )
}

export default App
