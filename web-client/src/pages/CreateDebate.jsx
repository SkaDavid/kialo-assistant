import { useNavigate } from 'react-router-dom';
import { useState } from 'react'
import keycloak from '../config/keycloak';


const CreateDebate = () => {
  const navigate = useNavigate();

  const [topic, setTopic] = useState("");
  const [thesis, setThesis] = useState("");

  const handleFormSubmit = async (e) => {
    e.preventDefault();

    const dto = {
      topic: topic,
      thesis: thesis,
      ownerId: "1"
    }

    const json_dto = JSON.stringify(dto);
    try {
      const response = await fetch("http://localhost:8082/debate", {
          method: 'POST',
          headers: {
          'Authorization': `Bearer ${keycloak.token}`,
          'Content-Type': 'application/json'
          },
          body: json_dto
      });
      if(response.ok){
        navigate("/");
      } else{
        console.error("server vratil chybu")
      }
    } catch (error) {
      console.error("Chyba:", error);
    }
  } 



  return (
    <div className="create-debate-container">
      <h1>Create debate</h1>
      <form onSubmit={handleFormSubmit}>
        <input type="text" value={topic} onChange={(e) => setTopic(e.target.value)} placeholder='Topic'/>
        <input type="text" value={thesis} onChange={(e) => setThesis(e.target.value)} placeholder='Thesis'/>
        <input type="submit"/>
      </form>
    </div>
  );
};

export default CreateDebate;