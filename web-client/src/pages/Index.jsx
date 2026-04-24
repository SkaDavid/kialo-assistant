import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import keycloak from '../config/keycloak';


const Index = () => {
  const navigate = useNavigate();

  const [debates, setDebates] = useState(null);
  const [updateDebateId, setDebateUpdateId] = useState(null);
  const [updateDebateData, setUpdateDebateData] = useState({topic: "1"});


    const fetchData = async () => {
      try {
        const response = await fetch("http://localhost:8082/debate", {
            method: 'GET',
            headers: {
            'Authorization': `Bearer ${keycloak.token}`,
            'Content-Type': 'application/json'
            }
        });
        const data = await response.json();
        setDebates(data);
      } catch (error) {
        console.error("Chyba při načítání:", error);
      }
    };

  useEffect(() => {
    fetchData();
  }, []);
  if (!debates) return <p>Načítání</p>;

  const handleUpdateFormSend = async (e, debateId) => {
    e.stopPropagation();
    const dto = {
      topic: updateDebateData.topic
    }
    
    try {
      const response = await fetch(`http://localhost:8082/debate/${debateId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${keycloak.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(dto)
      });
      if(response.ok){
        setDebateUpdateId(null);
        fetchData();
      }
    } catch (error) {
      console.error("Chyba při hledani:", error);
    }
  }

  const handleUpdateForm = (e, debateId) => {
    e.stopPropagation();
    setDebateUpdateId(debateId);
    setUpdateDebateData({ topic: debate.title });
  }

  return (
    <div className="debate-container">      
      <h1>Seznam debat</h1>
      <ul className="content">
        {debates.map((debate) => (
          <li key={debate.id} className="debate-card" onClick={() => navigate(`/debate/${debate.id}`)}>
            <h3>{debate.title}</h3>
            <p>{debate.arguments[0]?.text}</p>
            <p>{debate.owner.username}</p>
            <p><strong>{debate.visibility}</strong></p>
            {keycloak.tokenParsed?.preferred_username === debate.owner.username && (
              <button onClick={(e) => handleUpdateForm(e, debate.id)}>Upravit</button>
            )}
            {
              updateDebateId === debate.id && 
              <div className="debateForm">
                <input type="text" value={updateDebateData.topic} onChange={(e) => setUpdateDebateData({...updateDebateData, topic: e.target.value})} />
                <button onClick={(e) => handleUpdateFormSend(e, debate.id)}>Upravit</button>
              </div>
            }
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Index;