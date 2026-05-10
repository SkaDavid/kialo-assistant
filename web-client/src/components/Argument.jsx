import ArgumentForm from "./ArgumentForm.jsx";

const Argument = ({ arg, activePath, currentUser, currentAction, handlers }) => {
  const isReplyOpen = currentAction.replyArgId === arg.id; 
  const isUpdateOpen = currentAction.updateArgId === arg.id; 

  const termitLink=`http://localhost:1234/termit/#/vocabularies/debate-${arg.debate}/document/${arg.debate}-${arg.id}.html?namespace=http://onto.fel.cvut.cz/ontologies/slovnik&fileNamespace=http://onto.fel.cvut.cz/ontologies/slovnik/debate-${arg.debate}/document/soubor/`

  return (
    <div className={activePath.includes(arg.id) ? "active argument-wrapper" : "argument-wrapper"}>
      <article className={arg.type} onClick={() => handlers.onArgumentClick(arg)}>
        <p>{arg.text}</p>
        <p className="owner">{arg.owner.username}</p>
        
        <div className="actions">
          <button onClick={(e) => handlers.onOpenReply(e, arg.id)}>React</button>
          <button onClick={(e) => handlers.onFallacyTest(e, arg.text)}>Check for fallacy</button>
          {currentUser === arg.owner.username && (
            <>
              <button onClick={(e) => handlers.onDelete(e, arg.id)}>Delete</button>
              <button onClick={(e) => handlers.onOpenUpdate(e, arg.id)}>Change</button>
              <a href={termitLink} target="_blank">Go to termit</a>
              <button onClick={() => handlers.onSyncArgument(arg.id)}>Sync</button>
            </>
          )}
        </div>
      </article>

      {isReplyOpen && (
        <ArgumentForm 
          initialData={{ text: "", type: "PRO"}}  
          onSubmit={(data) => handlers.onSubmitReply(arg.id, data)}
          onCancel={() => handlers.setReplyArgId(null)}
        />
      )}

      {isUpdateOpen && (
        <ArgumentForm 
          initialData={{ text: arg.text, type: arg.type }}
          onSubmit={(data) => handlers.onSubmitUpdate(arg.id, data)}
          onCancel={() => handlers.setUpdateArgId(null)}
        />
      )}
    </div>
  );
};

export default Argument;