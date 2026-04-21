import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import keycloak from '../config/keycloak';
import './DebateDetail.css'

const DebateDetail = () => {
  const { id } = useParams();
  const [debate, setDebate] = useState(null);
  const [activePath, setActivePath] = useState([]);

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

  const thesis = debate?.arguments.find(arg => arg.type === "THESIS");

  useEffect(() => {
    if(thesis){
      setActivePath([thesis.id]);
    }
  }, [debate]);

  if (!debate) return <p>Načítám detail debaty...</p>;

  const findParent = (argument, parentId) => {
    return argument.parent === parentId;
  }


  const handleArgumentClick = (clickedArgument) => {
    if (clickedArgument.type === "THESIS") {
      setActivePath([clickedArgument.id]);
      return;
    }

    const parentIndex = activePath.indexOf(clickedArgument.parent);

    if (parentIndex !== -1) {
      const newPath = activePath.slice(0, parentIndex + 1);
      newPath.push(clickedArgument.id);
      setActivePath(newPath);
    } else {
      setActivePath([...activePath, clickedArgument.id]);
    }
  };
  

  return (
    <div className="detail-container">
      <h1>{debate.title}</h1>
      <div className='layerContainer'>
        <article className="THESIS" onClick={() => handleArgumentClick(thesis)}>
          <p>{thesis.text}</p>
          <p>{thesis.owner.username}</p>
        </article>
      {activePath.map((parentId, index) => {
          const children = debate.arguments.filter(arg => arg.parent === parentId);
          if (children.length === 0) return null;
          return (
            <div key={parentId} className='argContainer'>
              {
                children.map((child) => (
                  <article key={child.id} className={child.type} onClick={() => handleArgumentClick(child)}>
                    <p>{child.text}</p>
                    <p>{child.owner.username}</p>
                  </article>
                ))
              }
            </div>
          );
        })
      }
      </div>
      <button onClick={() => navigate('/')}>Zpět</button>
    </div>
  );
};



export default DebateDetail;