import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import keycloak from '../config/keycloak';


const Index = () => {
  const navigate = useNavigate();

  const [debates, setDebates] = useState(null);


  useEffect(() => {
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

    fetchData();
  }, []);
  if (!debates) return <p>Načítání</p>;

  return (
    <div className="debate-container">      
      <h1>Seznam debat</h1>
      <ul className="content">
        {debates.map((debate) => (
          <li key={debate.id} className="debate-card" onClick={() => navigate(`/debate/${debate.id}`)}>
            <h3>{debate.title}</h3>
            <p>{debate.arguments[0]?.text}</p>
            <p>{debate.owner.username}</p>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Index;