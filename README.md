# hmpps-electronic-monitoring-create-an-order-api

[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-electronic-monitoring-create-an-order-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/hmpps-electronic-monitoring-create-an-order-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-electronic-monitoring-create-an-order-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-electronic-monitoring-create-an-order-api)
[![Docker Repository on Quay](https://img.shields.io/badge/quay.io-repository-2496ED.svg?logo=docker)](https://quay.io/repository/hmpps/hmpps-electronic-monitoring-create-an-order-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-electronic-monitoring-create-an-order-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)


## Contents

- [About this project](#about-this-project)
- [First time setup](#first-time-setup)
- [Running the service locally](#running-the-service-locally)
- [Using local API endpoints](#using-local-api-endpoints)
- [Running this service and the CEMO frontend locally](#running-this-service-and-the-cemo-frontend-locally)
- [Tests](#tests)

---


## About this project
This service validates and persists EM order forms, and submits them to the field monitoring service (FMS).

Features include:
- API endpoints that accept EM order form data from validated clients
- Validation and persistence of submitted EM order form data
- Submission of client-submitted documents to a document store for persistence
- Transformation of complete EM order forms into the models expected by FMS
- Submission of complete EM order forms to FMS API endpoints, and persistence of submission response data
- Endpoints for other EM order activities, eg. creating new versions of existing orders

This service is hosted in [Cloud Platform](https://user-guide.cloud-platform.service.justice.gov.uk/#cloud-platform-user-guide).

---


## First time setup

### Using IntelliJ IDEA
IntelliJ IDEA is a good choice of IDE for this service.  
It integrates smoothly with Kotlin, Sping Boot and Gradle,  
making the service relatively easy to run locally.

This readme assumes use of IntelliJ IDEA, but other IDEs can be used.

### 1. Clone the repo
```bash
git clone git@github.com:ministryofjustice/hmpps-electronic-monitoring-create-an-order-api.git
```

### 2. Open the project & install dependencies
Once the project is open in IntelliJ IDEA the IDE will automatically download and install dependencies.  
This may take a few minutes.

### 3. Enable pre-commit hooks for formatting and linting code
```bash
./gradlew addKtlintFormatGitPreCommitHook addKtlintCheckGitPreCommitHook
```

### 4. Configure .env
Use this automated script to create and populate an .env file in the repo's root with the required secrets:

```bash
./scripts/create-env.sh
```

The secrets can also be obtained manually using kubectl. [See this section of the Cloud Platform User Guide](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html) for guidance on accessing kubernetes resources.

### 5. Configure run
In the top-right corener of the IDE, click the text  
`HmppsElectronicMonitoringCreateAnOrderApi` to reveal a drop-down menu.  
Select `edit configurations...`

- In the `Active Profiles` text field, write `local`.
- You may also need to set the JDK to `openjdk-23` or `openjdk-21`.  
  You'll need to [download & install the required JDK](https://openjdk.org/) before the option is shown in the menu.
- Click on `Modify options` -> `Choose Environment Variables`.  
  For the 'Environment Variables' field, enter the path to your `.env` file.
- Click the `Apply` button.

---


## Running the service locally

### 1. Start the database and HMPPS Auth in Docker
```bash
docker compose pull && docker compose up --scale hmpps-electronic-monitoring-create-an-order-api=0
```

### 2. Run the service
To run the appliation:
  - Click the run button in the top-right corner of the IDE (a green 'play' symbol).
  - Alternatively you can use the command line:
    ```bash
    SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
    ```

Visit [http://localhost:8080/health](hhttp://localhost:8081/health).  
This endpoint should return data indicating that the service is running.

---


## Using local API endpoints
When [running the CEMO API and frontend together locally](#running-this-service-and-the-cemo-frontend-locally), authentication is handled by the frontend service. The UI routes the developer to the login page where they can use their credentials to authenticate. The returned JWT can be used for subsequent API requests.

However when running the API alone authentication can't be handled that way. Instead we can request an access token from the HMPPS Auth service, then provide this JWT as a bearer token when making API calls. This section explains how.

### Generating a token for a HMPPS Auth client
- Use this command to request an auth token:
  ```bash
  curl -X POST "http://localhost:8090/auth/oauth/token?grant_type=client_credentials" \ 
  -H 'Content-Type: application/json' \
  -H "Authorization: Basic $(echo -n hmpps-electronic-monitoring-cemo-ui:clientsecret | base64)"
  ```

- The returned JWT will look something like this:
  ```json
  {
    "access_token": "eyJhbGciOiJSUzI1NiIs...BAtWD653XpCzn8A",
    "token_type": "bearer",
    "expires_in": 3599,
    "scope": "read write",
    "user_name": "CEMO.INTEGRATION",
    "sub": "CEMO.INTEGRATION",
    "auth_source": "none",
    "jti": "Ptr-MIdUBDGDOl8_qqeIuNV9Wpc",
    "iss": "http://localhost:8090/auth/issuer"
  }
  ```

- Use the value of `access_token` as a Bearer Token to authenticate when calling the local API endpoints.  

- Some tools you can use to call the API:
  - [curl](https://curl.se/)
  - [Postman](https://www.postman.com/)

---


## Running this service and the CEMO frontend locally
Running [the CEMO frontend service](https://github.com/ministryofjustice/hmpps-electronic-monitoring-create-an-order) & api together locally can be useful for development purposes:

- The frontend handles authentication via HMPPS Auth
- Changes that span the frontend and backend can be implemented

### 1. Run the frontend service
Run the frontend service locally in the normal way, [following the instructions in that repo's readme](https://github.com/ministryofjustice/hmpps-electronic-monitoring-create-an-order/blob/main/#running-the-service-locally).

### 2. Configure the API to use dev instances of HMPPS Auth and the Document Store API
In the API, in the  `services` section of [application-local.yml](https://github.com/ministryofjustice/hmpps-electronic-monitoring-create-an-order-api/blob/main/src/main/resources/application-local.yml),  
comment out the localhost URLs and uncomment the dev URLs for both HMPPS Auth and Document Management.  
It should look like this:

```yaml
services:
  hmppsauth:
#   url: http://localhost:9090/auth
    url: https://sign-in-dev.hmpps.service.justice.gov.uk/auth
  document:
#   url: http://localhost:8081/
    url: https://document-api-dev.hmpps.service.justice.gov.uk/
```

### 3. Run the API
Run the API locally using the same steps as in [Running the service locally](#running-the-service-locally),  
but replace the Docker command in step 1 with:

```bash
docker compose pull && docker compose up --scale hmpps-electronic-monitoring-create-an-order-api=0 --scale hmpps-auth=0
```

### 4. Log in
Access the UI at http://localhost:3000.  
Use your HMPPS Auth credentials (dev) to sign in.  
*([See this section of the frontend readme](https://github.com/ministryofjustice/hmpps-electronic-monitoring-create-an-order/blob/main/#1-create-a-personal-client-in-the-development-environment-of-dps) for guidance on getting HMPPS Auth credentials.)*

---


## Tests

### Unit tests
```bash
./gradlew unitTest
```

### Integration tests
1. Start localstack:
```bash
  docker compose -f docker-compose-test.yml up`
  ```

2.  Run integration tests:
```bash
  ./gradlew integration
```

---
