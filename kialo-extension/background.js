chrome.sidePanel.setPanelBehavior({ openPanelOnActionClick: true }).catch((error) => console.error(error));

const CLIENT_ID = "extension"; 
const AUTH_URL = `http://localhost:1234/termit/sluzby/auth/realms/termit/protocol/openid-connect/auth?client_id=${CLIENT_ID}&response_type=token&redirect_uri=${encodeURIComponent(chrome.identity.getRedirectURL())}&scope=openid`;

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.action === "login") {
        chrome.identity.launchWebAuthFlow({
            url: AUTH_URL,
            interactive: true 
        }, (redirectUrl) => {
            if (chrome.runtime.lastError || !redirectUrl) {
                console.error("Login failed", chrome.runtime.lastError);
                sendResponse({ success: false });
                return;
            }

            const url = new URL(redirectUrl.replace("#", "?"));
            const accessToken = url.searchParams.get("access_token");

            if (accessToken) {
                chrome.storage.local.set({ access_token: accessToken }, () => {
                    sendResponse({ success: true });
                });
            } else {
                sendResponse({ success: false });
            }
        });
        return true;
    } else if (message.action === "processDiscussion") {
        chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
            const activeTab = tabs[0];
            
            if (!activeTab) return;

            chrome.tabs.sendMessage(activeTab.id, { 
                action: "createDiscussion", 
                payload: message.payload 
            });
        });
    }
});