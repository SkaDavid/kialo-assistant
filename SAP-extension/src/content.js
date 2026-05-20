import { kialoApi, getToken, assistantApi } from "./api";

let debateData = null;
let debateId = null;

chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === "getDebate") {
        handleGetDebate(sendResponse);
        return true; 
    } else if (request.action === "getDebateInfo") {
        handleGetDebateInfo(sendResponse);
        return true;
    } else if (request.action === "postArgument"){
        handlePostArgument(request.payload, sendResponse);
        return true;
    } else if (request.action === "redirectTo") {
        handleRedirectTo(request.payload, sendResponse);
        return true;
    }
})



const handleGetDebate = async (sendResponse) => {
    try {
        const data = await getDebate();
        sendResponse({ debate: data });
    } catch (err) {
        sendResponse({ error: err.message });
    }
}

const handleGetDebateInfo = async (sendResponse) => {
    try{
        const info = await getDebateInfo();
        sendResponse({ debateInfo: info });
    } catch (err) {
        sendResponse({ error: err.message });
    }
}

const handlePostArgument = async (data, sendResponse) => {
    try{
        const body = {
            "parentLocationPath": await findLocations(data.parentId),
            "relation": data.argumentType === "PRO" ? 1 : -1,
            "text": data.argumentText,
            "isSuggestion": false,
            "markdownVersion": 1
        }
        const argument = await kialoApi.postArgument(body);
        sendResponse({ argument: argument });
    } catch (err) {
        sendResponse({ error: err.message });
    }
}

const handleRedirectTo = (argumentId, sendResponse) => {
    try {
        const debateId = getDebateId();
        
        const newUrl = `${window.location.origin}/${debateId}.${argumentId}`;
        
        console.log("Redirecting tab to:", newUrl);
        
        window.location.href = newUrl;
        
        sendResponse({ success: true });
    } catch (err) {
        sendResponse({ error: err.message });
    }
};


const getDebateInfo = async () => {
    return {
        debateId: getDebateId(),
        argumentVersions: await getArgumentVersions(),
    }
}

const getDebate = async () => {
    const csrfToken = getToken();
    const debateId = getDebateId();
    const topic = getTopic();

    const debateArguments = await getDebateData();

    return {
        debateId: debateId,
        topic: topic,
        arguments: parseArguments(debateArguments, debateId)
    };
}

const getArgumentVersions = async () => {
    const data = await getDebateData();
    const claims = data.claims;
    const locations = data.locations; 
    return claims.map(claim => ({
        id: claim.id.split(".")[1], 
        version: claim.version, 
        text: claim.text, 
        type: locations.find(location => location.targetId == claim.id).relation == 1 ? "PRO" : "CON",
        parent: locations.find(location => location.targetId == claim.id).parentId
    }))
}

const getDebateId = () => {
    const element = document.querySelector(".hidden-claim-card-text");
    const id = element.id.split("-").pop().split(".")[0];
    return id;
    
}

const getTopic = () => {
    return document.querySelector("h1.topbar-title__discussion-title").textContent;
}



const findLocations = async (parentClaimsId) => {
    const data = await getDebateData(); 
    const locations = data.locations;
    let result = [];
    
    const parentLocation = locations.find(location => location.targetId.split(".")[1] == parentClaimsId);
    result.push(parentLocation.id);
    let currentId = parentLocation.parentId;

    console.log(result);
    console.log(currentId);
    console.log(parentLocation)
    
    while(currentId != null){
        const currentLocation = locations.find(location => location.targetId == currentId);
        console.log("currentLocation:")
        console.log(currentLocation)
        result.push(currentLocation.id);
        currentId = currentLocation.parentId;
    }
    return result.reverse();
}




const parseArguments = (rawArguments, debateId) => {
    const claims = rawArguments.claims;
    const locations = rawArguments.locations;
    const parsedArguments = [];

    claims.forEach((claim) => {
        if(claim.id == debateId + ".0"){
            return;
        }
        const claimsLocation = locations.find(location => location.targetId == claim.id);

        let claimsType;
        if(claimsLocation.relation == 0){
            claimsType = "THESIS";
        } else{
            claimsLocation.relation == 1 ? claimsType = "PRO" : claimsType = "CON";
        }

        const argumentId = claim.id.split(".")[1];
        const parentId = claimsLocation.parentId.split(".")[1];

        const newArgument = {
            id: argumentId,
            text: claim.text,
            type: claimsType, 
            parent: parentId,
            version: claim.version
        }
        parsedArguments.push(newArgument);
    });

    console.log(parsedArguments);
    return parsedArguments;
}

const getDebateData = async () =>{
    if(!debateData){
        debateData = await kialoApi.getDebate(getDebateId());
    }
    return debateData;
}