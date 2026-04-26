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

  const [updateArgId, setUpdateArgId] = useState(null);
  const [updateData, setUpdateData] = useState({text: "", type: "PRO"});

  const [argumentFallacy, setArgumentFallacy] = useState({text: "", label: "", score: null});


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
    setUpdateArgId(null);
  }

  const handleDeleteArgument = async (e, argumentId) => {
    e.stopPropagation();
    
    try {
      const response = await fetch(`http://localhost:8082/argument/${argumentId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${keycloak.token}`,
          'Content-Type': 'application/json'
        }
      });
      if(response.ok){
        await fetchData();              
      } else{
        console.error("server vratil chybu")
      }
    } catch (error) {
      console.error("Chyba:", error);
    }
  }

  const handleArgumentForm = (e, argumentId, type, text) => {
    e.stopPropagation();
    setReplyArgId(null);
    setUpdateData({type: type, text: text});
    setUpdateArgId(argumentId);
  }

  const handleUpdateSubmit = async (argumentId) => {
    const dto = {
      text: updateData.text,
      type: updateData.type
    }
    try {
      const response = await fetch(`http://localhost:8082/argument/${argumentId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${keycloak.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(dto)
      });
      if(response.ok){
        await fetchData();              
        setUpdateArgId(null);            
        setUpdateData({text: "", type: "PRO"}); 
      } else{
        console.error("server vratil chybu")
      }
    } catch (error) {
      console.error("Chyba:", error);
    }
  }

    const handleFallacyTest = async (argumentText) => {
    const dto = {
      text: argumentText
    };
    try {
      const response = await fetch(`http://localhost:8082/argument/fallacy`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${keycloak.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(dto)
      });
      if(response.ok){
        const data = await response.json();
        setArgumentFallacy({
          text: argumentText,
          label: data.fallacy,
          score: data.confidence
        });
        console.log(data);
      }
    } catch (error) {
      console.error("Chyba při hledani:", error);
    }
  }

  

  return (
    <div className="detail-container">
      <h1>{debate.title}</h1>
      <div className='layerContainer'>
        <article className="THESIS" onClick={() => handleArgumentClick(thesis)}>
          <p>{thesis.text}</p>
          <p>{thesis.owner.username}</p>
          <button onClick={(e) => handleOpenForm(e, thesis.id)}>Reagovat</button>
          {replyArgId === thesis.id && (
            <div className="reply-form" onClick={(e) => e.stopPropagation()}>
              <input type="text" placeholder="Text" onChange={(e) => setReplyData({ ...replyData, text: e.target.value })} value={replyData.text}/>
              <input type="text" placeholder='PRO/CON' onChange={(e) => setReplyData({ ...replyData, type: e.target.value })} value={replyData.type}/>
              <button onClick={() => handleSubmit(thesis.id)}>Send</button>
              <button onClick={() => setReplyArgId(null)}>Zrušit</button>
            </div>
            )}
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
                    <button onClick={() => handleFallacyTest(child.text)}>Check for fallacy</button>
                    {
                      keycloak.tokenParsed?.preferred_username === child.owner.username && (
                        <div>
                          <button onClick={(e) => handleDeleteArgument(e, child.id)}>Smazat argument</button>
                          <button onClick={(e) => handleArgumentForm(e, child.id, child.type, child.text)}>Upravit</button>
                        </div>
                      )
                    }
                  </article>
                  {replyArgId === child.id && (
                    <div className="reply-form" onClick={(e) => e.stopPropagation()}>
                      <input type="text" placeholder="Text" onChange={(e) => setReplyData({ ...replyData, text: e.target.value })} value={replyData.text}/>
                      <input type="text" placeholder='PRO/CON' onChange={(e) => setReplyData({ ...replyData, type: e.target.value })} value={replyData.type}/>
                      <button onClick={() => handleSubmit(child.id)}>Send</button>
                      <button onClick={() => setReplyArgId(null)}>Zrušit</button>
                    </div>
                  )}
                  {updateArgId === child.id && (
                    <div className="update-form" onClick={(e) => e.stopPropagation()}>
                      <input type="text" placeholder="Text" onChange={(e) => setUpdateData({ ...updateData, text: e.target.value })} value={updateData.text}/>
                      <input type="text" placeholder='PRO/CON' onChange={(e) => setUpdateData({ ...updateData, type: e.target.value })} value={updateData.type}/>
                      <button onClick={() => handleUpdateSubmit(child.id)}>Send</button>
                      <button onClick={() => setUpdateArgId(null)}>Zrušit</button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          );
        })
      }
      </div>
      {argumentFallacy.text !== "" &&
        <div className='THESIS'>
          <h2>Argument fallacy test</h2>
          <p>Text: {argumentFallacy.text}</p>
          <p>Label: {argumentFallacy.label}</p>
          <p>Score: {argumentFallacy.score}</p>
        </div>
      }
      
      <button onClick={() => navigate('/')}>Zpět</button>
    </div>
  );
};



export default DebateDetail;