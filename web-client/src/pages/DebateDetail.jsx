import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import keycloak from '../config/keycloak';
import './DebateDetail.css'

const DebateDetail = () => {
  const { id } = useParams();
  const [debate, setDebate] = useState(null);
  const [activePath, setActivePath] = useState([]);

  const [replyArgId, setReplyArgId] = useState(null);
  const [replyData, setReplyData] = useState({text: "", type: "PRO"});

  const navigate = useNavigate();

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
    
  useEffect(() => {
    fetchData();
  }, []);
  
  const thesis = debate?.arguments.find(arg => arg.type === "THESIS");

  
  useEffect(() => {
    if(thesis){
      if(activePath.length > 1){
        return;
      }
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
  
  const handleSubmit = async (childId) => {
    const dto = {
      debateId: id,
      text: replyData.text,
      type: replyData.type,
      parentId: childId
    }
    
    const json_dto = JSON.stringify(dto);
    try {
      const response = await fetch("http://localhost:8082/argument", {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${keycloak.token}`,
          'Content-Type': 'application/json'
        },
        body: json_dto
      });
      if(response.ok){
        await fetchData();              
        setReplyArgId(null);            
        setReplyData({text: "", type: "PRO"}); 
      } else{
        console.error("server vratil chybu")
      }
    } catch (error) {
      console.error("Chyba:", error);
    }
  }
  
  const handleOpenForm = (e, childId) => {
    e.stopPropagation();
    setReplyArgId(childId);
  }

  

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
              {children.map((child) => (
                <div key={child.id} className="argument-wrapper">
                  <article className={child.type} onClick={() => handleArgumentClick(child)}>
                    <p>{child.text}</p>
                    <p>{child.owner.username}</p>
                    <button onClick={(e) => handleOpenForm(e, child.id)}>Reagovat</button>
                  </article>
                  {replyArgId === child.id && (
                    <div className="reply-form" onClick={(e) => e.stopPropagation()}>
                      <input type="text" placeholder="Text" onChange={(e) => setReplyData({ ...replyData, text: e.target.value })} value={replyData.text}/>
                      <input type="text" placeholder='PRO/CON' onChange={(e) => setReplyData({ ...replyData, type: e.target.value })} value={replyData.type}/>
                      <button onClick={() => handleSubmit(child.id)}>Send</button>
                      <button onClick={() => setReplyArgId(null)}>Zrušit</button>
                    </div>
                  )}
                </div>
              ))}
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