import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import DebateForm from "../components/DebateForm";
import keycloak from '../config/keycloak';
import { api } from "../api.js";
import AIArgument from '../components/AIArgument'; 
import { 
  Container, 
  Typography, 
  Paper, 
  Box, 
  Button,
  Grid,
  Stack,
  Divider
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';

const CreateDebate = () => {
  const navigate = useNavigate();

  const [data, setData] = useState({ topic: "", thesis: "", isPrivate: false });
  const [aiResponse, setAiResponse] = useState(null);
  const [fallacyCheck, setFallacyCheck] = useState({ text: null, label: null, score: null });
  const [replyArgId, setReplyArgId] = useState(null);

  const [activeId, setActiveId] = useState(null);

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    const dto = getData();

    try {
      const response = await api.createDebate(dto);
      navigate("/");
    } catch (error) {
      console.error("Error when sending create debate request:", error);
    }
  };

  const handleAiCreateSubmit = async () => {
    const dto = getData();

    try {
      const response = await api.createAIDebate({ thesis: data.thesis});
      setAiResponse(response);
      setActiveId(null); 
    } catch (error) {
      console.error("Error when sending create debate request:", error);
    }
  };

  const getData = () => {
    const visibilityData = data.isPrivate ? "PRIVATE" : "PUBLIC";
    const dto = {
      topic: data.topic,
      thesis: data.thesis,
      visibility: visibilityData
    }
    return dto;
  };

  const handleDeleteArgument = (argumentId) => {
    if (activeId === argumentId) {
      setActiveId(null);
    }
    setAiResponse({
      ...aiResponse, 
      arguments: aiResponse.arguments.filter(argument => argument.id !== argumentId)
    });
  };

  const handleFallacyCheck = async (text) => {
    const data = await api.testFallacy(text);
    setFallacyCheck({
      text: text,
      label: data.label,
      score: data.score
    });
  };

  const handleAddArgument = (parentId, formData) => {
    const newArgument = {
        id: Date.now(), 
        text: formData.text,
        type: formData.type,
        parent: parentId,
        owner: { id: 1, username: "You" }
    };

    setAiResponse({
        ...aiResponse,
        arguments: [...aiResponse.arguments, newArgument]
    });
    setReplyArgId(null);
  };

  const handleGenerateArgument = async (type, argumentId) => {
    const argument = aiResponse.arguments.find((argument) => argument.id === argumentId);
    const children = aiResponse.arguments.filter((argument) => argument.parent === argumentId);
    let currentParentId = argument.parent;

    let parents = [];
    while(currentParentId != null){
      const parent = aiResponse.arguments.find((argument) => argument.id === currentParentId);
      if(parent){
        parents.push(parent);
        currentParentId = parent.parent;
      } else {
        currentParentId = null;
      }
    }

    const debate = [...parents.reverse(), argument, ...children]; 

    const dto = {
      type: type,
      text: argument.text,
      debate: debate
    };

    const reply = await api.createAIArgument(dto);
    const newArgument = {
      id: Date.now(),
      text: reply.text,
      type: type,
      parent: argumentId,
      owner: { id: 1, username: "AI" }
    };
    setAiResponse({
        ...aiResponse,
        arguments: [...aiResponse.arguments, newArgument]
    });
  };

  const handleDebateSubmit = async () => {
    const dto = {
      topic: data.topic,
      ...aiResponse
    };
    const reply = await api.importDebate(dto);
  };

  const thesis = aiResponse?.arguments.find(arg => arg.type === "THESIS");
  const currentActiveId = activeId || thesis?.id;

  const getLineage = () => {
    if (!aiResponse || !thesis) return [];
    const path = [];
    let current = aiResponse.arguments.find(arg => arg.id === currentActiveId);
    
    while (current && current.type !== "THESIS") {
      path.unshift(current);
      current = aiResponse.arguments.find(arg => arg.id === current.parent);
    }
    return path;
  };

  const lineage = getLineage();

  const previewChildren = aiResponse?.arguments.filter(arg => arg.parent === currentActiveId) || [];
  const proArguments = previewChildren.filter(arg => arg.type === "PRO");
  const conArguments = previewChildren.filter(arg => arg.type === "CON");

  const aiArgumentProps = {
    onDelete: handleDeleteArgument,
    onFallacyCheck: handleFallacyCheck,
    onAddArgument: handleAddArgument,
    setReplyArgId: setReplyArgId,
    replyArgId: replyArgId,
    onGenerateArgument: handleGenerateArgument
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Box sx={{ mb: 2, display: "flex", justifyContent: "flex-start" }}>
        <Button variant="text" startIcon={<ArrowBackIcon />} onClick={() => navigate('/')}>
          Back to main menu
        </Button>
      </Box>

      <Paper variant="outlined" sx={{ p: 4, mb: 4, borderRadius: 2 }}>
        <Typography variant="h4" component="h1" sx={{ fontWeight: 'bold', mb: 3 }}>
          Create
        </Typography>

        <DebateForm 
          data={data}
          setData={setData}
          handleFormSubmit={handleFormSubmit}
          handleAiCreate={handleAiCreateSubmit}
          onAddArgument={handleAddArgument}
          replyArgId={replyArgId} 
          setReplyArgId={setReplyArgId}
          onGenerateArgument={handleGenerateArgument}
        />
      </Paper>

      {aiResponse && (
        <Box sx={{ mt: 5 }}>
          <Typography variant="h5" sx={{ fontWeight: 'bold', mb: 3, textAlign: 'center' }}>
            AI Generated Debate Preview 
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
                    borderColor: currentActiveId === thesis.id ? 'primary.main' : 'transparent'
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
                      borderColor: currentActiveId === arg.id ? 'primary.main' : 'transparent'
                    }}
                  >
                    <AIArgument argument={arg} {...aiArgumentProps} />
                  </Box>
                </Box>
              ))}
            </Stack>
          </Box>

          <Divider sx={{ my: 3 }} />

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
                </Stack>
              </Paper>
            </Grid>
          </Grid>

          {fallacyCheck.text !== null && (
            <Paper variant="outlined" sx={{ mt: 4, p: 3, bgcolor: '#fafafa', borderRadius: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1 }}>Argument fallacy test</Typography>
              <Typography variant="body2"><strong>Text:</strong> {fallacyCheck.text}</Typography>
              <Typography variant="body2"><strong>Label:</strong> {fallacyCheck.label}</Typography>
              <Typography variant="body2"><strong>Score:</strong> {fallacyCheck.score}</Typography>
            </Paper>
          )}

          <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
            <Button variant="contained" size="large" color="success" onClick={handleDebateSubmit}>
              Save full debate
            </Button>
          </Box>
        </Box>
      )}
    </Container>
  );
};

export default CreateDebate;