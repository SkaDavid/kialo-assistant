import keycloak from "./config/keycloak.js";

const URL = "http://localhost:8082";

const request = async (endpoint, options) => {
    const headers = {
        "Content-type": "application/json",
        "Authorization": `Bearer ${keycloak.token}`
    }

    const response = await fetch(`${URL}${endpoint}`, {headers, ...options});
    if(response.ok){
        if (response.status === 204) {
            return null;
        }
        return response.json();
    } else {
        console.log("Error sending request");
    }
}


export const api = {
    getArgument: (id) => request(`/debate/${id}`, { "method": "GET" }),
    createArgument: (dto) => request(`/argument`, { "method": "POST", "body": JSON.stringify(dto)}),
    deleteArgument: (id) => request(`/argument/${id}`, { "method": "DELETE" }),
    updateArgument: (id, dto) => request(`/argument/${id}`, { "method": "PUT", "body": JSON.stringify(dto) }),
    testFallacy: (dto) => request("/argument/fallacy", { "method": "POST", "body": JSON.stringify(dto)}),
    getDebate: (id) => request(`/debate/${id}`, { "method": "GET" }),
    createDebate: (dto) => request("/debate", { "method": "POST", "body": JSON.stringify(dto)}),
    createAIDebate: (dto) => request("/debate/ai", { "method": "POST", "body": JSON.stringify(dto)}),
    createAIArgument: (dto) => request("/argument/ai", { "method": "POST", body: JSON.stringify(dto)}),
    importDebate: (dto) => request("/debate/import-debate", { "method": "POST", "body": JSON.stringify(dto)}),
    syncArgument: (id) => request(`/argument/sync-termit/${id}`, { "method": "POST" }),
    deleteDebate: (id) => request(`/debate/${id}`, { "method": "DELETE" }),
    deleteFallacy: (id) => request(`/argument/fallacy/${id}`, { "method": "DELETE" })
}