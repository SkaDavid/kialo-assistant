import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import keycloak from '../config/keycloak';
import { api } from '../api.js';
import Argument from '../components/Argument';
import { 
  Container, 
  Typography, 
  Box, 
  Stack, 
  Grid, 
  Paper, 
  Button, 
  Divider 
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';

const DebateDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [debate, setDebate] = useState(null);
  
  const [activeId, setActiveId] = useState(null);
  
  const [replyArgId, setReplyArgId] = useState(null);
  const [updateArgId, setUpdateArgId] = useState(null);
  const [argumentFallacy, setArgumentFallacy] = useState({ text: null, label: null, score: null, explanation: null });

  const fetchData = async () => {
    const data = await api.getArgument(id);
    setDebate(data);
    console.log(data);
  };

  useEffect(() => { 
    fetchData(); 
  }, [id]);

  if (!debate) return <Typography sx={{ p: 4, textAlign: 'center' }}>Loading</Typography>;

  const thesis = debate.arguments.find(arg => arg.type === "THESIS");
  
  const currentActiveId = activeId || thesis?.id;

  const getLineage = () => {
    if (!thesis) return [];
    const path = [];
    let current = debate.arguments.find(arg => arg.id === currentActiveId);
    
    while (current && current.type !== "THESIS") {
      path.unshift(current);
      current = debate.arguments.find(arg => arg.id === current.parent);
    }
    return path;
  };

  const lineage = getLineage();

  const children = debate.arguments.filter(arg => arg.parent === currentActiveId);
  const proArguments = children.filter(arg => arg.type === "PRO");
  const conArguments = children.filter(arg => arg.type === "CON");

  const handleArgumentClick = (clicked) => {
    setActiveId(clicked.id);
  };

  const argumentHandlers = {
    onArgumentClick: handleArgumentClick,
    onOpenReply: (e, id) => { e.stopPropagation(); setReplyArgId(id); setUpdateArgId(null); },
    onOpenUpdate: (e, id) => { e.stopPropagation(); setUpdateArgId(id); setReplyArgId(null); },
    onDelete: async (e, id) => { e.stopPropagation(); await api.deleteArgument(id); fetchData(); },
    onSubmitReply: async (parentId, data) => { 
      await api.createArgument({ debateId: id, parentId: parentId, ...data });
      setReplyArgId(null); 
      fetchData(); 
    },
    onSubmitUpdate: async (id, data) => {
      await api.updateArgument(id, data);
      setUpdateArgId(null); 
      fetchData();
    },
    setReplyArgId: setReplyArgId, 
    setUpdateArgId: setUpdateArgId,
    onFallacyTest: async (e, text, argumentId) => {
      e.stopPropagation();
      const data = await api.testFallacy({ text: text, argumentId: argumentId });
      if(data){
        setArgumentFallacy({
          text: text,
          label: data.label,
          score: data.score,
          explanation: data.explanation
        });
      }
      fetchData();
    },
    onDeleteFallacy: async (e, argumentId) => {
    e.stopPropagation(); 
    await api.deleteFallacy(argumentId);
    fetchData();
  },
    onSyncArgument: async (id) => {
      await api.syncArgument(id);
      fetchData();
    }
  };

  const activePathIds = thesis ? [thesis.id, ...lineage.map(a => a.id)] : [];

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      
      <Box sx={{ mb: 3, display: "flex", justifyContent: "flex-start" }}>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/')}>
          Back to main menu
        </Button>
      </Box>

      <Typography variant="h3" component="h1" sx={{ fontWeight: 'bold', mb: 5, textAlign: 'center' }}>
        {debate.title}
      </Typography>

      <Box sx={{ mb: 5, display: "flex", flexDirection: "column", alignItems: "center" }}>        
        <Stack spacing={1.5} sx={{ width: '100%', maxWidth: 850, alignItems: 'center' }}>
          {thesis && (
            <Argument 
              arg={thesis} 
              activePath={activePathIds} 
              currentUser={keycloak.tokenParsed?.preferred_username}
              currentAction={{ replyArgId, updateArgId }}
              handlers={argumentHandlers}
            />
          )}

          {lineage.map((parentArg) => (
            <Box key={parentArg.id} sx={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <ChevronRightIcon sx={{ transform: 'rotate(90deg)', color: 'text.secondary', my: 0.5 }} />
              <Argument 
                arg={parentArg} 
                activePath={activePathIds} 
                currentUser={keycloak.tokenParsed?.preferred_username}
                currentAction={{ replyArgId, updateArgId }}
                handlers={argumentHandlers}
              />
            </Box>
          ))}
        </Stack>
      </Box>

      <Divider sx={{ my: 4 }} />

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper variant="outlined" sx={{ p: 2, bgcolor: '#f4fbf4', borderColor: '#c3e6cb', minHeight: 300, borderRadius: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2e7d32', mb: 2, textAlign: 'center' }}>
              Pro ({proArguments.length})
            </Typography>
            <Stack spacing={2}>
              {proArguments.map(child => (
                <Argument 
                  key={child.id}
                  arg={child}
                  activePath={activePathIds}
                  currentUser={keycloak.tokenParsed?.preferred_username}
                  currentAction={{ replyArgId, updateArgId }}
                  handlers={argumentHandlers}
                />
              ))}
              {proArguments.length === 0 && (
                <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', fontStyle: 'italic', py: 5 }}>
                  Žádné podpůrné argumenty. Buď první, kdo zareaguje!
                </Typography>
              )}
            </Stack>
          </Paper>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Paper variant="outlined" sx={{ p: 2, bgcolor: '#fff5f5', borderColor: '#fde8e8', minHeight: 300, borderRadius: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#d32f2f', mb: 2, textAlign: 'center' }}>
              Proti ({conArguments.length})
            </Typography>
            <Stack spacing={2}>
              {conArguments.map(child => (
                <Argument 
                  key={child.id}
                  arg={child}
                  activePath={activePathIds}
                  currentUser={keycloak.tokenParsed?.preferred_username}
                  currentAction={{ replyArgId, updateArgId }}
                  handlers={argumentHandlers}
                />
              ))}
            </Stack>
          </Paper>
        </Grid>
      </Grid>

      {argumentFallacy.text !== null && (
        <Paper variant="outlined" sx={{ mt: 5, p: 3, bgcolor: '#fff9c4', borderColor: '#fff59d', borderRadius: 2 }}>
          <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2, color: '#f57f17' }}>
            Argument fallacy test
          </Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}><strong>Text:</strong> {argumentFallacy.text}</Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}><strong>Label:</strong> {argumentFallacy.label}</Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}><strong>Score:</strong> {argumentFallacy.score}</Typography>
          {argumentFallacy.explanation !== null && (
            <Typography variant="body2" sx={{ mt: 1 }}><strong>Explanation:</strong> {argumentFallacy.explanation}</Typography>
          )}
        </Paper>
      )}
    </Container>
  );
};

export default DebateDetail;