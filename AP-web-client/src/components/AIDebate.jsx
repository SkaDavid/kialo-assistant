import { useState } from 'react';
import AIArgument from './AIArgument';
import { 
  Box, 
  Typography, 
  Stack, 
  Grid, 
  Paper, 
  Divider, 
  Button 
} from '@mui/material';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import SaveIcon from '@mui/icons-material/Save';

const AIDebatePreview = ({ 
  debateData, 
  fallacyData, 
  onDelete, 
  onFallacyCheck, 
  onAddArgument, 
  setReplyArgId, 
  replyArgId, 
  onGenerateArgument, 
  onSubmitDebate 
}) => {
  
  const [activeId, setActiveId] = useState(null);

  if (!debateData) return null;

  const thesis = debateData.arguments.find(arg => arg.type === "THESIS");
  const currentActiveId = activeId || thesis?.id;

  const getLineage = () => {
    if (!thesis) return [];
    const path = [];
    let current = debateData.arguments.find(arg => arg.id === currentActiveId);
    
    while (current && current.type !== "THESIS") {
      path.unshift(current);
      current = debateData.arguments.find(arg => arg.id === current.parent);
    }
    return path;
  };

  const lineage = getLineage();

  const children = debateData.arguments.filter(arg => arg.parent === currentActiveId);
  const proArguments = children.filter(arg => arg.type === "PRO");
  const conArguments = children.filter(arg => arg.type === "CON");

  const aiArgumentProps = {
    onDelete,
    onFallacyCheck,
    onAddArgument,
    setReplyArgId,
    replyArgId,
    onGenerateArgument
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Typography variant="h5" sx={{ fontWeight: 'bold', mb: 3, textAlign: 'center' }}>
        AI Generated Preview
      </Typography>

      <Box sx={{ mb: 4, display: "flex", flexDirection: "column", alignItems: "center" }}>
        <Stack spacing={1.5} sx={{ width: '100%', alignItems: 'center' }}>
          
          {thesis && (
            <Box 
              onClick={(e) => { if (e.target.tagName !== 'BUTTON') setActiveId(thesis.id); }}
              sx={{ 
                width: '100%',
                p: 0.5,
                borderRadius: 2,
                border: '3px solid',
                borderColor: currentActiveId === thesis.id ? 'primary.main' : 'transparent',
                transition: '0.2s'
              }}
            >
              <AIArgument argument={thesis} {...aiArgumentProps} />
            </Box>
          )}

          {lineage.map((arg) => (
            <Box key={arg.id} sx={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <ChevronRightIcon sx={{ transform: 'rotate(90deg)', color: 'text.secondary', my: 0.5 }} />
              <Box 
                onClick={(e) => { if (e.target.tagName !== 'BUTTON') setActiveId(arg.id); }}
                sx={{ 
                  width: '100%',
                  p: 0.5,
                  borderRadius: 2,
                  border: '3px solid',
                  borderColor: currentActiveId === arg.id ? 'primary.main' : 'transparent',
                  transition: '0.2s'
                }}
              >
                <AIArgument argument={arg} {...aiArgumentProps} />
              </Box>
            </Box>
          ))}
        </Stack>
      </Box>

      <Divider sx={{ my: 4 }} />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper variant="outlined" sx={{ p: 2, bgcolor: '#f4fbf4', borderColor: '#c3e6cb', minHeight: 250, borderRadius: 2 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 'bold', color: '#2e7d32', mb: 2, textAlign: 'center' }}>
              Pro ({proArguments.length})
            </Typography>
            <Stack spacing={2}>
              {proArguments.map(arg => (
                <Box key={arg.id} onClick={(e) => { if (e.target.tagName !== 'BUTTON') setActiveId(arg.id); }}>
                  <AIArgument argument={arg} {...aiArgumentProps} />
                </Box>
              ))}
              {proArguments.length === 0 && (
                <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', fontStyle: 'italic', py: 4 }}>
                  No supporting arguments
                </Typography>
              )}
            </Stack>
          </Paper>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Paper variant="outlined" sx={{ p: 2, bgcolor: '#fff5f5', borderColor: '#fde8e8', minHeight: 250, borderRadius: 2 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 'bold', color: '#d32f2f', mb: 2, textAlign: 'center' }}>
              Con ({conArguments.length})
            </Typography>
            <Stack spacing={2}>
              {conArguments.map(arg => (
                <Box key={arg.id} onClick={(e) => { if (e.target.tagName !== 'BUTTON') setActiveId(arg.id); }}>
                  <AIArgument argument={arg} {...aiArgumentProps} />
                </Box>
              ))}
              {conArguments.length === 0 && (
                <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', fontStyle: 'italic', py: 4 }}>
                  No opposing arguments
                </Typography>
              )}
            </Stack>
          </Paper>
        </Grid>
      </Grid>

      {fallacyData.text !== null && (
        <Paper variant="outlined" sx={{ mt: 4, p: 3, bgcolor: '#fff9c4', borderColor: '#fff59d', borderRadius: 2 }}>
          <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1, color: '#f57f17' }}>
            Argument fallacy test
          </Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}><strong>Text:</strong> {fallacyData.text}</Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}><strong>Label:</strong> {fallacyData.label}</Typography>
          <Typography variant="body2"><strong>Score:</strong> {fallacyData.score}</Typography>
        </Paper>
      )}

      <Box sx={{ mt: 5, display: 'flex', justifyContent: 'center' }}>
        <Button 
          variant="contained" 
          size="large" 
          color="success" 
          startIcon={<SaveIcon />}
          onClick={onSubmitDebate}
          sx={{ px: 4, py: 1.5, fontWeight: 'bold', borderRadius: 2 }}
        >
          Save full debate
        </Button>
      </Box>
    </Box>
  );
};

export default AIDebatePreview;