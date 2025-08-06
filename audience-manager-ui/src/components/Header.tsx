import React from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Box,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Check as HealthIcon,
} from '@mui/icons-material';
import { useApi } from '../context/ApiContext';

const Header: React.FC = () => {
  const { isHealthy, checkHealth } = useApi();

  const handleRefreshHealth = () => {
    checkHealth();
  };

  return (
    <AppBar
      position="static"
      elevation={1}
      sx={{
        backgroundColor: 'white',
        color: 'text.primary',
        borderBottom: '1px solid',
        borderBottomColor: 'divider',
      }}
    >
      <Toolbar>
        <Typography
          variant="h6"
          component="h1"
          sx={{
            flexGrow: 1,
            fontWeight: 600,
            color: 'primary.main',
          }}
        >
          Audience Manager
        </Typography>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {/* API Health Status */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Chip
              icon={<HealthIcon />}
              label={isHealthy ? 'API Connected' : 'API Disconnected'}
              color={isHealthy ? 'success' : 'error'}
              size="small"
              variant="outlined"
            />
            <Tooltip title="Refresh Health Status">
              <IconButton
                size="small"
                onClick={handleRefreshHealth}
                sx={{ color: 'text.secondary' }}
              >
                <RefreshIcon />
              </IconButton>
            </Tooltip>
          </Box>

          {/* Environment Info */}
          <Chip
            label={process.env.NODE_ENV === 'production' ? 'Production' : 'Development'}
            size="small"
            color={process.env.NODE_ENV === 'production' ? 'primary' : 'secondary'}
            variant="outlined"
          />
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;