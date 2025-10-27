SHELL := /bin/bash

SCRIPTS_DIR := scripts
BASE_URL ?= http://localhost:8080
OAUTH_AUTHORIZE_URL ?= http://localhost:9999/oauth/authorize
COOKIE_JAR ?= .tmp/cookies/session.jar
BALLOT_ID ?= 1
INSTITUTION_ID ?= inst-1
CANDIDATE_IDS ?= cand-1
WALLET_ADDRESS ?= 0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266

export BASE_URL
export OAUTH_AUTHORIZE_URL
export COOKIE_JAR
export BALLOT_ID
export INSTITUTION_ID
export CANDIDATE_IDS
export WALLET_ADDRESS

.PHONY: login seed seed-close token token-json vote tally clean-cookies

login:
	@$(SCRIPTS_DIR)/login.sh

seed:
	@$(SCRIPTS_DIR)/seed-default.sh

seed-close:
	@$(SCRIPTS_DIR)/seed-close.sh

token:
	@$(SCRIPTS_DIR)/issue-token.sh --token

token-json:
	@$(SCRIPTS_DIR)/issue-token.sh

vote:
	@$(SCRIPTS_DIR)/vote.sh

tally:
	@$(SCRIPTS_DIR)/tally.sh

clean-cookies:
	@rm -f $(COOKIE_JAR)
