SERCO_SECRETS=$(kubectl get secret serco-secret -n hmpps-ems-cemo-ui-dev -o json)

export SERCO_CLIENT_ID=$(jq -r '.data.SERCO_CLIENT_ID' <<< "${SERCO_SECRETS}" | base64 -d)
export SERCO_AUTH_URL=$(jq -r '.data.SERCO_AUTH_URL' <<< "${SERCO_SECRETS}" | base64 -d)
export SERCO_CLIENT_SECRET=$(jq -r '.data.SERCO_CLIENT_SECRET' <<< "${SERCO_SECRETS}" | base64 -d)
export SERCO_USERNAME=$(jq -r '.data.SERCO_USERNAME' <<< "${SERCO_SECRETS}" | base64 -d)
export SERCO_PASSWORD=$(jq -r '.data.SERCO_PASSWORD' <<< "${SERCO_SECRETS}" | base64 -d)
export SERCO_URL=$(jq -r '.data.SERCO_URL' <<< "${SERCO_SECRETS}" | base64 -d)

