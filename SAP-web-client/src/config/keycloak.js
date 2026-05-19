import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:1234/termit/sluzby/auth/',
  realm: 'termit',
  clientId: 'react-app',
});

export default keycloak;