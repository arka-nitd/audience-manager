import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  Card,
  CardContent,
  Stack,
  Alert,
  CircularProgress,
  IconButton,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Divider,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  PlayArrow as PlayArrowIcon,
  Pause as PauseIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { useApi, SegmentResponse } from '../context/ApiContext';

const SegmentDetails: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { apiService } = useApi();
  
  const [segment, setSegment] = useState<SegmentResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [updating, setUpdating] = useState(false);

  const loadSegment = async () => {
    if (!id) {
      setError('Segment ID is required');
      setLoading(false);
      return;
    }

    try {
      setError(null);
      const segmentData = await apiService.getSegmentById(id);
      setSegment(segmentData);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to load segment');
    } finally {
      setLoading(false);
    }
  };

  const toggleSegmentStatus = async () => {
    if (!segment) return;
    
    setUpdating(true);
    try {
      // For now, we'll just toggle the local state since we don't have an update endpoint
      // In a real implementation, you'd call apiService.updateSegment(segment.id, { active: !segment.active })
      setSegment({ ...segment, active: !segment.active });
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to update segment');
    } finally {
      setUpdating(false);
    }
  };

  const deleteSegment = async () => {
    if (!segment) return;
    
    if (!window.confirm(`Are you sure you want to delete segment "${segment.name}"? This action cannot be undone.`)) {
      return;
    }
    
    setUpdating(true);
    try {
      await apiService.deleteSegment(segment.id);
      navigate('/segments');
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to delete segment');
      setUpdating(false);
    }
  };

  useEffect(() => {
    loadSegment();
  }, [id]);

  if (loading) {
    return (
      <Box sx={{ p: 3, display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error && !segment) {
    return (
      <Box sx={{ p: 3 }}>
        <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton onClick={() => navigate('/segments')} color="primary">
            <ArrowBackIcon />
          </IconButton>
          <Typography variant="h4" component="h1">
            Segment Details
          </Typography>
        </Box>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  if (!segment) {
    return (
      <Box sx={{ p: 3 }}>
        <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton onClick={() => navigate('/segments')} color="primary">
            <ArrowBackIcon />
          </IconButton>
          <Typography variant="h4" component="h1">
            Segment Details
          </Typography>
        </Box>
        <Alert severity="warning">Segment not found</Alert>
      </Box>
    );
  }

  const operatorSymbols: { [key: string]: string } = {
    GT: '>',
    LT: '<',
    EQ: '=',
    GTE: '>=',
    LTE: '<=',
    NEQ: '!='
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton onClick={() => navigate('/segments')} color="primary">
            <ArrowBackIcon />
          </IconButton>
          <Typography variant="h4" component="h1">
            {segment.name}
          </Typography>
          <Chip
            label={segment.active ? 'Active' : 'Inactive'}
            color={segment.active ? 'success' : 'default'}
            variant="filled"
          />
        </Box>
        
        <Box sx={{ display: 'flex', gap: 1 }}>
          <IconButton
            onClick={() => loadSegment()}
            color="primary"
            disabled={loading}
            title="Refresh"
          >
            <RefreshIcon />
          </IconButton>
          <IconButton
            onClick={toggleSegmentStatus}
            color={segment.active ? 'warning' : 'success'}
            disabled={updating}
            title={segment.active ? 'Deactivate' : 'Activate'}
          >
            {segment.active ? <PauseIcon /> : <PlayArrowIcon />}
          </IconButton>
          <IconButton
            onClick={() => navigate(`/segments/${segment.id}/edit`)}
            color="primary"
            title="Edit"
          >
            <EditIcon />
          </IconButton>
          <IconButton
            onClick={deleteSegment}
            color="error"
            disabled={updating}
            title="Delete"
          >
            <DeleteIcon />
          </IconButton>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Stack spacing={3}>
        {/* Basic Information */}
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Basic Information
            </Typography>
            <Stack spacing={2}>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
                <Box sx={{ minWidth: 200 }}>
                  <Typography variant="subtitle2" color="text.secondary">
                    ID
                  </Typography>
                  <Typography variant="body1" sx={{ fontFamily: 'monospace', wordBreak: 'break-all' }}>
                    {segment.id}
                  </Typography>
                </Box>
                <Box sx={{ minWidth: 150 }}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Type
                  </Typography>
                  <Box sx={{ mt: 0.5 }}>
                    <Chip
                      label={segment.type}
                      size="small"
                      color={segment.type === 'INDEPENDENT' ? 'success' : 'secondary'}
                      variant="outlined"
                    />
                  </Box>
                </Box>
                <Box sx={{ minWidth: 150 }}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Segment Type
                  </Typography>
                  <Box sx={{ mt: 0.5 }}>
                    <Chip
                                                label={segment.type}
                      size="small"
                      variant="outlined"
                    />
                  </Box>
                </Box>
                <Box sx={{ minWidth: 150 }}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Window (minutes)
                  </Typography>
                  <Typography variant="body1">
                    {segment.windowMinutes || 'N/A'}
                  </Typography>
                </Box>
              </Box>
              
              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  Description
                </Typography>
                <Typography variant="body1">
                  {segment.description || 'No description provided'}
                </Typography>
              </Box>
              
              {segment.logicalExpression && (
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">
                    Logical Expression
                  </Typography>
                  <Typography variant="body1" sx={{ fontFamily: 'monospace', bgcolor: 'grey.100', p: 1, borderRadius: 1 }}>
                    {segment.logicalExpression}
                  </Typography>
                </Box>
              )}
            </Stack>
          </CardContent>
        </Card>

        {/* Rules (for Independent Segments) */}
        {segment.type === 'INDEPENDENT' && segment.rules && segment.rules.length > 0 && (
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Segment Rules
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Event Type</TableCell>
                      <TableCell>Attribute</TableCell>
                      <TableCell>Operator</TableCell>
                      <TableCell>Value</TableCell>
                      <TableCell>Window (min)</TableCell>
                      <TableCell>Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {segment.rules.map((rule, index) => (
                      <TableRow key={index}>
                        <TableCell>
                          <Chip label={rule.eventType} size="small" variant="outlined" />
                        </TableCell>
                        <TableCell>{rule.attribute}</TableCell>
                        <TableCell>
                          <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                            {operatorSymbols[rule.operator] || rule.operator}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                            {rule.value}
                          </Typography>
                        </TableCell>
                        <TableCell>{segment.windowMinutes || 'N/A'}</TableCell>
                        <TableCell>
                          <Chip
                            label="Active"
                            size="small"
                            color="success"
                            variant="filled"
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
              
              {/* Rule Summary */}
              <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  Rule Logic Summary:
                </Typography>
                <Typography variant="body2">
                  {segment.rules.map((rule, index) => (
                    <span key={index}>
                      {index > 0 && ' AND '}
                      <strong>{rule.eventType}</strong> {rule.attribute} {operatorSymbols[rule.operator] || rule.operator} <strong>{rule.value}</strong>
                      {segment.windowMinutes && ` in last ${segment.windowMinutes} minutes`}
                    </span>
                  ))}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        )}

        {/* Dependencies (for Derived Segments) */}
        {segment.type === 'DERIVED' && segment.dependencies && segment.dependencies.length > 0 && (
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Segment Dependencies
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Independent Segment ID</TableCell>
                      <TableCell>Logical Operator</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {segment.dependencies.map((dep, index) => (
                      <TableRow key={index}>
                        <TableCell>
                          <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                            {dep.independentSegmentId}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={dep.logicalOperator}
                            size="small"
                            color={dep.logicalOperator === 'AND' ? 'primary' : 'secondary'}
                            variant="outlined"
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              {/* Dependency Summary */}
              <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  Dependency Logic Summary:
                </Typography>
                <Typography variant="body2">
                  {segment.dependencies.map((dep, index) => (
                    <span key={index}>
                      {index > 0 && ` ${dep.logicalOperator} `}
                      Segment <strong>{dep.independentSegmentId}</strong>
                    </span>
                  ))}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        )}

        {/* Timestamps */}
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Timestamps
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
              <Box sx={{ minWidth: 200 }}>
                <Typography variant="subtitle2" color="text.secondary">
                  Created At
                </Typography>
                <Typography variant="body1">
                  {segment.createdAt ? new Date(segment.createdAt).toLocaleString() : 'N/A'}
                </Typography>
              </Box>
              <Box sx={{ minWidth: 200 }}>
                <Typography variant="subtitle2" color="text.secondary">
                  Updated At
                </Typography>
                <Typography variant="body1">
                  {segment.updatedAt ? new Date(segment.updatedAt).toLocaleString() : 'N/A'}
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Stack>
    </Box>
  );
};

export default SegmentDetails;