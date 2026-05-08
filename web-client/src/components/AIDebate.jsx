import AIArgument from './AIArgument';

const AIDebatePreview = ({ debateData, fallacyData, onDelete, onFallacyCheck, onAddArgument, setReplyArgId, replyArgId, onGenerateArgument, onSubmitDebate }) => {
  if (!debateData) return null;

  return (
    <div className="layerContainer">
      <h2>AI Generated Preview: {debateData.thesis}</h2>
      
      <div className="argContainer">
        {debateData.arguments.filter((argument) => argument.type === "THESIS").map((argument) => (
          <AIArgument key={argument.id} 
            argument={argument}
            onDelete={onDelete}
            onFallacyCheck={onFallacyCheck}
            onAddArgument={onAddArgument}
            replyArgId={replyArgId} 
            setReplyArgId={setReplyArgId}
            onGenerateArgument={onGenerateArgument}
          />
        ))}
      </div>
      <div className="argContainer">
        {debateData.arguments.filter((argument) => argument.type !== "THESIS").map((argument) => (
          <AIArgument key={argument.id} 
            argument={argument}
            onDelete={onDelete}
            onFallacyCheck={onFallacyCheck}
            onAddArgument={onAddArgument}
            replyArgId={replyArgId} 
            setReplyArgId={setReplyArgId}
            onGenerateArgument={onGenerateArgument}
          />
        ))}
      </div>

      {fallacyData.text !== null &&
        <div className='THESIS'>
          <h2>Argument fallacy test</h2>
          <p>Text: {fallacyData.text}</p>
          <p>Label: {fallacyData.label}</p>
          <p>Score: {fallacyData.score}</p>
        </div>
      }

      <div className="preview-footer">
        <button className="btn-save-all" onClick={onSubmitDebate}>Save full debate to database</button>
      </div>
    </div>
  );
};

export default AIDebatePreview;