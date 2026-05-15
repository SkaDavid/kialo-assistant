import { useState, useEffect } from 'react'
import { assistantApi, contentApi } from './api';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [debate, setDebate] = useState("");

  useEffect(() => {
    chrome.storage.local.get(["access_token"], (result) => {
      if (result.access_token) setIsLoggedIn(true)
    })
  }, [])

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
    const rawData = await contentApi.getDebateInfo();
    const parsedArgs = parseArguments(rawData.arguments, rawData.debateId);

    const debateDto = {
        topic: rawData.topic,
        debateId: rawData.debateId,
        arguments: parsedArgs 
    };

    setDebate(JSON.stringify(debateDto, null, 2));
    const apiResult = await assistantApi.createDebate(debateDto);
    console.log(apiResult);
};

  return (
    <div style={{ padding: '1rem' }}>
      <h1>Kialo Assistant</h1>
      {!isLoggedIn ? (
        <button onClick={login}>Log in through keycloak</button>
      ) : (
        <div>
          <button onClick={handleImportDebate}>Import debate</button>
          <p>{debate}</p>
          
          <button onClick={logout} style={{ marginBottom: '10px' }}>Logout</button>
          
        </div>
      )}
    </div>
  )
}


const parseArguments = (rawArguments, debateId) => {
    const claims = rawArguments.claims;
    const locations = rawArguments.locations;
    const parsedArguments = [];

    claims.forEach((claim) => {
        if(claim.id == debateId + ".0"){
            return;
        }
        const claimsLocation = locations.find(location => location.targetId == claim.id);

        let claimsType;
        if(claimsLocation.relation == 0){
            claimsType = "THESIS";
        } else{
            claimsLocation.relation == 1 ? claimsType = "PRO" : claimsType = "CON";
        }

        const argumentId = claim.id.split(".")[1];
        const parentId = claimsLocation.parentId.split(".")[1];

        const newArgument = {
            id: argumentId,
            text: claim.text,
            type: claimsType, 
            parent: parentId
        }
        parsedArguments.push(newArgument);
    });

    console.log(parsedArguments);
    return parsedArguments;
}

export default App