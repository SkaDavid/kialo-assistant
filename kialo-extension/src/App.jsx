import { useState, useEffect } from 'react'
import { assistantApi, contentApi } from './api';

import { 
  Container, Typography, Stack, Button, Box, Card, CardContent, CardActions,
  TableContainer, Table, TableHead, TableBody, TableRow, TableCell, Paper,
  Accordion, AccordionSummary, AccordionDetails
} from '@mui/material';

import LogoutIcon from '@mui/icons-material/Logout';
import ImportExportIcon from '@mui/icons-material/ImportExport';
import LoginIcon from '@mui/icons-material/Login';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import SyncIcon from '@mui/icons-material/Sync';
import SearchIcon from '@mui/icons-material/Search';

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [completeDebate, setCompleteDebate] = useState("");
    
    const [currentDebateInfo, setCurrentDebateInfo] = useState({ debateId: "", argumentVersions: [] });
    const [assistantInfo, setAssistantInfo] = useState({ isPresent: null, id: null, argumentVersions: [] });
    
    const [unknownArguments, setUnknownArguments] = useState([]);
    const [newArguments, setNewArguments] = useState([]);
    const [modifiedArguments, setModifiedArguments] = useState([]);


    useEffect(() => {
    const initData = async () => {
        try {
            const contentInfo = await contentApi.getDebateInfo();
            console.log(contentInfo);
            setCurrentDebateInfo(contentInfo);

            if (contentInfo?.debateId) {
                const assistInfo = await assistantApi.getDebateInfo(contentInfo.debateId);
                console.log(assistInfo);
                setAssistantInfo(assistInfo);

                if (assistInfo?.argumentVersions) {
                    const newArguments = assistInfo.argumentVersions.filter(argument => argument.kialoId == null);
                    let fullArguments = [];
                    for (const argument of newArguments) {
                        const detail = await assistantApi.getArgument(argument.id);
                        fullArguments.push(detail);
                    }
                    console.log("unknown args: " + JSON.stringify(fullArguments));
                    setUnknownArguments(fullArguments);

                    let foundModifiedArguments = [];
                    let foundNewArguments = [];

                    contentInfo.argumentVersions.forEach(kialoArgument => {
                        const assistantArgument = assistInfo.argumentVersions.find(assistantArgument => assistantArgument.kialoId == kialoArgument.id);
                        if (!assistantArgument) {
                            if(kialoArgument.id === "0"){
                                return;
                            }
                            foundNewArguments.push(kialoArgument);
                        } else {
                            if (assistantArgument.version !== kialoArgument.version) {
                                foundModifiedArguments.push({...assistantArgument, text: kialoArgument.text, type: kialoArgument.type });
                            }
                        }
                    });
                    console.log("newArgs: " + JSON.stringify(foundNewArguments));
                    setNewArguments(foundNewArguments);
                    console.log("ModifiedArgs: " + JSON.stringify(foundModifiedArguments));
                    setModifiedArguments(foundModifiedArguments);
                }
            }
        } catch (err) {
            console.error("Something went wrong", err);
        }
    };

    initData();
}, []);


    const login = () => {
        chrome.runtime.sendMessage({ action: "login" }, (res) => {
            if (res?.success) setIsLoggedIn(true);
        })
    }

    const logout = () => {
        chrome.storage.local.remove("access_token", () => setIsLoggedIn(false))
    }

    const handleImportDebate = async () => {
        const data = await contentApi.getCompleteDebateInfo();
        setCompleteDebate(JSON.stringify(data, null, 2));
        const apiResult = await assistantApi.createDebate(data);
        console.log(apiResult);
    }

    const handleSendToKialo = async (argument) => {
        try {
            const parentMapping = assistantInfo.argumentVersions.find(item => item.id == argument.parent);
            if (!parentMapping || !parentMapping.kialoId) {
                alert("This arguments parent is not in kialo yet");
                return;
            }
            const response = await contentApi.postArgument({ 
                argumentText: argument.text,
                argumentType: argument.type,
                parentId: parentMapping.kialoId
            });
            
            console.log("Kialo response:", response)
        } catch(error) {
            console.error("Odesílání do Kiala selhalo:", error);
        }
    }

    const handleFindArgument = async (id) => {
        try {
            await contentApi.redirectTo(id);
        } catch (error) {
            console.error("Redirect failed:", error);
        }
    };

    const handleAssistantPutArgument = async (argument) => {
        console.log(argument);
        const dto = {
            id: argument.id,
            kialoVersion: argument.kialoVersion,
            text: argument.text,
            type: argument.type  
        }
        assistantApi.updateArgument(dto);
    }

    const handleAssistantPostArgument = async (argument) => {
        const dto = {
            text: argument.text,
            version: argument.version,
            kialoId: argument.id,
            type: argument.type, 
            parentId: argument.parent.substring(argument.parent.indexOf(".") + 1), 
            debateId: assistantInfo.id
        }
        console.log(dto);
        assistantApi.createArgument(dto);
    }

    const findParentText = (argument) => {
        const assistantVersion = assistantInfo.argumentVersions.find(version => version.id == argument.parent);
        return currentDebateInfo.argumentVersions.find(version => version.id == assistantVersion.kialoId).text.substring(0, 40) + "...";
    }

  return (
    <Container disableGutters sx={{ p: 2, maxWidth: "90%" }}>
      <Typography variant="h4" sx={{ fontWeight: "bold", mb: 3, textAlign: "center" }} >Kialo Assistant</Typography>
      {!isLoggedIn ? (
        <Stack 
            sx={{
                p: 3,
                border: "1px solid",
                borderColor: "divider",
                borderRadius: 2
            }}
        >
            <Typography variant="outlined" sx={{ fontWeight: "bold", mb: 3, textAlign: "center" }} >Please, log in to proceed</Typography>
            <Button variant="contained" color="primary" onClick={login} endIcon={<LoginIcon/>}>
                Log in
            </Button>
        </Stack>
        ) : (
        <Stack spacing={3}>
            {!assistantInfo.present && (
            <Stack 
                alignItems="center" 
                justifyContent="center" 
                sx={{
                    p: 3,
                    border: "1px solid",
                    borderColor: "divider",
                    borderRadius: 2
                }}
            >
                <Typography variant="outlined" sx={{ fontWeight: "bold", mb: 3, textAlign: "center" }} >This debate is not imported yet.</Typography>
                <Button variant="contained" color="primary" onClick={handleImportDebate} startIcon={<ImportExportIcon/>}>
                    Import debate
                </Button>
            </Stack>
            )}
            <Accordion variant="outlined" sx={{ borderRadius: '4px !important' }}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="h5" sx={{ fontWeight: "bold", color: '#d8ae24da' }}>
                        Unknown ({unknownArguments.length})
                    </Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <Stack spacing={1.5}>
                        {unknownArguments.map((argument) => (
                            <Card key={argument.id} variant="outlined" sx={{ borderLeft: `5px solid ${argument.type == "PRO" ? "green" : "red" }` }}>
                                <CardContent>
                                    <Typography variant="body2" sx={{ mb: 1 }}>
                                        <strong>Text:</strong> {argument.text}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary" display="block">
                                        <strong>Parent argument:</strong> {findParentText(argument)}
                                    </Typography>
                                </CardContent>
                                <CardActions sx={{ justifyContent: 'center', pt: 1, flexDirection: "column", gap: 1 }}>
                                    <Button
                                        size="small" 
                                        variant="outlined" 
                                        color="primary" 
                                        startIcon={<SearchIcon/>}
                                        onClick={() => handleFindArgument(assistantInfo.argumentVersions.find(version => version.id == argument.parent).kialoId)}
                                    >
                                        Find parent
                                    </Button>
                                    <Button 
                                        size="small" 
                                        variant="contained" 
                                        color="primary" 
                                        startIcon={<ImportExportIcon/>}
                                        onClick={() => handleSendToKialo(argument)}
                                    >
                                        Export to kialo
                                    </Button>
                                </CardActions>
                            </Card>
                        ))}
                    </Stack>
                </AccordionDetails>
            </Accordion>
            <Accordion variant="outlined" sx={{ borderRadius: '4px !important' }}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="h5" sx={{ fontWeight: "bold", color: '#0080a7' }}>
                        Modified on Kialo ({modifiedArguments.length})
                    </Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <Stack spacing={1.5}>
                        {modifiedArguments.map((argument) => (
                            <Card key={argument.id} variant="outlined" sx={{ borderLeft: `5px solid ${argument.type == "PRO" ? "green" : "red" }` }}>
                                <CardContent>
                                    <Typography variant="body2" sx={{ mb: 1 }}>
                                        <strong>New text:</strong> {argument.text}
                                    </Typography>
                                </CardContent>
                                <CardActions sx={{ justifyContent: 'flex-end', pt: 0, flexDirection: "column", gap: 1 }}>
                                    <Button
                                        size="small" 
                                        variant="outlined" 
                                        color="primary" 
                                        startIcon={<SearchIcon/>}
                                        onClick={() => handleFindArgument(argument.kialoId)}
                                    >
                                        Find argument
                                    </Button>
                                    <Button 
                                        size="small" 
                                        variant="contained" 
                                        color="primary"
                                        startIcon={<SyncIcon/>} 
                                        onClick={() => handleAssistantPutArgument(argument)}
                                    >
                                        Refresh
                                    </Button>
                                </CardActions>
                            </Card>
                        ))}
                    </Stack>
                </AccordionDetails>
            </Accordion>
            <Accordion variant="outlined" sx={{ borderRadius: '4px !important' }}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="h5" sx={{ fontWeight: "bold", color: '#007ea5' }}>
                        New on Kialo ({newArguments.length})
                    </Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <Stack spacing={1.5}>
                        {newArguments.map((argument) => (
                            <Card key={argument.id} variant="outlined" sx={{ borderLeft: `5px solid ${argument.type == "PRO" ? "green" : "red" }` }}>
                                <CardContent>
                                    <Typography variant="body2" sx={{ mb: 1 }}>
                                        <strong>Text:</strong> {argument.text}
                                    </Typography>
                                </CardContent>
                                <CardActions sx={{ justifyContent: 'flex-end', pt: 0, flexDirection: "column", gap: 1 }}>
                                    <Button
                                        size="small" 
                                        variant="outlined" 
                                        color="primary" 
                                        startIcon={<SearchIcon/>}
                                        onClick={() => handleFindArgument(argument.id)}
                                    >
                                        Find Argument
                                    </Button>
                                    <Button 
                                        size="small" 
                                        variant="contained" 
                                        color="primary" 
                                        startIcon={<ImportExportIcon/>}
                                        onClick={() => handleAssistantPostArgument(argument)}
                                    >
                                        Import to Assistant
                                    </Button>
                                </CardActions>
                            </Card>
                        ))}
                    </Stack>
                </AccordionDetails>
            </Accordion>
        {assistantInfo.terms && assistantInfo.terms.length > 0 && (
            <Accordion variant="outlined" sx={{ borderRadius: '4px !important' }}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="h5" sx={{ fontWeight: "bold", color: '#2e7d32' }}>
                        Terms & Definitions ({assistantInfo.terms.length})
                    </Typography>
                </AccordionSummary>
                <AccordionDetails sx={{ p: 0 }}> {/* Odsazení p: 0 zajistí, že tabulka hezky sedne ke krajům */}
                    <TableContainer component={Paper} variant="outlined" sx={{ border: 'none' }}>
                        <Table size="small">
                            <TableHead sx={{ bgcolor: 'rgba(0, 0, 0, 0.04)' }}>
                                <TableRow>
                                    <TableCell sx={{ fontWeight: 'bold', width: '30%' }}>Term</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Definition</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {assistantInfo.terms.map((term) => (
                                    <TableRow 
                                        key={term.term} 
                                        sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                                    >
                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                                            {term.term}
                                        </TableCell>
                                        <TableCell>
                                            {term.definition}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </AccordionDetails>
            </Accordion>
        )}          
            <Button 
                size="medium" 
                variant="contained" 
                color="primary" 
                onClick={logout}
                endIcon={<LogoutIcon/>}
            >
                Logout
            </Button>
        </Stack>
      )}
    </Container>
  )
}




export default App