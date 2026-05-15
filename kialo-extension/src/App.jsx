import { useState, useEffect } from 'react'
import { assistantApi, contentApi } from './api';

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [completeDebate, setCompleteDebate] = useState("");
    const [currentDebateInfo, setCurrentDebateInfo] = useState({ debateId: "", argumentVersions: [] });
    const [assistantInfo, setAssistantInfo] = useState({ isPresent: null, id: null, argumentVersions: null });
    const [unknownArguments, setUnknownArguments] = useState([]);
    const [modifiedArguments, setModifiedArguments] = useState([]);


    useEffect(() => {
    const initData = async () => {
        try {
            const contentInfo = await contentApi.getDebateInfo();
            setCurrentDebateInfo(contentInfo);

            if (contentInfo?.debateId) {
                const assistInfo = await assistantApi.getDebateInfo(contentInfo.debateId);
                setAssistantInfo(assistInfo);

                if (assistInfo?.argumentVersions) {
                    const newArguments = assistInfo.argumentVersions.filter(argument => argument.kialoId == null);
                    let fullArguments = [];
                    for (const argument of newArguments) {
                        const detail = await assistantApi.getArgument(argument.id);
                        fullArguments.push(detail);
                    }
                    setUnknownArguments(fullArguments);
                        let foundModifiedArguments = [];
                        currentDebateInfo.argumentVersions.forEach(kialoArgument => {
                            const assistantArgument = assistInfo.argumentVersions.find(assistantArgument => assistantArgument.id === kialoArgument.id);
                            if(assistantArgument && assistantArgument.version !== kialoArgument.version){
                                console.log("found modified " + assistantArgument.id);
                                foundModifiedArguments.push(assistantArgument);
                            }
                        });
                        setModifiedArguments(foundModifiedArguments)
                }
            }
        } catch (err) {
            console.error("Něco se nepovedlo:", err);
        }
    };

    initData();
}, []);


    const login = () => {
        chrome.runtime.sendMessage({ action: "login" }, (res) => {
            if (res?.success) setIsLoggedIn(true);
            console.log("Login called")
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
                </article>
            ))}

            <h2>Modified on Kialo</h2>
            {modifiedArguments.map((version) => (
                <article key={argument.id} style={{ border: "2px solid white", marginBottom: "10px", padding: "5px" }}>
                    <p><strong>ID:</strong> {argument.id}</p>
                    <p><strong>Version:</strong> {argument.version}</p>
                </article>
            ))}
          
            <button onClick={logout} style={{ marginBottom: '10px' }}>Logout</button>
          
        </div>
      )}
    </div>
  )
}




export default App