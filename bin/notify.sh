#!/bin/bash

# Helper script to send messages to slack channel.

#################
# CONFIGURATION #
#################

# webhook to slack channel typically starting with https://hooks.slack.com/services/
WEBHOOK=

# context of the software e.g. PROD/TEST/DEV
CTX=DEV

########
# MAIN #
########

MESSAGE="https//kbss.felk.cvut.cz/lkpr successfully deployed."
ICON=":tada:"

curl -X POST -H 'Content-type: application/json' --data "{\"channel\": \"#d2030-lkpr-dev\", \"username\": \"[$CTX] Deployment\", \"text\": \"$MESSAGE\", \"icon_emoji\": \"$ICON\"}" $WEBHOOK
