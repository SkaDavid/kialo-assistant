import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import keycloak from '../config/keycloak';
import { api } from '../api/api.js';
import Argument from '../components/Argument';
import './DebateDetail.css';

const DebateDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [debate, setDebate] = useState(null);
  const [activePath, setActivePath] = useState([]);
  const [replyArgId, setReplyArgId] = useState(null);
  const [updateArgId, setUpdateArgId] = useState(null);
  const [argumentFallacy, setArgumentFallacy] = useState({ text: null, label: null, score: null, explanation: null })

  const fetchData = async () => {
    const data = await api.getArgument(id);
    setDebate(data);
  };

  useEffect(() => { fetchData(); }, [id]);

  if (!debate) return <p>Načítání...</p>;

  const thesis = debate.arguments.find(arg => arg.type === "THESIS");

  const handleArgumentClick = (clicked) => {
    const parentIndex = activePath.indexOf(clicked.parent);
    if (parentIndex !== -1) {
      setActivePath([...activePath.slice(0, parentIndex + 1), clicked.id]);
    } else {
      setActivePath([...activePath, clicked.id]);
    }
  };

  return (
    <div className="detail-container">
      <h1>{debate.title}</h1>
      <div className='layerContainer'>
        <Argument 
          arg={thesis} 
          activePath={activePath} 
          currentUser={keycloak.tokenParsed?.preferred_username}
          currentAction={{ replyArgId, updateArgId }}
          handlers={{
                    onArgumentClick: handleArgumentClick,
                    onOpenReply: (e, id) => { e.stopPropagation(); setReplyArgId(id); setUpdateArgId(null); },
                    onOpenUpdate: (e, id) => { e.stopPropagation(); setUpdateArgId(id); setReplyArgId(null); },
                    onDelete: async (e, id) => { e.stopPropagation(); await api.deleteArgument(id); fetchData(); },
                    onSubmitReply: async (parentId, data) => { 
                      await api.createArgument({ debateId: id, parentId: parentId, ...data });
                      setReplyArgId(null); 
                      fetchData(); 
                    },
                    onSubmitUpdate: async (id, data) => {
                      await api.updateArgument(id, data);
                      setUpdateArgId(null); 
                      fetchData();
                    },
                    setReplyArgId: setReplyArgId, 
                    setUpdateArgId: setUpdateArgId,
                    onFallacyTest: async (e, text) => {
                      e.stopPropagation();
                      const data = await api.testFallacy(text);
                      if(data){
                        setArgumentFallacy({
                          text: text,
                          label: data.label,
                          score: data.score,
                          explanation: data.explanation
                        });
                      }
                    },
                    onSyncArgument: async (id) => {
                      const response = await api.syncArgument(id);
                      console.log(response);
                    }
                  }}
        />

        {activePath.map(parentId => {
          const children = debate.arguments.filter(arg => arg.parent === parentId);
          if (children.length === 0) return null;
          return (
            <div key={parentId} className="argContainer">
              {children.map(child => (
                <Argument 
                  key={child.id}
                  arg={child}
                  activePath={activePath}
                  currentUser={keycloak.tokenParsed?.preferred_username}
                  currentAction={{ replyArgId, updateArgId }}
                  handlers={{
                    onArgumentClick: handleArgumentClick,
                    onOpenReply: (e, id) => { e.stopPropagation(); setReplyArgId(id); setUpdateArgId(null); },
                    onOpenUpdate: (e, id) => { e.stopPropagation(); setUpdateArgId(id); setReplyArgId(null); },
                    onDelete: async (e, id) => { e.stopPropagation(); await api.deleteArgument(id); fetchData(); },
                    onSubmitReply: async (parentId, data) => { 
                      await api.createArgument({ debateId: id, parentId: parentId, ...data });
                      setReplyArgId(null); 
                      fetchData(); 
                    },
                    onSubmitUpdate: async (id, data) => {
                      await api.updateArgument(id, data);
                      setUpdateArgId(null); 
                      fetchData();
                    },
                    setReplyArgId: setReplyArgId, 
                    setUpdateArgId: setUpdateArgId,
                    onFallacyTest: async (e, text) => {
                      e.stopPropagation();
                      const data = await api.testFallacy(text);
                      if(data){
                        setArgumentFallacy({
                          text: text,
                          label: data.label,
                          score: data.score,
                          explanation: data.explanation
                        });
                      }
                    },
                    onSyncArgument: async (id) => {
                      await api.syncArgument(id);
                      console.log(response);
                    }
                  }}
                />
              ))}
            </div>
          );
        })}
      </div>
      {argumentFallacy.text !== null &&
        <div className='THESIS'>
          <h2>Argument fallacy test</h2>
          <p>Text: {argumentFallacy.text}</p>
          <p>Label: {argumentFallacy.label}</p>
          <p>Score: {argumentFallacy.score}</p>
          {argumentFallacy.explanation !== null &&
            <p>Explanation: {argumentFallacy.explanation}</p>
          }
        </div>
      }
      <button onClick={() => navigate('/')}>Back to main menu</button>
    </div>
  );
};

export default DebateDetail;