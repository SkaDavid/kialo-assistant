import { useNavigate } from 'react-router-dom';
import { useState } from 'react'
import DebateForm from "../components/DebateForm"
import keycloak from '../config/keycloak';
import { api } from "../api/api"
import AIDebatePreview from '../components/AIDebate';


const CreateDebate = () => {
  const navigate = useNavigate();

  const [data, setData] = useState({topic: "", thesis: "", isPrivate: false});
  const [aiResponse, setAiResponse] = useState(null);
  const [fallacyCheck, setFallacyCheck] = useState({text: null, label: null, score: null});
  const [replyArgId, setReplyArgId] = useState(null);

  const handleFormSubmit = async (e) => {
    e.preventDefault();

    const dto = getData();

    try {
      const response = await api.createDebate(dto);
      navigate("/");
    } catch (error) {
      console.error("Error when sending create debate request:", error);
    }
  } 

  const handleAiCreateSubmit = async () => {
    const dto = getData();

    try {
      /* const response = await api.createAIDebate(dto); */
      const response = getResponse();
      setAiResponse(response);
    } catch (error) {
      console.error("Error when sending create debate request:", error);
    }
  }

  const getData = () => {
    const visibilityData = data.isPrivate ? "PRIVATE" : "PUBLIC";
    const dto = {
      topic: data.topic,
      thesis: data.thesis,
      visibility: visibilityData
    }
    return dto;
  }

  const handleDeleteArgument = (argumentId) => {
    setAiResponse({
      ...aiResponse, 
      arguments: aiResponse.arguments.filter(argument => argument.id !== argumentId)
    });
  }

  const handleFallacyCheck = async (text) => {
    const data = await api.testFallacy(text);
    setFallacyCheck({
      text: text,
      label: data.fallacy,
      score: data.confidence
    });
  }

  const handleAddArgument = (parentId, formData) => {
    const newArgument = {
        id: Date.now(), 
        text: formData.text,
        type: formData.type,
        parent: parentId,
        owner: {
          id: 1,
          username: "You"
        }
    };

    setAiResponse({
        ...aiResponse,
        arguments: [...aiResponse.arguments, newArgument]
    });
    setReplyArgId(null);
};

  const handleGenerateArgument = async (type, argumentId) => {
    const argument = aiResponse.arguments.find((argument) => argument.id === argumentId);
    
    const children = aiResponse.arguments.filter((argument) => argument.parent === argumentId);
    let currentParentId = argument.parent;

    let parents = [];
    while(currentParentId != null){
      const parent = aiResponse.arguments.find((argument) => argument.id === currentParentId);
      if(parent){
        parents.push(parent);
        currentParentId = parent.parent;
      } else {
        currentParentId = null;
      }
    }

    const debate = [...parents.reverse(), argument, ...children]; 

    const dto = {
      type: type,
      text: argument.text,
      debate: debate
    }

    const reply = await api.createAIArgument(dto);
    const newArgument = {
      id: Date.now(),
      text: reply.text,
      type: type,
      parent: argumentId,
      owner: {
        id: 1,
        username: "AI"
      }
    }
    setAiResponse({
        ...aiResponse,
        arguments: [...aiResponse.arguments, newArgument]
    });
  }

  const handleDebateSubmit = async () => {
    const dto = {
      topic: data.topic,
      ...aiResponse
    };
    const reply = await api.importDebate(dto);
  }

  return (
    <div className="create-debate-container">
      <h1>Create debate</h1>
      <DebateForm 
        data={data}
        setData={setData}
        handleFormSubmit={handleFormSubmit}
        handleAiCreate={handleAiCreateSubmit}
        onAddArgument={handleAddArgument}
        replyArgId={replyArgId} 
        setReplyArgId={setReplyArgId}
        onGenerateArgument={handleGenerateArgument}
      />
      {aiResponse && (
          <AIDebatePreview 
            debateData={aiResponse}
            fallacyData={fallacyCheck}
            onDelete={(id) => handleDeleteArgument(id)} 
            onFallacyCheck={handleFallacyCheck}
            onAddArgument={handleAddArgument}
            replyArgId={replyArgId} 
            setReplyArgId={setReplyArgId}
            onGenerateArgument={handleGenerateArgument}
            onSubmitDebate={handleDebateSubmit}
          />
      )}
    </div>
  )
};


const getResponse = () => {
  return {
    "thesis": "Bears are better than people",
    "arguments": [
        {
            "id": 1,
            "text": "Bears are better than people",
            "type": "THESIS",
            "parent": null,
            "owner": {
                "id": 0,
                "username": "AI",
                "debates": null
            },
            "debate": null
        },
        {
            "id": 2,
            "text": "Bears live harmoniously in nature and do not damage the environment the way humans often do.",
            "type": "PRO",
            "parent": 1,
            "owner": {
                "id": 0,
                "username": "AI",
                "debates": null
            },
            "debate": null
        },
        {
            "id": 3,
            "text": "Bears are strong, independent creatures that symbolize resilience and survival, qualities that can be admired more than some human traits.",
            "type": "PRO",
            "parent": 1,
            "owner": {
                "id": 0,
                "username": "AI",
                "debates": null
            },
            "debate": null
        },
        {
            "id": 4,
            "text": "People have created complex societies, technologies, and cultures that have advanced civilization in ways bears cannot.",
            "type": "CON",
            "parent": 1,
            "owner": {
                "id": 0,
                "username": "AI",
                "debates": null
            },
            "debate": null
        },
        {
            "id": 5,
            "text": "Humans possess empathy, reasoning, and moral capacities that allow them to solve problems and help others, making them more valuable to society than bears.",
            "type": "CON",
            "parent": 1,
            "owner": {
                "id": 0,
                "username": "AI",
                "debates": null
            },
            "debate": null
        }
    ],
    "owner": {
        "id": 0,
        "username": "AI",
        "debates": null
    }
}
} 

export default CreateDebate;