import { useNavigate } from 'react-router-dom';
import { useState } from 'react'
import keycloak from '../config/keycloak';


const CreateDebate = () => {
  const navigate = useNavigate();

  const [topic, setTopic] = useState("");
  const [thesis, setThesis] = useState("");

  const [data, setData] = useState({topic: "", thesis: "", isPrivate: false})

  const handleFormSubmit = async (e) => {
    e.preventDefault();

    const visibilityData = data.isPrivate ? "PRIVATE" : "PUBLIC";
    const dto = {
      topic: data.topic,
      thesis: data.thesis,
      visibility: visibilityData
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
        <input type="text" value={data.topic} onChange={(e) => setData({...data, topic: e.target.value})} placeholder='Topic'/>
        <input type="text" value={data.thesis} onChange={(e) => setData({...data, thesis: e.target.value})} placeholder='Thesis'/>
        <label htmlFor="private"> private </label>
        <input type="checkbox" name="private" checked={data.isPrivate} onChange={(e) => setData({...data, isPrivate: e.target.checked})}/>
        <input type="submit"/>
      </form>
    </div>
  );
};

export default CreateDebate;