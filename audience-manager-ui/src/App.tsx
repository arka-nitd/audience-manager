import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Box } from '@mui/material';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import Dashboard from './pages/Dashboard';
import SimpleSegmentList from './pages/SimpleSegmentList';
import CreateSegment from './pages/CreateSegment';
import SegmentDetails from './pages/SegmentDetails';
import EditSegment from './pages/EditSegment';
// import CreateSegment from './pages/CreateSegment';
// import SegmentDetails from './pages/SegmentDetails';
import { ApiProvider } from './context/ApiContext';

// Create a modern, clean theme
const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
      light: '#42a5f5',
      dark: '#1565c0',
    },
    secondary: {
      main: '#9c27b0',
      light: '#ba68c8',
      dark: '#7b1fa2',
    },
    background: {
      default: '#f5f5f5',
      paper: '#ffffff',
    },
    success: {
      main: '#4caf50',
    },
    error: {
      main: '#f44336',
    },
    warning: {
      main: '#ff9800',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 600,
    },
    h5: {
      fontWeight: 500,
    },
    h6: {
      fontWeight: 500,
    },
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 8,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          borderRadius: 12,
        },
      },
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <ApiProvider>
        <Router>
          <Box sx={{ display: 'flex', minHeight: '100vh' }}>
            <Sidebar />
            <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
              <Header />
              <Box
                component="main"
                sx={{
                  flexGrow: 1,
                  p: 3,
                  backgroundColor: 'background.default',
                }}
              >
                <Routes>
                  <Route path="/" element={<Dashboard />} />
                  <Route path="/segments" element={<SimpleSegmentList />} />
                  <Route path="/segments/create" element={<CreateSegment />} />
                  <Route path="/segments/:id/edit" element={<EditSegment />} />
                  <Route path="/segments/:id" element={<SegmentDetails />} />
                </Routes>
              </Box>
            </Box>
          </Box>
        </Router>
      </ApiProvider>
    </ThemeProvider>
  );
}

export default App;