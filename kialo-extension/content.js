chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.action === "createDiscussion"){
    handleDiscussionCreation(message.payload);
  }    
});

async function handleDiscussionCreation(messagePayload){
  const csrfToken = getToken();
  const debateDto = messagePayload;
  const name = debateDto.name;
  const thesis = debateDto.thesis;
  const confirmedArguments = debateDto.confirmedArguments;
  let debateId;

  try {
    const res = await fetch(
      "https://www.kialo.com/api/v1/discussions",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Csrftoken": csrfToken,
        },
        body: JSON.stringify({
          "title": name,
          "topNodeText": thesis,
          "language":"en",
          "isMultipleChoice":false,
          "isAnonymousParticipationType":false,
          "isPublic":false,
          "backgroundInfo":"",
          "enableVoting":false,
          "displayArgumentFeedback":"Never",
          "displayArgumentAuthors":"Never",
          "groupModeConfig":{
            "assignmentMethod":"nogroups",
            "groupCount":null
          },
          "markdownVersion":1
        }),
        credentials: "include",
      }
    );
    console.log("Discussion status: ", res.status);
    const data = await res.json(); 
    debateId = data.discussion.id;
    await sleep(1500);
  } catch (err) {
    console.error("Discussion creation failed: ", err);
  }
  for (const argument of confirmedArguments){
    console.log("sending a message- type of argument:" + argument.type + ", text of argument:\n" + argument.text );
    try {
      const res = await fetch(
        "https://www.kialo.com/api/v1/discussiongraph",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "X-Csrftoken": csrfToken,
          },
          body: JSON.stringify({
            "parentLocationPath":[`${debateId}.1`,`${debateId}.2`],
            "relation": argument.type === "pro" ? 1 : -1,
            "text":argument.text,
            "isSuggestion":false,
            "markdownVersion":1
          }),
          credentials: "include",
        }
      );
      await sleep(1500);
      console.log("argument status: ", res.status);
    } catch (err) {
      console.error("argument creation failed: ", err);
    }
  }
  window.location.href = `https://www.kialo.com/${debateId}`;
}

function getCookie(name) {
  return document.cookie.match(new RegExp(name + "=([^;]+)"))?.[1];
}

function getToken() {
  const csrfCookie = getCookie("_xsrf");
  const csrfToken = csrfCookie ? csrfCookie : null;
  return csrfToken;
}

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));