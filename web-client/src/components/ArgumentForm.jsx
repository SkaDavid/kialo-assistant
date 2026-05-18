import { useState } from 'react';
import { 
  Box, 
  TextField, 
  Radio, 
  RadioGroup, 
  FormControlLabel, 
  FormControl, 
  Button 
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import CloseIcon from '@mui/icons-material/Close';

const ArgumentForm = ({ onSubmit, onCancel, initialData }) => {
  const [formData, setFormData] = useState(initialData);

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
          onClick={() => onSubmit(formData)}
        >
          Send
        </Button>
      </Box>
    </Box>
  );
}

export default ArgumentForm;