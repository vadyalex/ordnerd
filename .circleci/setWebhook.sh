#!/usr/bin/bash

ORDNERD_APP_HOST="$1"
WEBHOOK_UID="$2"
TELEGRAM_TOKEN="$3"

curl -F "url=https://${ORDNERD_APP_HOST}/bot/telegram/${WEBHOOK_UID}" "https://api.telegram.org/bot${TELEGRAM_TOKEN}/setWebhook"