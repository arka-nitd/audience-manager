import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Alert,
  CircularProgress,
  Chip,
  IconButton,
  Tooltip,
  Stack,
} from '@mui/material';
import {
  Add as AddIcon,
  Group as GroupIcon,
  TrendingUp as TrendingUpIcon,
  Visibility as VisibilityIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useApi, SegmentResponse } from '../context/ApiContext';

interface DashboardStats {
  totalSegments: number;
  independentSegments: number;
  derivedSegments: number;
  activeSegments: number;
}

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const { apiService, isHealthy } = useApi();
  const [stats, setStats] = useState<DashboardStats>({
    totalSegments: 0,
    independentSegments: 0,
    derivedSegments: 0,
    activeSegments: 0,
  });
  const [recentSegments, setRecentSegments] = useState<SegmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Load recent segments
      const segmentsResponse = await apiService.getSegments(0, 5);
      setRecentSegments(segmentsResponse.content);

      // Calculate stats
      const allSegments = await apiService.getSegments(0, 1000); // Get a large page to count all
      const segments = allSegments.content;

      const stats: DashboardStats = {
        totalSegments: segments.length,
        independentSegments: segments.filter(s => s.type === 'INDEPENDENT').length,
        derivedSegments: segments.filter(s => s.type === 'DERIVED').length,
        activeSegments: segments.filter(s => s.active).length,
      };

      setStats(stats);
    } catch (err: any) {
      console.error('Failed to load dashboard data:', err);
      setError(err.response?.data?.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isHealthy) {
      loadDashboardData();
    }
  }, [isHealthy]);

  const StatCard: React.FC<{
    title: string;
    value: number;
    icon: React.ReactElement;
    color: 'primary' | 'secondary' | 'success' | 'warning';
  }> = ({ title, value, icon, color }) => (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box>
            <Typography color="text.secondary" gutterBottom variant="h6">
              {title}
            </Typography>
            <Typography variant="h4" component="div" color={`${color}.main`}>
              {value.toLocaleString()}
            </Typography>
          </Box>
          <Box sx={{ color: `${color}.main`, fontSize: '2.5rem' }}>
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  if (!isHealthy) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 3 }}>
          API is not accessible. Please check your connection and try again.
        </Alert>
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1" fontWeight="bold">
          Dashboard
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title="Refresh Data">
            <IconButton onClick={loadDashboardData} disabled={loading}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/segments/create')}
          >
            Create Segment
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          {/* Stats Cards */}
          <Box sx={{ 
            display: 'grid', 
            gridTemplateColumns: { 
              xs: '1fr', 
              sm: '1fr 1fr', 
              md: '1fr 1fr 1fr 1fr' 
            }, 
            gap: 3, 
            mb: 4 
          }}>
            <StatCard
              title="Total Segments"
              value={stats.totalSegments}
              icon={<GroupIcon />}
              color="primary"
            />
            <StatCard
              title="Independent"
              value={stats.independentSegments}
              icon={<TrendingUpIcon />}
              color="success"
            />
            <StatCard
              title="Derived"
              value={stats.derivedSegments}
              icon={<TrendingUpIcon />}
              color="secondary"
            />
            <StatCard
              title="Active"
              value={stats.activeSegments}
              icon={<VisibilityIcon />}
              color="warning"
            />
          </Box>

          {/* Recent Segments */}
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" component="h2">
                  Recent Segments
                </Typography>
                <Button
                  variant="outlined"
                  size="small"
                  onClick={() => navigate('/segments')}
                >
                  View All
                </Button>
              </Box>

              {recentSegments.length === 0 ? (
                <Typography color="text.secondary" sx={{ py: 2 }}>
                  No segments created yet. Create your first segment to get started!
                </Typography>
              ) : (
                <Stack spacing={2}>
                  {recentSegments.map((segment) => (
                    <Box
                      key={segment.id}
                      sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        p: 2,
                        border: '1px solid',
                        borderColor: 'divider',
                        borderRadius: 2,
                        cursor: 'pointer',
                        '&:hover': {
                          backgroundColor: 'action.hover',
                        },
                      }}
                      onClick={() => navigate(`/segments/${segment.id}`)}
                    >
                      <Box>
                        <Typography variant="subtitle1" fontWeight="medium">
                          {segment.name}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {segment.description || 'No description'}
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
                          <Chip
                            label={segment.type}
                            size="small"
                            color={segment.type === 'INDEPENDENT' ? 'success' : 'secondary'}
                            variant="outlined"
                          />
                          <Chip
                            label={segment.type}
                            size="small"
                            variant="outlined"
                          />
                          {segment.active && (
                            <Chip
                              label="Active"
                              size="small"
                              color="success"
                              variant="filled"
                            />
                          )}
                        </Box>
                      </Box>
                      <Typography variant="caption" color="text.secondary">
                        {new Date(segment.createdAt).toLocaleDateString()}
                      </Typography>
                    </Box>
                  ))}
                </Stack>
              )}
            </CardContent>
          </Card>
        </>
      )}
    </Box>
  );
};

export default Dashboard;