import { useState } from 'react';

const DebateForm = ({ data, setData, handleFormSubmit, handleAiCreate }) => {
    return (
      <form onSubmit={handleFormSubmit}>
        <input type="text" value={data.topic} onChange={(e) => setData({...data, topic: e.target.value})} placeholder='Topic'/>
        <input type="text" value={data.thesis} onChange={(e) => setData({...data, thesis: e.target.value})} placeholder='Thesis'/>
        <label htmlFor="private"> private </label>
        <input type="checkbox" name="private" checked={data.isPrivate} onChange={(e) => setData({...data, isPrivate: e.target.checked})}/>
        <input type="submit"/>
        <button type="button" onClick={handleAiCreate}>Create with AI</button>
      </form>
  );
}

export default DebateForm;