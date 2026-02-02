#!/bin/sh

SECRETS=$(kubectl get secret serco-secret -o json -n hmpps-ems-cemo-ui-dev)
DOCUMENT_SECRETS=$(kubectl get secret hmpps-electronic-monitoring-create-an-order-api -o json -n hmpps-ems-cemo-ui-dev)

# Auth
echo "HMPPS_AUTH_URL=https://sign-in-dev.hmpps.service.justice.gov.uk/auth" > .env

# Database
echo "DB_SERVER=localhost:5432" >> .env
echo "DB_NAME=postgres" >> .env
echo "DB_USER=postgres" >> .env
echo "DB_PASS=postgres" >> .env

# Document Management API
echo "DOCUMENT_MANAGEMENT_URL=http://localhost:8081/" >> .env
echo "CLIENT_ID=$(jq -r '.data.API_CLIENT_ID' <<< "${DOCUMENT_SECRETS}" | base64 -d)" >> .env
echo "CLIENT_SECRET=$(jq -r '.data.API_CLIENT_SECRET' <<< "${DOCUMENT_SECRETS}" | base64 -d)" >> .env

# FMS Integration
echo "SERCO_AUTH_URL=$(jq -r '.data.SERCO_AUTH_URL' <<< "${SECRETS}" | base64 -d)" >> .env
echo "SERCO_CLIENT_ID=$(jq -r '.data.SERCO_CLIENT_ID' <<< "${SECRETS}" | base64 -d)" >> .env
echo "SERCO_CLIENT_SECRET=$(jq -r '.data.SERCO_CLIENT_SECRET' <<< "${SECRETS}" | base64 -d)" >> .env
echo "SERCO_USERNAME=$(jq -r '.data.SERCO_USERNAME' <<< "${SECRETS}" | base64 -d)" >> .env
echo "SERCO_PASSWORD=$(jq -r '.data.SERCO_PASSWORD' <<< "${SECRETS}" | base64 -d)" >> .env
echo "SERCO_URL=$(jq -r '.data.SERCO_URL' <<< "${SECRETS}" | base64 -d)" >> .env

# Feature Flags
echo "CP_PROCESSING_ENABLED=false" >> .env
echo "CP_FMS_INTEGRATION_ENABLED=false" >> .env
echo "CEMO_FMS_INTEGRATION_ENABLED=true" >> .env
