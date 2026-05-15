import { useState, useEffect } from 'react'
import { assistantApi, contentApi } from './api';

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [completeDebate, setCompleteDebate] = useState("");
    const [currentDebateInfo, setCurrentDebateInfo] = useState({ debateId: "", argumentVersions: [] });
    const [assistantInfo, setAssistantInfo] = useState({});


    useEffect(() => {
    chrome.storage.local.get(["access_token"], (result) => {
        if (result.access_token) setIsLoggedIn(true);
    });

    const fetchContentData = async () => {
        try {
            const info = await contentApi.getDebateInfo();
            setCurrentDebateInfo(info);
        } catch (err) {
            console.error("Chyba při načítání info z content scriptu:", err);
        }
    };

    fetchContentData();
    }, []);

    useEffect(() => {
            if (currentDebateInfo.debateId) {
            const fetchAssistantData = async () => {
                try {
                    const info = await assistantApi.getDebateInfo(currentDebateInfo.debateId);
                    setAssistantInfo(info);
                    console.log("Data z backendu přijata:", info);
                } catch (err) {
                    console.error("Chyba při načítání dat z backendu:", err);
                }
            };
            fetchAssistantData();
        }
    }, [currentDebateInfo.debateId]);

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
            {currentDebateInfo.isPresent &&
                <button onClick={handleImportDebate}>Import debate</button>
            }  
            <p>{completeDebate}</p>
          
            <button onClick={logout} style={{ marginBottom: '10px' }}>Logout</button>
          
        </div>
      )}
    </div>
  )
}




export default App