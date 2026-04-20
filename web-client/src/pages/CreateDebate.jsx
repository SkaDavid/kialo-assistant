import { useNavigate } from 'react-router-dom';


const CreateDebate = () => {
  return (
    <div className="detail-container">
      <h1>Create debate</h1>
      <button onClick={() => navigate('/')}>Zpět</button>
    </div>
  );
};

export default CreateDebate;