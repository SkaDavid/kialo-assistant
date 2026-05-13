import { useState, useEffect } from 'react'

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  useEffect(() => {
    chrome.storage.local.get(["access_token"], (result) => {
      if (result.access_token) setIsLoggedIn(true)
    })
  }, [])

  const login = () => {
    chrome.runtime.sendMessage({ action: "login" }, (res) => {
      if (res?.success) setIsLoggedIn(true);
      console.log("Login called")
    })
  }

  const logout = () => {
    chrome.storage.local.remove("access_token", () => setIsLoggedIn(false))
  }

  return (
    <div style={{ padding: '1rem' }}>
      <h1>Kialo Assistant</h1>
      {!isLoggedIn ? (
        <button onClick={login}>Log in through keycloak</button>
      ) : (
        <div>
          <button onClick={logout} style={{ marginBottom: '10px' }}>Logout</button>
          <p>Jsi přihlášen! Tady může začít tvá nová React aplikace.</p>
        </div>
      )}
    </div>
  )
}

export default App