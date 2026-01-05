class View {
    constructor(){
        this.loginSection = document.getElementById("login-section");
        this.contentSection = document.getElementById("content-section");
        this.btnLogin = document.getElementById("btn-login");
        this.btnLogout = document.getElementById("btn-logout");
        this.btnAI = document.getElementById("ai");
        this.body = document.querySelector("body");
        this.thesisInput = document.getElementById("thesisInput");
        this.nameInput = document.getElementById("nameInput");
        this.generatedContainer = document.getElementById("generatedDebateContainer");
    }

    initialiseButtons(handleLogin, handleLogout, handleAIdebateCreation){
        this.btnLogin.addEventListener("click", handleLogin);
        this.btnLogout.addEventListener("click", handleLogout);
        this.btnAI.addEventListener("click", handleAIdebateCreation);
    }

    showLogin(){
        this.loginSection.classList.remove("hidden");
        this.contentSection.classList.add("hidden");
    }

    showContent() {
        this.loginSection.classList.add("hidden");
        this.contentSection.classList.remove("hidden");
    }

    renderAIdebate(thesis, proArgs, conArgs, confirmedArgs, handleCancelDebate, handleSendToKialo, handleConfirmArgument, handleRejectArgument){
        this.clearAIdebate();

        const h2 = document.createElement("h2");
        h2.innerText = `Thesis: ${thesis}`
        this.generatedContainer.append(h2);

        this.createAIarguments(proArgs, "pro", handleConfirmArgument, handleRejectArgument);
        this.createAIarguments(conArgs, "con", handleConfirmArgument, handleRejectArgument);

        const h3 = document.createElement("h3");
        h3.innerText = "Confirmed arguments"
        this.generatedContainer.append(h3);

        this.createAIarguments(confirmedArgs, "confirmed", handleConfirmArgument, handleRejectArgument)

        const cancelBtn = document.createElement("button");
        cancelBtn.innerText = "Cancel";
        cancelBtn.addEventListener("click", handleCancelDebate)

        const sendToKialoBtn = document.createElement("button");
        sendToKialoBtn.innerText = "Create new debate on Kialo";
        sendToKialoBtn.addEventListener("click", handleSendToKialo);

        this.generatedContainer.append(cancelBtn, sendToKialoBtn)
        
        this.body.append(this.generatedContainer);
    }

    clearAIdebate(){
        this.generatedContainer.innerHTML = "";
    }

    showLoading(isLoading){
        if(isLoading){
            const message = document.createElement("p");
            message.innerText = "Waiting for a reply...";
            message.id = "loadingMessage";
            this.generatedContainer.append(message);
        } else{
            const message = document.getElementById("loadingMessage");
            if(message){
                message.remove();
            }
        }
    }

    createAIarguments(statements, classname, handleConfirmArgument, handleRejectArgument){
        if(classname === "confirmed"){
            statements.forEach(statement => {
                const article = document.createElement("article");
                article.classList.add(classname);
                article.classList.add(statement.type);
                article.classList.add("argument");

                const text = document.createElement("p");
                text.innerText = statement.text;

                article.append(text);
                this.generatedContainer.append(article);
            })
                
        } else{
            statements.forEach(statement => {
                const article = document.createElement("article");
                article.classList.add(classname);
                article.classList.add("argument");

                const text = document.createElement("p");
                text.innerText = statement;

                const rejectBtn = document.createElement("button");
                rejectBtn.classList.add("reject");
                rejectBtn.innerText = "Reject";
                rejectBtn.addEventListener("click", () => handleRejectArgument(statement, classname))

                const acceptBtn = document.createElement("button");
                acceptBtn.classList.add("accept");
                acceptBtn.innerText = "Accept";
                acceptBtn.addEventListener("click", () => handleConfirmArgument(statement, classname))

                article.append(text, rejectBtn, acceptBtn);
                this.generatedContainer.append(article);
            });    
        }
    }

    getThesisValue(){
        return this.thesisInput.value;
    }

    getNameValue(){
        return this.nameInput.value;
    }
}

export {View};