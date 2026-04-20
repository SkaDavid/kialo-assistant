import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import keycloak from '../config/keycloak';
import './DebateDetail.css'

const DebateDetail = () => {
  const { id } = useParams();
  const [debate, setDebate] = useState(null);
  const navigate = useNavigate();
  useEffect(() => {
      const fetchData = async () => {
        try {
          const response = await fetch(`http://localhost:8082/debate/${id}`, {
              method: 'GET',
              headers: {
              'Authorization': `Bearer ${keycloak.token}`,
              'Content-Type': 'application/json'
              }
          });
          const data = await response.json();
          setDebate(data);
        } catch (error) {
          console.error("Chyba při hledani:", error);
        }
      };

      fetchData();
    }, []);

  if (!debate) return <p>Načítám detail debaty...</p>;
  return (
    <div className="detail-container">
      <h1>{debate.title}</h1>
      {
        debate.arguments.map((argument) => (
          <article className={argument.type}>
            <p>{argument.text}</p>
            <p>{argument.owner.username}</p>
          </article>
        ))
      }
      <button onClick={() => navigate('/')}>Zpět</button>
    </div>
  );
};

export default DebateDetail;