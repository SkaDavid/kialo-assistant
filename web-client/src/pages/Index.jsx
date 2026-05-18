import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import keycloak from '../config/keycloak';
import { 
  Container, 
  Typography, 
  Button, 
  Card, 
  CardContent, 
  CardActions, 
  TextField, 
  Box,
  Grid,
  Chip,
  Radio, 
  RadioGroup, 
  FormControlLabel, 
  FormControl, 
  FormLabel
} from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import DeleteIcon from '@mui/icons-material/Delete';
import AddBoxIcon from '@mui/icons-material/AddBox';
import { api } from '../api.js';


const Index = () => {
  const navigate = useNavigate();

  const [debates, setDebates] = useState(null);
  const [updateDebateId, setDebateUpdateId] = useState(null);
  const [updateDebateData, setUpdateDebateData] = useState({ topic: "1", visibility: "PUBLIC" });

  const fetchData = async () => {
    try {
      const response = await fetch("http://localhost:8082/debate", {
          method: 'GET',
          headers: {
          'Authorization': `Bearer ${keycloak.token}`,
          'Content-Type': 'application/json'
          }
      });
      const data = await response.json();
      setDebates(data);
    } catch (error) {
      console.error("Chyba při načítání:", error);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  if (!debates) return <Typography sx={{ p: 4, textAlign: 'center' }}>Načítání...</Typography>;

  const handleUpdateFormSend = async (e, debateId) => {
    e.stopPropagation();
    const dto = { 
      topic: updateDebateData.topic,
      visibility: updateDebateData.visibility
    };
    
    try {
      const response = await fetch(`http://localhost:8082/debate/${debateId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${keycloak.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(dto)
      });
      if(response.ok){
        setDebateUpdateId(null);
        fetchData();
      }
    } catch (error) {
      console.error("Chyba při úpravě:", error);
    }
  };

  const handleUpdateForm = (e, debate) => {
    e.stopPropagation();
    setDebateUpdateId(debate.id);
    setUpdateDebateData({ 
      topic: debate.title,
      visibility: debate.visibility
    });
  };

  const handleLogout = () => {
    keycloak.logout({ redirectUri: window.location.origin });
  };

  const handleDeleteDebate = async (e, debate) => {
    e.stopPropagation();
    const result = api.deleteDebate(debate.id);
    fetchData();
  }

  const handleNewDebate = () => {
    navigate("/createDebate");
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 4 }}>
        <Typography variant="h4" component="h1" sx={{ fontWeight: 'bold' }}>
          Kialo Assistant
        </Typography>
        <Button 
          variant="outlined" 
          color="error" 
          startIcon={<AddBoxIcon />} 
          onClick={handleNewDebate}
        >
          Create new debate
        </Button>
        <Button 
          variant="outlined" 
          color="error" 
          startIcon={<LogoutIcon />} 
          onClick={handleLogout}
        >
          Logout
        </Button>
      </Box>

      <Grid container spacing={3}>
        {debates.map((debate) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={debate.id}>
            <Card 
              variant="outlined" 
              sx={{ 
                height: '100%', 
                display: 'flex', 
                flexDirection: 'column',
                justifyContent: 'space-between',
                cursor: 'pointer',
                transition: '0.2s',
                '&:hover': { boxShadow: 3, transform: 'translateY(-2px)' }
              }}
              onClick={() => navigate(`/debate/${debate.id}`)}
            >
              <CardContent>
                <Chip 
                  label={debate.visibility} 
                  color={debate.visibility === 'PUBLIC' ? 'success' : 'warning'} 
                  size="small" 
                  sx={{ mb: 1.5, fontWeight: 'bold' }}
                />
                
                <Typography variant="h6" component="h2" sx={{ fontWeight: 'bold', mb: 1 }}>
                  {debate.title}
                </Typography>
                
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2, fontStyle: 'italic' }}>
                  {debate.arguments[0]?.text || "Žádný úvodní argument"}
                </Typography>

                <Typography variant="caption" display="block" color="text.secondary">
                  Autor: <strong>{debate.owner.username}</strong>
                </Typography>

                {updateDebateId === debate.id && (
                  <Box 
                    sx={{ 
                      display: "flex", 
                      flexDirection: "column", 
                      gap: 2, 
                      mt: 2 
                    }}
                    onClick={(e) => e.stopPropagation()}
                  >
                    <TextField 
                      label="Název tématu"
                      size="small" 
                      fullWidth
                      value={updateDebateData.topic} 
                      onChange={(e) => setUpdateDebateData({...updateDebateData, topic: e.target.value})} 
                    />
                    
                    <FormControl component="fieldset" size="small">
                      <FormLabel component="legend" sx={{ fontSize: '0.85rem', fontWeight: 'bold' }}>Viditelnost</FormLabel>
                      <RadioGroup
                        row
                        value={updateDebateData.visibility}
                        onChange={(e) => setUpdateDebateData({...updateDebateData, visibility: e.target.value})}
                      >
                        <FormControlLabel value="PUBLIC" control={<Radio size="small" />} label="Public" />
                        <FormControlLabel value="PRIVATE" control={<Radio size="small" />} label="Private" />
                      </RadioGroup>
                    </FormControl>

                    <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                      <Button 
                        variant="contained" 
                        size="medium"
                        startIcon={<SaveIcon />}
                        onClick={(e) => handleUpdateFormSend(e, debate.id)}
                      >
                        Save
                      </Button>
                    </Box>
                  </Box>
                )}
              </CardContent>

              {keycloak.tokenParsed?.preferred_username === debate.owner.username && (
                <CardActions sx={{ justifyContent: 'flex-end', p: 1.5, pt: 0 }}>
                  <Button
                    size="medium"
                    variant="error" 
                    startIcon={<DeleteIcon />} 
                    onClick={(e) => handleDeleteDebate(e, debate)}
                  >
                    Delete
                  </Button>
                  <Button 
                    size="medium"
                    variant="outlined" 
                    startIcon={<EditIcon />} 
                    onClick={(e) => handleUpdateForm(e, debate)}
                  >
                    Edit
                  </Button>
                </CardActions>
              )}
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
};

export default Index;