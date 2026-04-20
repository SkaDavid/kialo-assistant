import { useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import './App.css'
import Index from './pages/Index';
import DebateDetail from './pages/DebateDetail';
import CreateDebate from './pages/CreateDebate';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Index />} />
        <Route path="/debate/:id" element={<DebateDetail />} />
        <Route path="/createDebate" element={<CreateDebate />} />
      </Routes>
    </BrowserRouter>
  );
}


export default App
