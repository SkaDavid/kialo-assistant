const ASSISTANT_URL = "http://localhost:8082";
const KIALO_URL = "https://www.kialo.com/api/v1";

export const getToken = () => {
    const name = "_xsrf";
    const csrfCookie = document.cookie.match(new RegExp(name + "=([^;]+)"))?.[1];
    const csrfToken = csrfCookie ? csrfCookie : null;
    return csrfToken;
}

const assistantRequest = async (endpoint, options) => {
    const token = await chrome.storage.local.get("access_token");
    const headers = {
        "Content-type": "application/json",
        "Authorization": `Bearer ${token.access_token}`
    }

    const response = await fetch(`${ASSISTANT_URL}${endpoint}`, {headers, ...options});
    if(response.ok){
        return response.json();
    } else {
        throw new Error(`Request failed with status ${response.status}`);
    }
}

const contentRequest = (action, payload = {}) => {
    return new Promise((resolve, reject) => {
        chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
            const activeTab = tabs[0];
            if (!activeTab?.id) {
                return reject(new Error("Cant find active tab"));
            }

            chrome.tabs.sendMessage(activeTab.id, { action, payload }, (response) => {
                if (chrome.runtime.lastError) {
                    return reject(new Error("Tab is not reponding"));
                }
                if (response?.error) {
                    return reject(new Error(response.error));
                }
                resolve(response);
            });
        });
    });
};

const kialoRequest = async (endpoint, options) => {
    const token = getToken();
    const headers = {
        "Content-Type": "application/json",
        "X-Csrftoken": token,
    }

    const response = await fetch(`${KIALO_URL}${endpoint}`, {headers, ...options});
    if(response.ok){
        return response.json();
    } else{
        throw new Error(`Request failed with status ${response.status}`);
    }
}

export const kialoApi = {
    postArgument: (dto) => kialoRequest(`/discussiongraph`, {"method": "POST", "credentials": "include", "body": JSON.stringify(dto)}),
    getDebate: (debateId) => kialoRequest(`/discussiongraph?discussionId=${debateId}`, {"method": "GET", "credentials": "include"})
}

export const assistantApi = {
    createDebate: (dto) => assistantRequest(`/debate/import-debate`, { "method": "POST", "body": JSON.stringify(dto)}),
    getDebateInfo: (debateId) => assistantRequest(`/debate/kialo-info/${debateId}`, { "method": "GET" }),
    getArgument: (argumentId) => assistantRequest( `/argument/${argumentId}`, { "method": "GET" }),
    createArgument: (dto) => assistantRequest(`/argument`, { "method": "POST", "body": JSON.stringify(dto) }),
    updateArgument: (dto) => assistantRequest(`/argument/${dto.id}`, {"method": "PUT", "body": JSON.stringify(dto)})
}

export const contentApi = {
    getCompleteDebateInfo: async () => {
        const result = await contentRequest("getDebate");
        return result.debate;
    },
    getDebateInfo: async () => {
        const result = await contentRequest("getDebateInfo");
        return result.debateInfo;
    },
    postArgument: async (dto) => {
        const result = await contentRequest("postArgument", dto);
        return result.argument;
    },
    redirectTo: async (id) => {
        const result = await contentRequest("redirectTo", id);
        return result;
    }
};