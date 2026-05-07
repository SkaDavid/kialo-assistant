class Model {
    constructor(){
        this.proArgs = [];
        this.conArgs = [];
        this.confirmedArgs = [];
        this.name = "";
        this.thesis = "";
    }

    resetDebate(){
        this.proArgs = [];
        this.conArgs = [];
        this.confirmedArgs = [];
        this.thesis = "";
    }

    async fetchAIdebate(name, thesis){
        const fetch_token = await chrome.storage.local.get("access_token");
        const auth = `Bearer ${fetch_token.access_token}`;
        let result;
        try {
            const res = await fetch("http://localhost:8082/debate/ai", {
                method: "POST",
                headers: {
                    "Content-Type": "text/plain",
                    "Authorization": auth
                },
                body: thesis
            });

            if (!res.ok) {
                throw new Error(`Server responded with status ${res.status}`);
            }
            const response = await res.text();
            result = JSON.parse(response); 
        } catch (err) {
            console.error("Request error:", err);
            return;
        }

        this.name = name;
        this.thesis = thesis;
        this.proArgs = result.pro;
        this.conArgs = result.con; 
    }

    confirmArgument(statement, type){
        this.confirmedArgs.push({
                "type": type,
                "text": statement
            });

        if (type === "pro") {
            this.proArgs = this.proArgs.filter(item => item !== statement);
        } else {
            this.conArgs = this.conArgs.filter(item => item !== statement);
        }
    }

    removeArgument(statement, type){
        if (type === "pro") {
            this.proArgs = this.proArgs.filter(item => item !== statement);
        } else {
            this.conArgs = this.conArgs.filter(item => item !== statement);
        }
    }

    getConfirmedDebate(){
        return {
            "name": this.name,
            "thesis": this.thesis,
            "confirmedArguments": this.confirmedArgs
        }
    }
}

export {Model};
