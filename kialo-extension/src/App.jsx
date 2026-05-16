import { useState, useEffect } from 'react'
import { assistantApi, contentApi } from './api';

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
                                foundModifiedArguments.push(assistantArgument);
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

  return (
    <div style={{ padding: '1rem' }}>
      <h1>Kialo Assistant</h1>
      {!isLoggedIn ? (
        <button onClick={login}>Log in through keycloak</button>
      ) : (
        <div>
            {!currentDebateInfo.isPresent &&
                <button onClick={handleImportDebate}>Import debate</button>
            }  
            <p>{completeDebate}</p>
            <h2>Unknown</h2>
            {unknownArguments.map((argument) => (
                <article key={argument.id} style={{ border: "2px solid yellow", marginBottom: "10px", padding: "5px" }}>
                    <p><strong>ID:</strong> {argument.id}</p>
                    <p><strong>Text:</strong> {argument.text}</p>
                    <p><strong>Type:</strong> {argument.type}</p>
                    <p>parent ID: {argument.parent}</p>
                    <button onClick={() => handleSendToKialo(argument)}>Send to kialo</button>
                </article>
            ))}

            <h2>Modified on Kialo</h2>
            {modifiedArguments.map((argument) => (
                <article key={argument.id} style={{ border: "2px solid white", marginBottom: "10px", padding: "5px" }}>
                    <p><strong>ID:</strong> {argument.id}</p>
                    <p><strong>Version:</strong> {argument.version}</p>
                </article>
            ))}

            <h2>New on Kialo</h2>
            {newArguments.map((argument) => (
                <article key={argument.id} style={{ border: "2px solid red", marginBottom: "10px", padding: "5px" }}>
                    <p><strong>ID:</strong> {argument.id}</p>
                    <p><strong>Kialo ID:</strong> {argument.kialoId}</p>
                    <p>{argument.version}</p>
                </article>
            ))}


            <button onClick={logout} style={{ marginBottom: '10px' }}>Logout</button>
            <div className="terms">
                {assistantInfo.terms ? assistantInfo.terms.map(term => (
                    <article style={{ border: "2px solid green", marginBottom: "10px", padding: "5px" }} key={terms.term}>
                        <p><strong>Term:</strong>{term.term}</p>
                        <p><strong>Definition:</strong>{term.definition}</p>
                    </article>
                ))
                :
                <></>}
                
            </div>
          
        </div>
      )}
    </div>
  )
}




export default App