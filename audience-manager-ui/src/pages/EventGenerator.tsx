import React, { useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
  Alert,
  CircularProgress,
  Stack,
  Chip,
  Divider,
  Card,
  CardContent
} from '@mui/material';
import { Send as SendIcon, PlayArrow as PlayIcon } from '@mui/icons-material';

interface EventData {
  userId: string;
  eventType: string;
  timestamp: number;
  value: number;
  sessionId: string;
  metadata: Record<string, any>;
}

interface GeneratorState {
  topic: string;
  eventCount: number;
  userIdPrefix: string;
  isGenerating: boolean;
  generatedEvents: EventData[];
  message: string;
  messageType: 'success' | 'error' | 'info';
}

const EventGenerator: React.FC = () => {
  const [state, setState] = useState<GeneratorState>({
    topic: 'order_events',
    eventCount: 10,
    userIdPrefix: 'user',
    isGenerating: false,
    generatedEvents: [],
    message: '',
    messageType: 'info'
  });

  const topics = [
    { value: 'order_events', label: 'Order Events (purchase)', eventType: 'purchase' },
    { value: 'activity_events', label: 'Activity Events (view)', eventType: 'view' },
    { value: 'install_events', label: 'Install Events (install)', eventType: 'install' }
  ];

  const generateRealisticData = (eventType: string, index: number): EventData => {
    const userId = `${state.userIdPrefix}_${String(index % 100).padStart(3, '0')}`;
    const sessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const timestamp = Date.now() - Math.floor(Math.random() * 300000); // Random time within last 5 minutes

    let value: number;
    let metadata: Record<string, any>;

    switch (eventType) {
      case 'purchase':
        value = Math.round((Math.random() * 500 + 10) * 100) / 100; // $10-$510
        metadata = {
          productId: `prod_${Math.floor(Math.random() * 1000)}`,
          category: ['electronics', 'clothing', 'books', 'home', 'sports'][Math.floor(Math.random() * 5)],
          currency: 'USD',
          paymentMethod: ['card', 'paypal', 'wallet'][Math.floor(Math.random() * 3)]
        };
        break;
      case 'view':
        value = Math.round((Math.random() * 300 + 5) * 100) / 100; // 5-305 seconds
        metadata = {
          pageUrl: `/product/${Math.floor(Math.random() * 1000)}`,
          referrer: ['search', 'social', 'direct', 'email'][Math.floor(Math.random() * 4)],
          deviceType: ['mobile', 'desktop', 'tablet'][Math.floor(Math.random() * 3)]
        };
        break;
      case 'install':
        value = 1; // Installation is binary
        metadata = {
          appVersion: `1.${Math.floor(Math.random() * 10)}.${Math.floor(Math.random() * 10)}`,
          platform: ['ios', 'android'][Math.floor(Math.random() * 2)],
          source: ['organic', 'paid', 'referral'][Math.floor(Math.random() * 3)]
        };
        break;
      default:
        value = Math.random() * 100;
        metadata = {};
    }

    return {
      userId,
      eventType,
      timestamp,
      value,
      sessionId,
      metadata
    };
  };

  const generateEvents = async () => {
    setState(prev => ({ ...prev, isGenerating: true, message: '', generatedEvents: [] }));

    try {
      const selectedTopic = topics.find(t => t.value === state.topic);
      if (!selectedTopic) {
        throw new Error('Invalid topic selected');
      }

      const events: EventData[] = [];
      for (let i = 0; i < state.eventCount; i++) {
        const event = generateRealisticData(selectedTopic.eventType, i);
        events.push(event);
      }

      // Simulate API call to send events to Kafka
      await sendEventsToKafka(state.topic, events);

      setState(prev => ({
        ...prev,
        isGenerating: false,
        generatedEvents: events,
        message: `Successfully generated and sent ${events.length} events to topic: ${state.topic}`,
        messageType: 'success'
      }));

    } catch (error) {
      setState(prev => ({
        ...prev,
        isGenerating: false,
        message: `Failed to generate events: ${error instanceof Error ? error.message : 'Unknown error'}`,
        messageType: 'error'
      }));
    }
  };

  const sendEventsToKafka = async (topic: string, events: EventData[]): Promise<void> => {
    // For demo purposes, we'll create a mock API endpoint
    // In reality, this would send events to your event ingestion service
    
    const response = await fetch('/api/v1/events/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        topic,
        events
      })
    });

    if (!response.ok) {
      // For demo, we'll simulate success after a delay
      await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 2000));
      console.log(`Mock: Sent ${events.length} events to topic ${topic}`);
      return;
    }

    const result = await response.json();
    console.log('Events sent successfully:', result);
  };

  const clearEvents = () => {
    setState(prev => ({ 
      ...prev, 
      generatedEvents: [], 
      message: '',
      messageType: 'info'
    }));
  };

  return (
    <Box sx={{ p: 3, maxWidth: 1200, mx: 'auto' }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Event Generator
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Generate realistic test events for Kafka topics to test the segment processing pipeline.
      </Typography>

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={3}>
        {/* Generator Configuration */}
        <Box sx={{ flex: 1 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Event Configuration
            </Typography>

            <Stack spacing={3}>
              <FormControl fullWidth>
                <InputLabel>Topic</InputLabel>
                <Select
                  value={state.topic}
                  label="Topic"
                  onChange={(e) => setState(prev => ({ ...prev, topic: e.target.value }))}
                >
                  {topics.map((topic) => (
                    <MenuItem key={topic.value} value={topic.value}>
                      {topic.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <TextField
                label="Number of Events"
                type="number"
                value={state.eventCount}
                onChange={(e) => setState(prev => ({ 
                  ...prev, 
                  eventCount: Math.max(1, Math.min(1000, parseInt(e.target.value) || 1))
                }))}
                inputProps={{ min: 1, max: 1000 }}
                fullWidth
              />

              <TextField
                label="User ID Prefix"
                value={state.userIdPrefix}
                onChange={(e) => setState(prev => ({ 
                  ...prev, 
                  userIdPrefix: e.target.value.replace(/[^a-zA-Z0-9_]/g, '')
                }))}
                placeholder="user"
                helperText="Will generate user IDs like: user_001, user_002, etc."
                fullWidth
              />

              <Button
                variant="contained"
                size="large"
                startIcon={state.isGenerating ? <CircularProgress size={20} /> : <PlayIcon />}
                onClick={generateEvents}
                disabled={state.isGenerating}
                fullWidth
              >
                {state.isGenerating ? 'Generating Events...' : 'Generate Events'}
              </Button>

              {state.message && (
                <Alert severity={state.messageType}>
                  {state.message}
                </Alert>
              )}
            </Stack>
          </Paper>
        </Box>

        {/* Event Preview */}
        <Box sx={{ flex: 1 }}>
          <Paper sx={{ p: 3 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
              <Typography variant="h6">
                Generated Events ({state.generatedEvents.length})
              </Typography>
              {state.generatedEvents.length > 0 && (
                <Button variant="outlined" size="small" onClick={clearEvents}>
                  Clear
                </Button>
              )}
            </Stack>

            {state.generatedEvents.length === 0 ? (
              <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
                No events generated yet. Configure the settings above and click "Generate Events" to start.
              </Typography>
            ) : (
              <Box sx={{ maxHeight: 400, overflow: 'auto' }}>
                <Stack spacing={1}>
                  {state.generatedEvents.slice(0, 5).map((event, index) => (
                    <Card key={index} variant="outlined" sx={{ fontSize: '0.875rem' }}>
                      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                        <Stack direction="row" spacing={1} sx={{ mb: 1 }}>
                          <Chip label={event.eventType} size="small" color="primary" />
                          <Chip label={event.userId} size="small" variant="outlined" />
                        </Stack>
                        <Typography variant="body2" color="text.secondary">
                          Value: {event.value} | Time: {new Date(event.timestamp).toLocaleTimeString()}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Metadata: {Object.keys(event.metadata).join(', ')}
                        </Typography>
                      </CardContent>
                    </Card>
                  ))}
                  {state.generatedEvents.length > 5 && (
                    <Typography variant="caption" color="text.secondary" sx={{ textAlign: 'center', py: 1 }}>
                      ... and {state.generatedEvents.length - 5} more events
                    </Typography>
                  )}
                </Stack>
              </Box>
            )}
          </Paper>
        </Box>
      </Stack>

      {/* Info Section */}
      <Paper sx={{ p: 3, mt: 3, bgcolor: 'background.default' }}>
        <Typography variant="h6" gutterBottom>
          How it works
        </Typography>
        <Typography variant="body2" color="text.secondary" paragraph>
          This event generator creates realistic test data for different event types:
        </Typography>
        <Stack spacing={1}>
          <Box>
            <Typography variant="subtitle2" color="primary">Order Events (purchase)</Typography>
            <Typography variant="body2" color="text.secondary">
              Simulates purchase events with order values, product IDs, categories, and payment methods.
            </Typography>
          </Box>
          <Box>
            <Typography variant="subtitle2" color="primary">Activity Events (view)</Typography>
            <Typography variant="body2" color="text.secondary">
              Simulates page/content view events with view duration, URLs, referrers, and device types.
            </Typography>
          </Box>
          <Box>
            <Typography variant="subtitle2" color="primary">Install Events (install)</Typography>
            <Typography variant="body2" color="text.secondary">
              Simulates app installation events with version info, platform, and acquisition source.
            </Typography>
          </Box>
        </Stack>
      </Paper>
    </Box>
  );
};

export default EventGenerator;