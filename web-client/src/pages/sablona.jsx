import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import keycloak from '../keycloak'; // Tvůj inicializovaný keycloak

const DetailPage = () => {
  // 1. ZÍSKÁNÍ ID Z URL (např. /detail/123)
  const { id } = useParams();
  const navigate = useNavigate();

  // 2. STAV (Paměť komponenty)
  const [item, setItem] = useState(null); // Tady budou data z backendu
  const [loading, setLoading] = useState(true);

  // 3. EFEKT (Akce po načtení stránky)
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch(`http://localhost:8082/api/data/${id}`, {
          headers: {
            // Použijeme token z tvého realmu 'termit'
            'Authorization': `Bearer ${keycloak.token}`,
          }
        });
        const data = await response.json();
        setItem(data);
      } catch (error) {
        console.error("Chyba při načítání:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]); // [id] znamená: "Spusť to znovu, pokud se změní ID v URL"

  // 4. LOGIKA PODMÍNĚNÉHO RENDEROVÁNÍ
  if (loading) return <p>Načítám data z backendu...</p>;
  if (!item) return <p>Položka nebyla nalezena.</p>;

  // 5. SAMOTNÉ "HTML" (JSX)
  return (
    <div className="detail-container">
      <button onClick={() => navigate('/')}>← Zpět na seznam</button>
      
      <h1>Detail: {item.title}</h1>
      <div className="content">
        <p>{item.description}</p>
        <span className="badge">Status: {item.status}</span>
      </div>

      {/* Ukázka role: Pouze admin uvidí mazací tlačítko */}
      {keycloak.hasRealmRole('ROLE_ADMIN') && (
        <button className="delete-btn">Smazat položku</button>
      )}
    </div>
  );
};

export default DetailPage;