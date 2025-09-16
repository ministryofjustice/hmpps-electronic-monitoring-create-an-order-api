# hmpps-electronic-monitoring-create-an-order-api

[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-electronic-monitoring-create-an-order-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/hmpps-electronic-monitoring-create-an-order-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-electronic-monitoring-create-an-order-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-electronic-monitoring-create-an-order-api)
[![Docker Repository on Quay](https://img.shields.io/badge/quay.io-repository-2496ED.svg?logo=docker)](https://quay.io/repository/hmpps/hmpps-electronic-monitoring-create-an-order-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-electronic-monitoring-create-an-order-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)

## Contents

- [About this project](#about-this-project)
- [Get started](#get-started)
    - [Using IntelliJ IDEA](#using-intellij-idea)
- [Usage](#usage)
    - [Running the application locally](#running-the-application-locally)
      - [Calling endpoints](#calling-endpoints)
        - [Generate a token for a HMPPS Auth client](#generate-a-token-for-a-hmpps-auth-client)
      - [Running the application locally with the UI](#running-both-the-ui-and-the-api-locally)

---


## About this project
This service validates and persists EM order forms, and submits them to the field monitoring service (FMS).

Features include:
- API endpoints that accept EM order form data from validated clients
- Validation and persistance of submitted EM order form data
- Transformation of complete EM order forms into the models expected by FMS
- Submission of complete EM order forms to FMS API endpopints, and persistance of submission response data
- Endpoints for other EM order activities such as creating new versions of existing orders.

This service is hosted in MoJ's [Cloud Platform](https://user-guide.cloud-platform.service.justice.gov.uk/#cloud-platform-user-guide).

---


## First time setup

### Using IntelliJ IDEA

IntelliJ IDEA is a good choice of IDE for this Kotlin repo.  
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

---


## Running the service locally

To run the service using IntelliJ IDEA:

### 1. Start the database and HMPPS Auth in Docker
    ```bash
    docker compose pull && docker compose up --scale hmpps-electronic-monitoring-create-an-order-api=0`
    ```

### 2. Configure .env
1. Create a .env file in the root level of the repository.

2. Use this automated script to populate the .env file with the required secrets:
    ```bash
    ./scripts/create-env.sh
    ```

The secrets can also be obtained manually using kubectl.  
[See this section of the Cloud Platform User Guide](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html) for guidance on accessing kubernetes resources.

### 3. Configure run
In the top-right corener of the IDE,  
click the text `HmppsElectronicMonitoringCreateAnOrderApi` to reveal a drop-down menu.  
Select `edit configurations...`

- In the `Active Profiles` text field, write `local`.
- You may also need to set the JDK to `openjdk-23` or `openjdk-21`.  
  You may need to download the required JDK before the option is shown in the menu.
- Click on `Modify options` -> `Choose Environment Variables`.  
  For the 'Environment Variables' field, enter the path to your `.env` file.
- Click the `Apply` button.

### 4. Run the service
To run the appliation using the UI:
1. Click the run button in the top-right corner of the IDE (a green 'play' symbol).  
2. Visit [http://localhost:8080/health](hhttp://localhost:8081/health).  
This endpoint should return data indicating that the service is running.

Or, to run the application using the command line:

1. Set up environment variables:
    ```bash
    ./scripts/create-env.sh
    ```

2. Run the service:
    ```bash
    SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
    ```

3. Visit [http://localhost:8080/health](hhttp://localhost:8081/health).  
This endpoint should return data indicating that the service is running.


## Calling endpoints

As part of getting the HMPPS Auth service running locally, 
[the in-memory database is seeded with data including a number of clients](https://github.com/ministryofjustice/hmpps-auth/blob/main/src/main/resources/db/dev/data/auth/V900_0__clients.sql). 
A client can have different permissions i.e. read, write, reporting, although strangely the column name is called `​​autoapprove`.

If you wish to call an endpoint of the API, an access token must be provided that is generated from the HMPPS Auth service.

## Generate a token for a HMPPS Auth client

```bash
curl -X POST "http://localhost:8090/auth/oauth/token?grant_type=client_credentials" \ 
-H 'Content-Type: application/json' \
-H "Authorization: Basic $(echo -n hmpps-electronic-monitoring-cemo-ui:clientsecret | base64)"
```

This uses the client ID: `hmpps-electronic-monitoring-cemo-ui` and the client secret: `clientsecret`. A number of seeded
clients use the same client secret.

A JWT token is returned as a result, it will look something like this:

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

Using the value of `access_token`, you can call this API using it as a Bearer Token.

There are a couple of options for doing so such as [curl](https://curl.se/) or
[Postman](https://www.postman.com/).

#### Running both the UI And the API locally

The [Create an Electronic Monitoring Order UI](https://github.com/ministryofjustice/hmpps-electronic-monitoring-create-an-order) uses HMPPS Auth and Document api in development (rather than locally with Docker).

To interact with the API using the front end, with both running locally:
1. In the application-local.yml file in the API, comment out the localhost URLs and uncomment the dev URLs for both HMPPS Auth and Document Management
2. Run the UI locally, using [the UI documentation](https://github.com/ministryofjustice/hmpps-electronic-monitoring-create-an-order)
3. Run the API locally using the same steps outlined in [get started](#get-started), but replacing the command in step 1 with `docker compose pull && docker compose up --scale hmpps-electronic-monitoring-create-an-order-api=0 --scale hmpps-auth=0` to start a docker instance of the database but not HMPPS Auth.

There is no need to generate a HMPPS Auth token, as authentication is handled by the front end. Valid personal credentials will be needed to sign in via the UI.

#### Running integration test locally

1. For running integration tests locally, start localstack of SQS queues by:

   `docker compose -f docker-compose-test.yml up`
2.  Using command line to run integration test:

    `./gradlew integration`
