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
  Card,
  CardContent,
  Stack,
  Alert,
  CircularProgress,
  Paper,
  Chip,
  IconButton,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  Save as SaveIcon,
} from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { useApi, SegmentResponse } from '../context/ApiContext';

interface SegmentRule {
  eventType: string;
  attribute: 'count' | 'sum';
  operator: string;
  value: string;
  windowMinutes?: number;
}

interface SegmentDependency {
  independentSegmentId: string;
  logicalOperator: string;
}

const EditSegment: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { apiService } = useApi();
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  
  // Form data
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    type: 'INDEPENDENT' as 'INDEPENDENT' | 'DERIVED',
    active: true,
    windowMinutes: 5,
  });
  
  const [rules, setRules] = useState<SegmentRule[]>([]);
  const [dependencies, setDependencies] = useState<SegmentDependency[]>([]);

  const eventTypes = ['clicks', 'installs', 'orders', 'addToCart'];
  const attributes = ['count', 'sum'];
  const operators = [
    { value: 'GT', label: '>' },
    { value: 'LT', label: '<' },
    { value: 'EQ', label: '=' },
    { value: 'GTE', label: '>=' },
    { value: 'LTE', label: '<=' },
    { value: 'NEQ', label: '!=' },
  ];

  const loadSegment = async () => {
    if (!id) {
      setError('Segment ID is required');
      setLoading(false);
      return;
    }

    try {
      setError(null);
      const segment = await apiService.getSegmentById(id);
      
      // Populate form data
      setFormData({
        name: segment.name,
        description: segment.description || '',
        type: segment.type,
        active: segment.active,
        windowMinutes: segment.windowMinutes || 5,
      });

      // Populate rules for independent segments
      if (segment.rules) {
        setRules(segment.rules.map(rule => ({
          eventType: rule.eventType,
          attribute: rule.attribute,
          operator: rule.operator,
          value: rule.value.toString(),
          windowMinutes: segment.windowMinutes || 5,
        })));
      }

      // Populate dependencies for derived segments
      if (segment.dependencies) {
        setDependencies(segment.dependencies);
      }

    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to load segment');
    } finally {
      setLoading(false);
    }
  };

  const handleAddRule = () => {
            setRules([...rules, { eventType: 'clicks', attribute: 'count', operator: 'GT', value: '', windowMinutes: 5 }]);
  };

  const handleRemoveRule = (index: number) => {
    if (rules.length > 1) {
      setRules(rules.filter((_, i) => i !== index));
    }
  };

  const handleRuleChange = (index: number, field: keyof SegmentRule, value: string | number) => {
    const updatedRules = [...rules];
    updatedRules[index] = { ...updatedRules[index], [field]: value };
    setRules(updatedRules);
  };

  const handleAddDependency = () => {
    setDependencies([...dependencies, { independentSegmentId: '', logicalOperator: 'AND' }]);
  };

  const handleRemoveDependency = (index: number) => {
    setDependencies(dependencies.filter((_, i) => i !== index));
  };

  const handleDependencyChange = (index: number, field: keyof SegmentDependency, value: string) => {
    const updatedDeps = [...dependencies];
    updatedDeps[index] = { ...updatedDeps[index], [field]: value };
    setDependencies(updatedDeps);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(false);

    try {
      const segmentData = {
        ...formData,
        rules: formData.type === 'INDEPENDENT' ? rules : [],
        dependencies: formData.type === 'DERIVED' ? dependencies : [],
      };

      await apiService.updateSegment(id!, segmentData);
      setSuccess(true);
      
      // Redirect to segment details after 2 seconds
      setTimeout(() => {
        navigate(`/segments/${id}`);
      }, 2000);
      
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to update segment');
    } finally {
      setSaving(false);
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

  if (error && !formData.name) {
    return (
      <Box sx={{ p: 3 }}>
        <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton onClick={() => navigate('/segments')} color="primary">
            <ArrowBackIcon />
          </IconButton>
          <Typography variant="h4" component="h1">
            Edit Segment
          </Typography>
        </Box>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  if (success) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="success" sx={{ mb: 2 }}>
          Segment updated successfully! Redirecting to segment details...
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
        <IconButton onClick={() => navigate(`/segments/${id}`)} color="primary">
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" component="h1">
          Edit Segment: {formData.name}
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <form onSubmit={handleSubmit}>
        <Stack spacing={3}>
          {/* Basic Information */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Basic Information
              </Typography>
              <Stack spacing={2}>
                <TextField
                  label="Segment Name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                  fullWidth
                />
                
                <TextField
                  label="Description"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  multiline
                  rows={3}
                  fullWidth
                />

                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                  <FormControl sx={{ minWidth: 200 }}>
                    <InputLabel>Segment Category</InputLabel>
                    <Select
                      value={formData.type}
                      label="Segment Category"
                      onChange={(e) => setFormData({ ...formData, type: e.target.value as 'INDEPENDENT' | 'DERIVED' })}
                      disabled // Usually you don't want to change type after creation
                    >
                      <MenuItem value="INDEPENDENT">Independent</MenuItem>
                      <MenuItem value="DERIVED">Derived</MenuItem>
                    </Select>
                  </FormControl>



                  <TextField
                    label="Window (minutes)"
                    value={formData.windowMinutes}
                    onChange={(e) => setFormData({ ...formData, windowMinutes: parseInt(e.target.value) || 0 })}
                    type="number"
                    sx={{ minWidth: 150 }}
                  />
                </Box>

                <FormControl>
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={formData.active ? 'active' : 'inactive'}
                    label="Status"
                    onChange={(e) => setFormData({ ...formData, active: e.target.value === 'active' })}
                  >
                    <MenuItem value="active">Active</MenuItem>
                    <MenuItem value="inactive">Inactive</MenuItem>
                  </Select>
                </FormControl>
              </Stack>
            </CardContent>
          </Card>

          {/* Rules for Independent Segments */}
          {formData.type === 'INDEPENDENT' && (
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6">
                    Segment Rules
                  </Typography>
                  <Button
                    startIcon={<AddIcon />}
                    onClick={handleAddRule}
                    variant="outlined"
                    size="small"
                  >
                    Add Rule
                  </Button>
                </Box>
                
                <Stack spacing={2}>
                  {rules.map((rule, index) => (
                    <Paper key={index} sx={{ p: 2, bgcolor: 'grey.50' }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                        <Typography variant="subtitle2">
                          Rule {index + 1}
                        </Typography>
                        {rules.length > 1 && (
                          <IconButton
                            size="small"
                            onClick={() => handleRemoveRule(index)}
                            color="error"
                          >
                            <DeleteIcon />
                          </IconButton>
                        )}
                      </Box>
                      
                      <Stack spacing={2}>
                        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                          <FormControl sx={{ minWidth: 120 }}>
                            <InputLabel>Event Type</InputLabel>
                            <Select
                              value={rule.eventType}
                              label="Event Type"
                              onChange={(e) => handleRuleChange(index, 'eventType', e.target.value)}
                            >
                              {eventTypes.map((event) => (
                                <MenuItem key={event} value={event}>
                                  {event}
                                </MenuItem>
                              ))}
                            </Select>
                          </FormControl>

                          <FormControl sx={{ minWidth: 120 }}>
                            <InputLabel>Attribute</InputLabel>
                            <Select
                              value={rule.attribute}
                              label="Attribute"
                              onChange={(e) => handleRuleChange(index, 'attribute', e.target.value)}
                            >
                              {attributes.map((attr) => (
                                <MenuItem key={attr} value={attr}>
                                  {attr}
                                </MenuItem>
                              ))}
                            </Select>
                          </FormControl>

                          <FormControl sx={{ minWidth: 100 }}>
                            <InputLabel>Operator</InputLabel>
                            <Select
                              value={rule.operator}
                              label="Operator"
                              onChange={(e) => handleRuleChange(index, 'operator', e.target.value)}
                            >
                              {operators.map((op) => (
                                <MenuItem key={op.value} value={op.value}>
                                  {op.label}
                                </MenuItem>
                              ))}
                            </Select>
                          </FormControl>

                          <TextField
                            label="Value"
                            value={rule.value}
                            onChange={(e) => handleRuleChange(index, 'value', e.target.value)}
                            type="number"
                            sx={{ minWidth: 100 }}
                          />

                          <TextField
                            label="Window (minutes)"
                            value={rule.windowMinutes || ''}
                            onChange={(e) => handleRuleChange(index, 'windowMinutes', parseInt(e.target.value) || 0)}
                            type="number"
                            sx={{ minWidth: 120 }}
                          />
                        </Box>
                        
                        <Typography variant="caption" color="text.secondary">
                          Example: {rule.eventType} {rule.attribute} {operators.find(op => op.value === rule.operator)?.label} {rule.value} in last {rule.windowMinutes || 0} minutes
                        </Typography>
                      </Stack>
                    </Paper>
                  ))}
                </Stack>
              </CardContent>
            </Card>
          )}

          {/* Dependencies for Derived Segments */}
          {formData.type === 'DERIVED' && (
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6">
                    Segment Dependencies
                  </Typography>
                  <Button
                    startIcon={<AddIcon />}
                    onClick={handleAddDependency}
                    variant="outlined"
                    size="small"
                  >
                    Add Dependency
                  </Button>
                </Box>
                
                {dependencies.length === 0 ? (
                  <Alert severity="info">
                    Add dependencies to combine independent segments with logical operators (AND/OR).
                  </Alert>
                ) : (
                  <Stack spacing={2}>
                    {dependencies.map((dep, index) => (
                      <Paper key={index} sx={{ p: 2, bgcolor: 'grey.50' }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                          <Typography variant="subtitle2">
                            Dependency {index + 1}
                          </Typography>
                          <IconButton
                            size="small"
                            onClick={() => handleRemoveDependency(index)}
                            color="error"
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Box>
                        
                        <Stack spacing={2}>
                          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                            <TextField
                              label="Independent Segment ID"
                              value={dep.independentSegmentId}
                              onChange={(e) => handleDependencyChange(index, 'independentSegmentId', e.target.value)}
                              placeholder="Enter segment ID"
                              sx={{ minWidth: 200 }}
                            />

                            <FormControl sx={{ minWidth: 100 }}>
                              <InputLabel>Operator</InputLabel>
                              <Select
                                value={dep.logicalOperator}
                                label="Operator"
                                onChange={(e) => handleDependencyChange(index, 'logicalOperator', e.target.value)}
                              >
                                <MenuItem value="AND">AND</MenuItem>
                                <MenuItem value="OR">OR</MenuItem>
                              </Select>
                            </FormControl>
                          </Box>
                        </Stack>
                      </Paper>
                    ))}
                  </Stack>
                )}
              </CardContent>
            </Card>
          )}

          {/* Submit Button */}
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Button
              variant="outlined"
              onClick={() => navigate(`/segments/${id}`)}
              disabled={saving}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              startIcon={saving ? <CircularProgress size={20} /> : <SaveIcon />}
              disabled={saving}
            >
              {saving ? 'Saving...' : 'Save Changes'}
            </Button>
          </Box>
        </Stack>
      </form>
    </Box>
  );
};

export default EditSegment;