import { useState } from 'react';
import ArgumentForm from './ArgumentForm';

const AIArgument = ({ argument, onDelete, onFallacyCheck, onAddArgument, setReplyArgId, replyArgId, onGenerateArgument }) => {
    const [isAccepted, setIsAccepted] = useState(argument.type === "THESIS");
    const isThesis = argument.type === "THESIS" ? true : false; 


  return (
    <article className={`${argument.type} ${isAccepted ? 'accepted' : 'preview'}`}>
      <p>{argument.text}</p>
      <p className="owner">Owner: {argument.owner.username}</p>
      <div>
        {!isAccepted ? (
          <>
            <button onClick={() => setIsAccepted(true)}>Accept argument</button>
            <button onClick={() => onDelete(argument.id)}>Delete argument</button>
          </>
        ) : (
          <>
            <button onClick={() => onGenerateArgument("PRO", argument.id)}>Generate supporting argument</button>
            <button onClick={() => onGenerateArgument("CON", argument.id)}>Generate opposing argument</button>
            <button onClick={() => setReplyArgId(argument.id)}>React</button>
            <button onClick={() => onFallacyCheck(argument.text)}>Check for fallacy</button>
          </>
        )}
      </div>
      {replyArgId === argument.id && (
                <ArgumentForm 
                    initialData={{ text: "", type: "PRO" }}
                    onSubmit={(formData) => onAddArgument(argument.id, formData)}
                    onCancel={() => setReplyArgId(null)}
                />
        )}
    </article>
  );
};

export default AIArgument;