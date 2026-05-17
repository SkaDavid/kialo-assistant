import { kialoApi, getToken } from "./api";

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
            "parentLocationPath": findLocations(data.parentId),
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
        argumentVersions: getArgumentVersions(),
    }
}

const getDebate = async () => {
    const csrfToken = getToken();
    const debateId = getDebateId();
    const topic = getTopic();

    /* volani kiala, viz nize */ 
    /* nacacheuj si to sem nekam */
    const debateArguments = getOfflineDebate();

    return {
        debateId: debateId,
        topic: topic,
        arguments: parseArguments(debateArguments, debateId)
    };
}

const getArgumentVersions = () => {
    /* kialo request nize */
    /* nacacheuj si to sem nekam */
    const claims = getOfflineDebate().claims;
    const locations = getOfflineDebate().locations; 
    return claims.map(claim => ({id: claim.id.split(".")[1], version: claim.version, text: claim.text, type: locations.find(location => location.targetId == claim.id).relation == 1 ? "PRO" : "CON"}))
}

const getDebateId = () => {
    return window.location.href.substring(window.location.href.lastIndexOf('/') + 1, window.location.href.lastIndexOf('/') + 6);
}

const getTopic = () => {
    return document.querySelector("h1.topbar-title__discussion-title").textContent;
}



