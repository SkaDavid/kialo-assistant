import { useState } from 'react';
import { api } from '../api'

import{
  Box, 
  TextField, 
  Radio, 
  RadioGroup, 
  FormControlLabel, 
  FormControl, 
  Button,
  Paper,
  Typography
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import CloseIcon from '@mui/icons-material/Close';

const ArgumentForm = ({ onSubmit, onCancel, initialData, onGenerateAI }) => {
  const [formData, setFormData] = useState(initialData);
  const [fallacyResult, setFallacyResult] = useState(null);
  const [hasBeenChecked, setHasBeenChecked] = useState(false);

  const handleAIGenerate = async (type) => {
    try {
      const aiText = await onGenerateAI(type);
      if (aiText) {
        setFormData({ text: aiText, type: type });
      }
    } catch (error) {
      console.error("Chyba při komunikaci s AI:", error);
    }
  };

  const handleSendClick = async () => {
    if (hasBeenChecked) {
      onSubmit(formData);
      return;
    }

    try {
      const data = await api.testFallacy({ text: formData.text });
      if (data && data.score > 0.75) {
        setFallacyResult(data);
        setHasBeenChecked(true);
      } else {
        onSubmit(formData);
      }
    } catch (error) {
      console.error("Error when checking fallacy", error);
      setHasBeenChecked(true);
    }
  };

  return (
    <Box 
      className="argument-form" 
      onClick={(e) => e.stopPropagation()} 
      sx={{ 
        display: 'flex', 
        flexDirection: 'column', 
        gap: 1.5,
        mt: 1,
        width: '100%'
      }}
    >
      <TextField 
        label="Argument Text"
        placeholder="Write your reaction..."
        variant="outlined"
        size="small"
        fullWidth
        multiline
        rows={2}
        value={formData.text}
        onChange={(e) => setFormData({ ...formData, text: e.target.value })}
      />

      <FormControl component="fieldset" size="small">
        <RadioGroup
          row
          name="argType"
          value={formData.type}
          onChange={(e) => setFormData({ ...formData, type: e.target.value })}
        >
          <FormControlLabel 
            value="PRO" 
            control={<Radio size="small" color="success" />} 
            label="Pro" 
          />
          <FormControlLabel 
            value="CON" 
            control={<Radio size="small" color="error" />} 
            label="Con" 
          />
        </RadioGroup>
      </FormControl>
      {fallacyResult && (
        <Paper 
          variant="outlined" 
          sx={{ 
            p: 1.5, 
            bgcolor: '#fff9c4', 
            borderColor: '#fff59d', 
            borderRadius: 1,
            display: 'flex',
            flexDirection: 'column',
            gap: 0.5
          }}
        >
          <Typography 
            variant="subtitle2" 
            color="warning.dark" 
            sx={{ fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: 1 }}
          >
            Fallacy detected!
          </Typography>
          {fallacyResult.explanation && (
            <Typography variant="body2" color="text.primary" sx={{ fontStyle: 'italic' }}>
              <strong>Explanation:</strong> {fallacyResult.explanation}
            </Typography>
          )}
        </Paper>
      )}
      <Box sx={{ display: 'flex', gap: 1 }}>
        {onGenerateAI && (
          <>
            <Button
              size="small"
              variant="text"
              color="success"
              onClick={() => handleAIGenerate("PRO")}
            >
              Generate Pro argument
            </Button>
            <Button
              size="small"
              variant="text"
              color="error"
              onClick={() => handleAIGenerate("CON")}
            >
              Generovat Con argument
            </Button>
          </>
        )}
      </Box>
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
        <Button 
          size="small"
          variant="outlined" 
          color="inherit"
          startIcon={<CloseIcon />}
          onClick={onCancel}
        >
          Cancel
        </Button>
        <Button 
          size="small"
          variant="contained" 
          color="primary"
          startIcon={<SendIcon />}
          onClick={handleSendClick}
        >
          Send
        </Button>
      </Box>
    </Box>
  );
}

export default ArgumentForm;