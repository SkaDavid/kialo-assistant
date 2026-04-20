import keycloak from './config/keycloak.js'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

keycloak.init({
  onLoad: 'login-required',
  checkLoginIframe: false,
  pkceMethod: 'S256'
}).then((authenticated) => {
  if (authenticated) {
    createRoot(document.getElementById('root')).render(
      <StrictMode>
        <App />
      </StrictMode>
    );
  }
}).catch((err) => {
  console.error("Selhala inicializace Keycloaku:", err);
});