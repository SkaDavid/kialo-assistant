import { 
  TextField, 
  Checkbox, 
  FormControlLabel, 
  Button, 
  Box 
} from '@mui/material';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import AddIcon from '@mui/icons-material/Add'; 

const DebateForm = ({ data, setData, handleFormSubmit, handleAiCreate }) => {
  return (
    <Box 
      component="form" 
      onSubmit={handleFormSubmit} 
      sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}
    >
      <TextField
        label="Topic"
        variant="outlined"
        fullWidth
        value={data.topic}
        onChange={(e) => setData({ ...data, topic: e.target.value })}
      />
      
      <TextField
        label="Thesis"
        variant="outlined"
        fullWidth
        multiline
        rows={2} 
        value={data.thesis}
        onChange={(e) => setData({ ...data, thesis: e.target.value })}
      />

      <Box sx={{ alignSelf: 'flex-start' }}>
        <FormControlLabel
          control={
            <Checkbox
              checked={data.isPrivate}
              onChange={(e) => setData({ ...data, isPrivate: e.target.checked })}
              color="primary"
            />
          }
          label="Private"
        />
      </Box>

      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 1 }}>
        
        <Button
          type="button"
          variant="outlined"
          color="secondary"
          startIcon={<AutoAwesomeIcon />}
          onClick={handleAiCreate}
        >
          Create with AI
        </Button>
        
        <Button
          type="submit"
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
        >
          Create
        </Button>
        
      </Box>
    </Box>
  );
}

export default DebateForm;