const findLocations = (parentClaimsId) => {
    /* cache */
    const locations = getOfflineDebate().locations;
    console.log(locations);
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
    return result.reverse();;
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





















































/* volani kiala*/
/*   try {
    const response = await fetch(
      `https://www.kialo.com/api/v1/discussiongraph?discussionId=${debateId}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "X-Csrftoken": csrfToken,
        },
        credentials: "include",
      }
    );

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

    const data = await response.json();
    console.log("Data z Kialo API:", data);
    return data;
  } catch (err) {
    console.error("Fetch failed: ", err);
    throw err;
  } */









const getOfflineDebate = () => {
    return {
    "claims": [
        {
            "id": "72645.0",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1756664386256,
            "version": 1,
            "text": ""
        },
        {
            "id": "72645.3",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1756664386269,
            "version": 3,
            "text": "Je Rusko agresorem v rusko-ukrajinské válce?"
        },
        {
            "id": "72645.5",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1756664667428,
            "version": 1,
            "text": "Anexe Krymu v roce 2014 a invaze v roce 2022 porušují Budapešťské memorandum, v němž se Rusko zavázalo respektovat územní celistvost Ukrajiny."
        },
        {
            "id": "72645.7",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1756664705148,
            "version": 2,
            "text": "Cílem vojenské operace je chránit ruskou menšinu na Ukrajině, která čelí diskriminaci a násilí.."
        },
        {
            "id": "72645.9",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1756664790880,
            "version": 2,
            "text": "Zahájení vojenské akce bylo jednostranné. Ruská vojska překročila hranici Ukrajiny bez jejího souhlasu a předchozí eskalace"
        },
        {
            "id": "72645.11",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455066197,
            "version": 2,
            "text": "Soud v Haagu rozhodnul, že Rusko agresorem není. zdroj - [facebook.com](https://www.facebook.com/watch/?v=230723426786445)"
        },
        {
            "id": "72645.13",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455186523,
            "version": 1,
            "text": "Soudci OSN odmítli obvinění Ukrajiny, že je Moskva zodpovědná za sestřelení letu MH17 v roce 2014."
        },
        {
            "id": "72645.15",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455254763,
            "version": 1,
            "text": "Soud sice přímo nepodpořil tvrzení Ukrajiny, že Rusko je zodpovědné za sestřelení MH17, připomnělo ale že ruská podpora nespočívala dodávání zbraní nebo výcviku ale v podpoře finanční. zdroj - [irozhlas.cz](https://www.irozhlas.cz/zpravy-domov/overovna-mezinarodni-soudni-dvur-se-postavil-za-rusko-tvrdi-e-maily-rozsudek-ale_2408040500_kma)"
        },
        {
            "id": "72645.17",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455289562,
            "version": 1,
            "text": "Mezinárodní soudní dvůr se ruskou agresí nezabýval, zpráva odkazuje na jiné žaloby, která Ukrajina proti Rusku podala už v roce 2017. Zdroj - [manipulatori.cz](https://manipulatori.cz/rusko-je-agresor-soud-v-haagu-o-tom-nerozhodoval/)"
        },
        {
            "id": "72645.19",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455313201,
            "version": 1,
            "text": "Rusko podle soudu porušilo prvky protiteroristické smlouvy OSN. Zdroj - [irozhlas.cz](https://www.irozhlas.cz/zpravy-domov/overovna-mezinarodni-soudni-dvur-se-postavil-za-rusko-tvrdi-e-maily-rozsudek-ale_2408040500_kma)"
        },
        {
            "id": "72645.21",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455339715,
            "version": 1,
            "text": "Rusko podle rozsudku ICJ porušilo předběžné rozhodnutí z roku 2017, které apelovalo, aby situaci ani jedna ze stran dále neeskalovala. Zdroj - [irozhlas.cz](https://www.irozhlas.cz/zpravy-domov/overovna-mezinarodni-soudni-dvur-se-postavil-za-rusko-tvrdi-e-maily-rozsudek-ale_2408040500_kma)"
        },
        {
            "id": "72645.23",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758456433294,
            "version": 1,
            "text": "Memorandum mělo zajistit, že výměnou za garanci územní celistvosti se Ukrajina vzdá svých jaderných zbraní. To Rusko porušilo."
        },
        {
            "id": "72645.25",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758456484196,
            "version": 8,
            "text": "Memorandum nebylo mezinárodní smlouvou, ale politickým prohlášením. Právní závaznost je sporná."
        },
        {
            "id": "72645.27",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758456648071,
            "version": 2,
            "text": "Text Budapešťského memoranda používá slovo \"assurances\", ne \"guarantees\". zdroj - [rusi.org](https://www.rusi.org/explore-our-research/publications/commentary/budapest-memorandum-ukraine-compact-conundrum-guarantees?utm_source=chatgpt.com)"
        },
        {
            "id": "72645.29",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758457216930,
            "version": 7,
            "text": "Připojení Krymu bylo legitimní, rozhodlo o tom demokratické Krymské referendum v roce 2014, kde 96 procent voličů volilo pro připojení k Rusku"
        },
        {
            "id": "72645.31",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758457287712,
            "version": 1,
            "text": "Účast u referenda byla 83 %, nejsou tak pochyby o vůli lidu."
        },
        {
            "id": "72645.33",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758457371241,
            "version": 1,
            "text": "Referendum bylo organizováno během krize a pod vojenskou okupací ruských jednotek."
        },
        {
            "id": "72645.35",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758457530514,
            "version": 7,
            "text": "Mezinárodní právo uznává právo na sebeurčení, ale neumožňuje jednostrannou změnu hranic státu bez souhlasu vlády."
        },
        {
            "id": "72645.37",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758457976072,
            "version": 2,
            "text": "Většinou neruských států je referendum považované za zfalšované. Krym jako Ruské území neuznává většina zemí OSN. zdroj - [cs.wikipedia.org](https://cs.wikipedia.org/wiki/Krymsk%C3%A9_referendum_\\(2014\\))"
        },
        {
            "id": "72645.39",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758458115457,
            "version": 8,
            "text": "Existují spekulace, že referendum bylo zfalšované. Např. v Sevastopolu podle výsledků referenda muselo hlasovat o 233 procent více lidí, než kolik jich ve městě žilo rok před referendem. zdroj - [ct24.ceskatelevize.cz](https://ct24.ceskatelevize.cz/clanek/svet/krym-vyhlasil-samostatnost-bylo-ale-referendum-v-poradku-328088)"
        },
        {
            "id": "72645.41",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758458236074,
            "version": 6,
            "text": "Podle Marka Buffalase referenda zúčastnili zahraniční pozorovatelé z 21 států, včetně tří českých pozorovatelů \\(Milan Šarapatka, Stanislav Berkovec a Miloslav Soušek\\). Např. Soušek prohlásil, že \"lidé chodí v klidu, jsou rádi a všechno je dobré\". Zdroj - [cs.wikipedia.org](https://cs.wikipedia.org/wiki/Krymsk%C3%A9_referendum_\\(2014\\))"
        },
        {
            "id": "72645.43",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758458640865,
            "version": 2,
            "text": "Rusko reagovalo na hrozbu rozšiřování NATO směrem k ruským hranicím."
        },
        {
            "id": "72645.45",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758458994796,
            "version": 1,
            "text": "Konflikt začal mnohem dříve, a byl eskalací dlouhodobého konfliktu v Donbase."
        },
        {
            "id": "72645.47",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758459155768,
            "version": 4,
            "text": "Překročení mezinárodně uznaných hranic Ukrajiny ruskou armádou je porušením státní suverenity, která je zakotvena v Chartě OSN. Rusko jednalo bez souhlasu ukrajinské vlády a bez mandátu OSN. zdroj - [Charta osn, article 2.4.](https://www.un.org/en/about-us/un-charter/full-text)"
        },
        {
            "id": "72645.53",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1768227921404,
            "version": 6,
            "text": "pravda"
        },
        {
            "id": "72645.54",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1768227960553,
            "version": 2,
            "discussionLinkTo": "74050.47",
            "text": "Překročení mezinárodně uznaných hranic Ukrajiny ruskou armádou je porušením státní suverenity, která je zakotvena v Chartě OSN. Rusko jednalo bez souhlasu ukrajinské vlády a bez mandátu OSN. zdroj - [Charta osn, article 2.4.](https://www.un.org/en/about-us/un-charter/full-text)"
        },
        {
            "id": "72645.60",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1768227960554,
            "version": 1,
            "text": "miluju nové texty, opravdu moc."
        },
        {
            "id": "72645.62",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1768227960554,
            "version": 1,
            "text": "miluju nové texty, opravdu moc, moc, moc, moc."
        },
        {
            "id": "72645.64",
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1768227960554,
            "version": 1,
            "text": "NOVY TEST"
        }  
    ],
    "locations": [
        {
            "id": "72645.1",
            "targetId": "72645.0",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1756664386256,
            "parentId": null,
            "relation": null
        },
        {
            "id": "72645.2",
            "targetId": "72645.3",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1756664386269,
            "parentId": "72645.0",
            "relation": 0
        },
        {
            "id": "72645.4",
            "targetId": "72645.5",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1756664667429,
            "parentId": "72645.3",
            "relation": 1
        },
        {
            "id": "72645.6",
            "targetId": "72645.7",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1756664705150,
            "parentId": "72645.3",
            "relation": -1
        },
        {
            "id": "72645.8",
            "targetId": "72645.9",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1756664790881,
            "parentId": "72645.3",
            "relation": 1
        },
        {
            "id": "72645.10",
            "targetId": "72645.11",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455066199,
            "parentId": "72645.3",
            "relation": -1
        },
        {
            "id": "72645.12",
            "targetId": "72645.13",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455186525,
            "parentId": "72645.11",
            "relation": 1
        },
        {
            "id": "72645.14",
            "targetId": "72645.15",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455254765,
            "parentId": "72645.13",
            "relation": -1
        },
        {
            "id": "72645.16",
            "targetId": "72645.17",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455289564,
            "parentId": "72645.11",
            "relation": -1
        },
        {
            "id": "72645.18",
            "targetId": "72645.19",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455313203,
            "parentId": "72645.11",
            "relation": -1
        },
        {
            "id": "72645.20",
            "targetId": "72645.21",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758455339717,
            "parentId": "72645.11",
            "relation": -1
        },
        {
            "id": "72645.22",
            "targetId": "72645.23",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758456433296,
            "parentId": "72645.5",
            "relation": 1
        },
        {
            "id": "72645.24",
            "targetId": "72645.25",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758456484198,
            "parentId": "72645.5",
            "relation": -1
        },
        {
            "id": "72645.26",
            "targetId": "72645.27",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758456648073,
            "parentId": "72645.5",
            "relation": -1
        },
        {
            "id": "72645.28",
            "targetId": "72645.29",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758457216931,
            "parentId": "72645.5",
            "relation": -1
        },
        {
            "id": "72645.30",
            "targetId": "72645.31",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758457287713,
            "parentId": "72645.29",
            "relation": 1
        },
        {
            "id": "72645.32",
            "targetId": "72645.33",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758457371243,
            "parentId": "72645.29",
            "relation": -1
        },
        {
            "id": "72645.34",
            "targetId": "72645.35",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758457530516,
            "parentId": "72645.29",
            "relation": -1
        },
        {
            "id": "72645.36",
            "targetId": "72645.37",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758457976074,
            "parentId": "72645.29",
            "relation": -1
        },
        {
            "id": "72645.38",
            "targetId": "72645.39",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758458115459,
            "parentId": "72645.31",
            "relation": -1
        },
        {
            "id": "72645.40",
            "targetId": "72645.41",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758458236076,
            "parentId": "72645.31",
            "relation": 1
        },
        {
            "id": "72645.42",
            "targetId": "72645.43",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758458640867,
            "parentId": "72645.9",
            "relation": -1
        },
        {
            "id": "72645.44",
            "targetId": "72645.45",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758458994798,
            "parentId": "72645.9",
            "relation": -1
        },
        {
            "id": "72645.46",
            "targetId": "72645.47",
            "version": 2,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1758459155769,
            "parentId": "72645.9",
            "relation": 1,
            "isNonSuggestionArchived": true
        },
        {
            "id": "72645.52",
            "targetId": "72645.53",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1768227921406,
            "parentId": "72645.47",
            "relation": 1
        },
        {
            "id": "72645.55",
            "targetId": "72645.54",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1768227960563,
            "parentId": "72645.9",
            "relation": 1
        },
        {
            "id": "72645.59",
            "targetId": "72645.60",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1768227960563,
            "parentId": "72645.47",
            "relation": 1
        },
        {
            "id": "72645.63",
            "targetId": "72645.62",
            "version": 4,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1768227960563,
            "parentId": "72645.47",
            "relation": 1
        },{
            "id": "72645.67",
            "targetId": "72645.64",
            "version": 1,
            "isOrigin": true,
            "authorIdentityId": "68b49242346b6dff0e533982",
            "created": 1768227960563,
            "parentId": "72645.62",
            "relation": 1
        }

    ],
    "touchedClaimOrLocationIds": [
        "72645.0",
        "72645.3",
        "72645.5",
        "72645.7",
        "72645.9",
        "72645.11",
        "72645.13",
        "72645.15",
        "72645.17",
        "72645.19",
        "72645.21",
        "72645.23",
        "72645.25",
        "72645.27",
        "72645.29",
        "72645.31",
        "72645.33",
        "72645.35",
        "72645.37",
        "72645.39",
        "72645.41",
        "72645.43",
        "72645.45",
        "72645.47",
        "72645.53",
        "72645.54",
        "72645.1",
        "72645.2",
        "72645.4",
        "72645.6",
        "72645.8",
        "72645.10",
        "72645.12",
        "72645.14",
        "72645.16",
        "72645.18",
        "72645.20",
        "72645.22",
        "72645.24",
        "72645.26",
        "72645.28",
        "72645.30",
        "72645.32",
        "72645.34",
        "72645.36",
        "72645.38",
        "72645.40",
        "72645.42",
        "72645.44",
        "72645.46",
        "72645.52"
    ]
  }
}; 