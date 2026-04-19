import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'
import './App.css'

function App() {
  const [count, setCount] = useState(0);


  return <div>
    <h3>Count is: {count}</h3>
    <Button text="click" count={count} setCount={setCount}/>
  </div>;
}

function Button({ text, count, setCount }) {
  const handleClick = () => {
    setCount( count + 1)
  }

  return (
    <button onClick={handleClick}>{text} + {count}</button>
  );
}

export default App
