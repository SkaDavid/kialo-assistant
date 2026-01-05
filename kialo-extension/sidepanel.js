import {View} from "./view.js"
import {Model} from "./model.js"

class Controller{
    constructor(model, view){
        this.model = model;
        this.view = view;
    
        this.init();
    }

    init(){
        this.view.initialiseButtons(
            this.handleLogin.bind(this), 
            this.handleLogout.bind(this),
            this.handleAIdebateCreation.bind(this)
        );
        chrome.storage.local.get(["access_token"], (result) => {
            if (result.access_token) {
                this.view.showContent();
            } else {
                this.view.showLogin();
            }
        });

    }
    
    handleLogin(){
        chrome.runtime.sendMessage({ action: "login" }, (response) => {
            if (response && response.success) {
                this.view.showContent();
            }
        });
    }

    handleLogout(){
        chrome.storage.local.remove("access_token", () => {
            this.view.showLogin();
            this.view.clearAIdebate();
        });
    }

    handleSendToKialo(){
        const debateInfoDto = this.model.getConfirmedDebate();
        chrome.runtime.sendMessage({ action: "processDiscussion", payload: debateInfoDto });
    }

    handleCancelDebate(){
        this.model.resetDebate();
        this.view.clearAIdebate();
    }

    handleConfirmArgument(statement, type){
        this.model.confirmArgument(statement, type);
        this.view.renderAIdebate(this.model.thesis, this.model.proArgs, this.model.conArgs, this.model.confirmedArgs, 
            this.handleCancelDebate.bind(this), this.handleSendToKialo.bind(this), this.handleConfirmArgument.bind(this), this.handleRejectArgument.bind(this));
    }

    handleRejectArgument(statement, type){
        this.model.removeArgument(statement, type);
        this.view.renderAIdebate(this.model.thesis, this.model.proArgs, this.model.conArgs, this.model.confirmedArgs, 
            this.handleCancelDebate.bind(this), this.handleSendToKialo.bind(this), this.handleConfirmArgument.bind(this), this.handleRejectArgument.bind(this));
    }


    async handleAIdebateCreation(e){
        e.preventDefault();
        const name = this.view.getNameValue();
        const thesis = this.view.getThesisValue();

        this.view.showLoading(true);
        await this.model.fetchAIdebate(name, thesis);
        this.view.showLoading(false);
        this.view.renderAIdebate(thesis, this.model.proArgs, this.model.conArgs, [], 
            this.handleCancelDebate.bind(this), this.handleSendToKialo.bind(this), this.handleConfirmArgument.bind(this), this.handleRejectArgument.bind(this));
    }
}

document.addEventListener("DOMContentLoaded", ()=>{
    const app = new Controller(new Model(), new View());
})