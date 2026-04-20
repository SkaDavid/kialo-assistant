import { useParams } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';

const DebateDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();



  return (
    <div className="detail-container">
      <h1>Debata číslo: {id}</h1>
      <button onClick={() => navigate('/')}>Zpět</button>
    </div>
  );
};

export default DebateDetail;