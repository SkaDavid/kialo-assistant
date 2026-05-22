import ArgumentForm from "./ArgumentForm.jsx";
import { 
  Card, 
  CardContent, 
  CardActions, 
  Typography, 
  Button, 
  Box,
  Tooltip
} from '@mui/material';
import ReplyIcon from '@mui/icons-material/Reply';
import PsychologyIcon from '@mui/icons-material/Psychology';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import SyncIcon from '@mui/icons-material/Sync';
import LaunchIcon from '@mui/icons-material/Launch'; 
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

const Argument = ({ arg, activePath, currentUser, currentAction, handlers }) => {
  const isReplyOpen = currentAction.replyArgId === arg.id; 
  const isUpdateOpen = currentAction.updateArgId === arg.id; 
  const hasFallacy = arg.fallacyCheck?.fallacyResult === "FALLACY";
  const isClean = arg.fallacyCheck?.fallacyResult === "CLEAN";

  const termitLink = `http://localhost:1234/termit/#/vocabularies/debate-${arg.debate}/document/${arg.debate}-${arg.id}.html?namespace=http://onto.fel.cvut.cz/ontologies/slovnik&fileNamespace=http://onto.fel.cvut.cz/ontologies/slovnik/debate-${arg.debate}/document/soubor/`;

  const getBorderColor = () => {
    if (arg.type === "THESIS") return "#aa3bff";
    if (arg.type === "PRO") return "#2e7d32";   
    if (arg.type === "CON") return "#d32f2f";   
    return "divider";
  };

  const isActive = activePath.includes(arg.id);

  return (
    <Box sx={{ width: '100%' }}>
      <Card 
        variant="outlined"
        onClick={() => handlers.onArgumentClick(arg)}
        sx={{
          width: '100%',
          borderLeft: `6px solid ${getBorderColor()}`,
          boxShadow: isActive ? 4 : 1,
          borderColor: isActive ? 'primary.main' : 'divider',
          transition: '0.2s',
          cursor: 'pointer',
          '&:hover': { boxShadow: isActive ? 5 : 3 }
        }}
      >
        <CardContent sx={{ pb: 1 }}>
          <Typography variant="body1" component="div" sx={{ mb: 1, lineHeight: 1.6 }}>
            {arg.structuredText.map((segment, index) => (
              segment.type === "TERM" ? (
                <Tooltip 
                  key={index} 
                  title={segment.explanation || "No definition available"} 
                  arrow 
                  placement="top" 
                  enterDelay={300} 
                >
                  <Box
                    component="span"
                    onClick={(e) => {
                      e.stopPropagation();
                    }}
                    sx={{
                      color: 'primary.main',
                      fontWeight: 'bold',
                      textDecoration: 'underline',
                      cursor: 'pointer',
                      display: 'inline-block',
                      '&:hover': { color: 'primary.dark' }
                    }}
                  >
                    {segment.text}
                  </Box>
                </Tooltip>
              ) : (
                <span key={index}>{segment.text}</span>
              )
            ))}
          </Typography>
          {hasFallacy && (
            <Box 
              sx={{ 
                mt: 2, 
                mb: 1.5, 
                p: 2, 
                bgcolor: '#fde8e8', 
                borderLeft: '4px solid #d32f2f', 
                borderRadius: 1 
              }}
              onClick={(e) => e.stopPropagation()} 
            >
              <Typography 
                variant="subtitle2" 
                color="error.main" 
                sx={{ fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}
              >
                <ReportProblemIcon fontSize="small" /> Fallacy defected
              </Typography>
              <Typography variant="body2" color="text.primary" sx={{ fontStyle: 'italic' }}>
                {arg.fallacyCheck.explanation}
              </Typography>
            </Box>
          )}
          {isClean && (
            <Box 
              sx={{ 
                mt: 1.5, 
                mb: 1.5, 
                p: 1.2, 
                bgcolor: '#e8f5e9',
                borderLeft: '4px solid #2e7d32',
                borderRadius: 1,
                display: 'flex',
                alignItems: 'center',
                gap: 1
              }}
              onClick={(e) => e.stopPropagation()}
            >
              <CheckCircleIcon color="success" fontSize="small" />
              <Typography variant="body2" color="success.main" sx={{ fontWeight: 'medium' }}>
                Fallacy checked. This argument is clean.
              </Typography>
            </Box>
          )}

          <Typography variant="caption" color="text.secondary">
            Author: <strong>{arg.owner.username}</strong>
          </Typography>
        </CardContent>

        <CardActions sx={{ justifyContent: 'flex-end', flexWrap: 'wrap', gap: 1, pt: 0, p: 2 }}>
          {hasFallacy && (
            <Button
              size="small"
              variant="contained"
              color="error"
              startIcon={<ReportProblemIcon />}
              onClick={(e) => handlers.onDeleteFallacy(e, arg.id)}
              sx={{ mr: 'auto' }} 
            >
              Clear fallacy
            </Button>
          )}
          <Button
            size="small"
            variant="text"
            startIcon={<ReplyIcon />}
            onClick={(e) => handlers.onOpenReply(e, arg.id)}
          >
            React
          </Button>

          <Button
            size="small"
            variant="text"
            color="warning"
            startIcon={<PsychologyIcon />}
            onClick={(e) => handlers.onFallacyTest(e, arg.text, arg.id)}
          >
            Check for fallacy
          </Button>

          {currentUser === arg.owner.username && (
            <>
              <Button
                size="small"
                variant="outlined"
                color="error"
                startIcon={<DeleteIcon />}
                onClick={(e) => handlers.onDelete(e, arg.id)}
              >
                Delete
              </Button>

              <Button
                size="small"
                variant="outlined"
                startIcon={<EditIcon />}
                onClick={(e) => handlers.onOpenUpdate(e, arg.id)}
              >
                Edit
              </Button>
              <Button
                size="small"
                variant="outlined"
                color="secondary"
                component="a"
                href={termitLink}
                target="_blank"
                rel="noopener noreferrer"
                startIcon={<LaunchIcon />}
                onClick={(e) => e.stopPropagation()} 
              >
                Open in termit
              </Button>

              <Button
                size="small"
                variant="contained"
                color="info"
                startIcon={<SyncIcon />}
                onClick={(e) => { e.stopPropagation(); handlers.onSyncArgument(arg.id); }}
              >
                Sync with termit
              </Button>
            </>
          )}
        </CardActions>

        {isReplyOpen && (
          <Box 
            sx={{ p: 2, borderTop: '1px solid', borderColor: 'divider', bgcolor: 'rgba(0,0,0,0.01)' }}
            onClick={(e) => e.stopPropagation()}
          >
            <ArgumentForm 
              initialData={{ text: "", type: "PRO"}}  
              onSubmit={(data) => handlers.onSubmitReply(arg.id, data)}
              onCancel={() => handlers.setReplyArgId(null)}
              onGenerateAI={(type) => handlers.onGenerateAI(arg.id, type)}
            />
          </Box>
        )}

        {isUpdateOpen && (
          <Box 
            sx={{ p: 2, borderTop: '1px solid', borderColor: 'divider', bgcolor: 'rgba(0,0,0,0.01)' }}
            onClick={(e) => e.stopPropagation()}
          >
            <ArgumentForm 
              initialData={{ text: arg.text, type: arg.type }}
              onSubmit={(data) => handlers.onSubmitUpdate(arg.id, data)}
              onCancel={() => handlers.setUpdateArgId(null)}
            />
          </Box>
        )}
      </Card>
    </Box>
  );
};

export default Argument;