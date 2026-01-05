# Kialo Assistant extension prototype

This repo is a prototype application to showcase what is or isn't possible when creating an extension for Kialo.com.
The extension makes it possible to generate a list of pro and con arguments for a selected thesis. After reviewing and
selecting arguments, the extension then makes a new debate on Kialo.com and inputs generated arguments into that debate.

## Using this extension
Before using the extension, make sure everything is deployed as described in the deploy section. Log into Kialo.com
and open the extension. A panel on the right side should open. After logging in, input a short name of discussion you wish to generate
and the main thesis. After submitting, three pro and con arguments will generate. Each argument can be rejected (deleted) or 
confirmed. If an argument is confirmed, it will move to the "confirmed arguments" at the bottom of the sidepanel. When all
arguments which you wish to input into Kialo are accepted, selecting "Create new debate on Kialo" will create a new debate
and input each argument into a new discussion. This process might take maximum of 15 seconds. After the debate creation,
extension will redirect you to the new discussion.

## Extension description
The extension is created according to the MVC architectural pattern. Sidepanel.js acts as a controller. Content.js is a script which is injected into
the Kialo's page and background.js acts as a service worker. Implementing the frontend as an extension meant dealing with some hurdles. 
For examples, each request made into Kialo's backend (in this extension's case, creating a new discussion and inputting 
arguments) has to be routed from sidepanel.js through the background.js service worker into content.js. There is also manifest.json
file, which is used in chrome's extensions to specify metadata and functionality about the extension (https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/manifest.json).

## Kialo assistant backend
The backend of this application now serves two main purposes. 

Firstly, to support the extension in generating a debate. This functionality is in kialo-assistant. The app simply
receives a request with thesis and uses the OpenAI's API to generate a debate. For predictable response format, "structured model output" feature 
was used, where the app supplies a JSON schema with each request. This schema can be found in /resources/argumentSchema.json.

Secondly, to showcase implementation of a SSO access to the backend. Keycloak was used to accomplish this. When logging into the extension,
a google account can be used. User can then use any part of the backend with a single access token.


## To deploy this code:
- Generate an openAI API token and add it to your system variables under the "OPENAI_API_KEY" name
- Open "chrome://extensions" in developer mode
  - Click on "Load unpacked" and select "kialo-extension" folder
  - Copy extensions ID
- execute `docker-compose up`
- Open keycloak admin console and log in. Navigate to realms -> termit -> clients -> extension and put "https://<EXTENSION_ID>.chromiumapp.org/*" into "Valid redirect URI" and "https://<EXTENSION_ID>.chromiumapp.org/" into "Web origins".
- Log in to Kialo.com in Google Chrome and open the extension.
