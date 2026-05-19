import { useState } from 'react';
import ArgumentForm from './ArgumentForm';
import { 
  Card, 
  CardContent, 
  CardActions, 
  Typography, 
  Button, 
  Box 
} from '@mui/material';
import CheckIcon from '@mui/icons-material/Check';
import DeleteIcon from '@mui/icons-material/Delete';
import ReplyIcon from '@mui/icons-material/Reply';
import PsychologyIcon from '@mui/icons-material/Psychology';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';


const AIArgument = ({ argument, onDelete, onFallacyCheck, onAddArgument, setReplyArgId, replyArgId, onGenerateArgument }) => {
  const [isAccepted, setIsAccepted] = useState(argument.type === "THESIS");


  const getBorderColor = () => {
    if (argument.type === "THESIS") return "#aa3bff"; 
    if (argument.type === "PRO") return "#2e7d32";  
    if (argument.type === "CON") return "#d32f2f";    
    return "divider";
  };

  return (
    <Card 
      variant="outlined" 
      sx={{ 
        width: '100%',
        borderLeft: `6px solid ${getBorderColor()}`,
        borderStyle: isAccepted ? 'solid' : 'dashed', 
        bgcolor: isAccepted ? 'background.paper' : 'rgba(0, 0, 0, 0.02)',
        transition: '0.2s',
        '&:hover': { boxShadow: 2 }
      }}
    >
      <CardContent sx={{ pb: 1 }}>
        <Typography 
          variant="body1" 
          sx={{ 
            fontWeight: argument.type === "THESIS" ? 'bold' : 'normal',
            fontStyle: isAccepted ? 'normal' : 'italic', 
            color: 'text.primary'
          }}
        >
          {argument.text}
        </Typography>
        
        <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
          Owner: <strong>{argument.owner.username}</strong>
        </Typography>
      </CardContent>

      <CardActions sx={{ justifyContent: 'flex-end', flexWrap: 'wrap', gap: 1, pt: 0, p: 2 }}>
        {!isAccepted ? (
          <>
            <Button 
              size="small" 
              variant="contained" 
              color="success" 
              startIcon={<CheckIcon />}
              onClick={(e) => { e.stopPropagation(); setIsAccepted(true); }}
            >
              Accept
            </Button>
            <Button 
              size="small" 
              variant="outlined" 
              color="error" 
              startIcon={<DeleteIcon />}
              onClick={(e) => { e.stopPropagation(); onDelete(argument.id); }}
            >
              Delete
            </Button>
          </>
        ) : (
          <>
            <Button 
              size="small" 
              variant="outlined" 
              startIcon={<ReplyIcon />}
              onClick={(e) => { e.stopPropagation(); setReplyArgId(argument.id); }}
            >
              React
            </Button>
            <Button 
              size="small" 
              variant="outlined" 
              color="warning" 
              startIcon={<PsychologyIcon />}
              onClick={(e) => { e.stopPropagation(); onFallacyCheck(argument.text); }}
            >
              Fallacy
            </Button>
            <Button 
              size="small" 
              variant="text" 
              color="success" 
              startIcon={<AutoAwesomeIcon />}
              onClick={(e) => { e.stopPropagation(); onGenerateArgument("PRO", argument.id); }}
            >
              supporting
            </Button>
            <Button 
              size="small" 
              variant="text" 
              color="error" 
              startIcon={<AutoAwesomeIcon />}
              onClick={(e) => { e.stopPropagation(); onGenerateArgument("CON", argument.id); }}
            >
              opposing
            </Button>
          </>
        )}
      </CardActions>

      {replyArgId === argument.id && (
        <Box 
          sx={{ 
            p: 2, 
            borderTop: '1px solid', 
            borderColor: 'divider', 
            bgcolor: 'rgba(0,0,0,0.01)' 
          }} 
          onClick={(e) => e.stopPropagation()}
        >
          <ArgumentForm 
            initialData={{ text: "", type: "PRO" }}
            onSubmit={(formData) => onAddArgument(argument.id, formData)}
            onCancel={() => setReplyArgId(null)}
          />
        </Box>
      )}
    </Card>
  );
};

export default AIArgument;