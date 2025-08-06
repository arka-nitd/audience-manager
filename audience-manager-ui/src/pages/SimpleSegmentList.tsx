import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Alert,
  CircularProgress,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Card,
  CardContent,
  Stack,
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useApi, SegmentResponse, PageResponse } from '../context/ApiContext';

const SimpleSegmentList: React.FC = () => {
  const navigate = useNavigate();
  const { apiService, isHealthy } = useApi();
  
  const [segments, setSegments] = useState<SegmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState<'ALL' | 'INDEPENDENT' | 'DERIVED'>('ALL');
  const [activeFilter, setActiveFilter] = useState<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; segment: SegmentResponse | null }>({
    open: false,
    segment: null,
  });

  const loadSegments = async () => {
    try {
      setLoading(true);
      setError(null);

      let response: PageResponse<SegmentResponse>;

      if (searchQuery.trim()) {
        const searchResults = await apiService.searchSegments(searchQuery);
        response = {
          content: searchResults,
          totalElements: searchResults.length,
          totalPages: 1,
          number: 0,
          size: searchResults.length,
          first: true,
          last: true,
        };
      } else {
        const type = typeFilter === 'ALL' ? undefined : typeFilter;
        const active = activeFilter === 'ALL' ? undefined : activeFilter === 'ACTIVE';
        
        response = await apiService.getSegments(0, 100, type, active);
      }

      setSegments(response.content);
    } catch (err: any) {
      console.error('Failed to load segments:', err);
      setError(err.response?.data?.message || 'Failed to load segments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isHealthy) {
      loadSegments();
    }
  }, [isHealthy, typeFilter, activeFilter]);

  const handleSearch = () => {
    loadSegments();
  };

  const handleToggleStatus = async (segment: SegmentResponse) => {
    try {
      await apiService.updateSegmentStatus(segment.id, !segment.active);
      loadSegments();
    } catch (err: any) {
      console.error('Failed to update segment status:', err);
      setError(err.response?.data?.message || 'Failed to update segment status');
    }
  };

  const handleDeleteSegment = async () => {
    if (!deleteDialog.segment) return;

    try {
      await apiService.deleteSegment(deleteDialog.segment.id);
      setDeleteDialog({ open: false, segment: null });
      loadSegments();
    } catch (err: any) {
      console.error('Failed to delete segment:', err);
      setError(err.response?.data?.message || 'Failed to delete segment');
      setDeleteDialog({ open: false, segment: null });
    }
  };

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
          All Segments
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/segments/create')}
        >
          Create Segment
        </Button>
      </Box>

      {/* Filters */}
      <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField
          placeholder="Search segments..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          sx={{ minWidth: 250 }}
          InputProps={{
            endAdornment: (
              <IconButton size="small" onClick={handleSearch}>
                <SearchIcon />
              </IconButton>
            ),
          }}
        />

        <FormControl size="small" sx={{ minWidth: 140 }}>
          <InputLabel>Type</InputLabel>
          <Select
            value={typeFilter}
            label="Type"
            onChange={(e) => setTypeFilter(e.target.value as any)}
          >
            <MenuItem value="ALL">All Types</MenuItem>
            <MenuItem value="INDEPENDENT">Independent</MenuItem>
            <MenuItem value="DERIVED">Derived</MenuItem>
          </Select>
        </FormControl>

        <FormControl size="small" sx={{ minWidth: 140 }}>
          <InputLabel>Status</InputLabel>
          <Select
            value={activeFilter}
            label="Status"
            onChange={(e) => setActiveFilter(e.target.value as any)}
          >
            <MenuItem value="ALL">All Status</MenuItem>
            <MenuItem value="ACTIVE">Active</MenuItem>
            <MenuItem value="INACTIVE">Inactive</MenuItem>
          </Select>
        </FormControl>

        <Tooltip title="Refresh">
          <IconButton onClick={loadSegments} disabled={loading}>
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Segments List */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Stack spacing={2}>
          {segments.length === 0 ? (
            <Card>
              <CardContent>
                <Typography color="text.secondary" align="center">
                  No segments found. Create your first segment to get started!
                </Typography>
              </CardContent>
            </Card>
          ) : (
            segments.map((segment) => (
              <Card key={segment.id}>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography 
                        variant="h6" 
                        component="h2" 
                        sx={{ cursor: 'pointer', color: 'primary.main' }}
                        onClick={() => navigate(`/segments/${segment.id}`)}
                      >
                        {segment.name}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        {segment.description || 'No description'}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                        <Chip
                          label={segment.type}
                          size="small"
                          color={segment.type === 'INDEPENDENT' ? 'success' : 'secondary'}
                          variant="outlined"
                        />
                        <Chip
                          label={segment.segmentType}
                          size="small"
                          variant="outlined"
                        />
                        <Chip
                          label={segment.active ? 'Active' : 'Inactive'}
                          size="small"
                          color={segment.active ? 'success' : 'default'}
                          variant="filled"
                        />
                        {segment.windowMinutes && (
                          <Chip
                            label={`${segment.windowMinutes} min`}
                            size="small"
                            variant="outlined"
                          />
                        )}
                      </Box>
                    </Box>
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 1 }}>
                      <Typography variant="caption" color="text.secondary">
                        {new Date(segment.createdAt).toLocaleDateString()}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 0.5 }}>
                        <Tooltip title="View Details">
                          <IconButton
                            size="small"
                            onClick={() => navigate(`/segments/${segment.id}`)}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title={segment.active ? 'Deactivate' : 'Activate'}>
                          <IconButton
                            size="small"
                            onClick={() => handleToggleStatus(segment)}
                          >
                            {segment.active ? (
                              <VisibilityOffIcon fontSize="small" />
                            ) : (
                              <VisibilityIcon fontSize="small" />
                            )}
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete">
                          <IconButton
                            size="small"
                            onClick={() => setDeleteDialog({ open: true, segment })}
                            color="error"
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            ))
          )}
        </Stack>
      )}

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialog.open}
        onClose={() => setDeleteDialog({ open: false, segment: null })}
      >
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete the segment "{deleteDialog.segment?.name}"?
            This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialog({ open: false, segment: null })}>
            Cancel
          </Button>
          <Button onClick={handleDeleteSegment} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default SimpleSegmentList;