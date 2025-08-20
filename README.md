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


## About this project

An API used by the [Create an Electronic Monitoring Order UI](https://github.com/ministryofjustice/hmpps-electronic-monitoring-create-an-order), 
a service that allows users to create electronic monitoring orders.
It's built using [Spring Boot](https://spring.io/projects/spring-boot/) and [Kotlin](https://kotlinlang.org/)
as well as the following technologies for its infrastructure:
- [AWS](https://aws.amazon.com/) - Services utilise AWS features through Cloud Platform.
- [CircleCI](https://circleci.com/developer) - Used for our build platform, responsible for executing workflows to
  build, validate, test and deploy our project.
- [Cloud Platform](https://user-guide.cloud-platform.service.justice.gov.uk/#cloud-platform-user-guide) - Ministry of
  Justice's (MOJ) cloud hosting platform built on top of AWS which offers numerous tools such as logging, monitoring and
  alerting for our services.
- [Docker](https://www.docker.com/) - The API is built into docker images which are deployed to our containers.
- [Kubernetes](https://kubernetes.io/docs/home/) - Creates 'pods' to host our environment. Manages auto-scaling, load
  balancing and networking to our application.

## Get started

### Using IntelliJ IDEA

When using an IDE like [IntelliJ IDEA](https://www.jetbrains.com/idea/), getting started is very simple as it will
handle installing the required Java SDK and [Gradle](https://gradle.org/) versions. The following are the steps for
using IntelliJ but other IDEs will prove similar.

1. Clone the repo.

```bash
git clone git@github.com:ministryofjustice/hmpps-electronic-monitoring-create-an-order-api.git
```

2. Launch IntelliJ and open the `hmpps-electronic-monitoring-create-an-order-api` project by navigating to the location 
of the repository.

Upon opening the project, IntelliJ will begin downloading and installing necessary dependencies which may take a few
minutes.

3. Enable pre-commit hooks for formatting and linting code.

```bash
./gradlew addKtlintFormatGitPreCommitHook addKtlintCheckGitPreCommitHook
```

## Usage

### Running the application locally

To run the application using IntelliJ:

1. Run `docker compose pull && docker compose up --scale hmpps-electronic-monitoring-create-an-order-api=0`
, which will just start a docker instance of the database and HMPPS Auth.
2. Create a .env file in the root level of the repository with the following contents. Populate variable values from Kubernetes secret `serco-secret` 
```
SERCO_AUTH_URL=
SERCO_CLIENT_ID=
SERCO_CLIENT_SECRET=
SERCO_USERNAME=
SERCO_PASSWORD=
SERCO_URL=
```
3. Click the drop-down button for the `HmppsElectronicMonitoringCreateAnOrderApi` run configuration file in the top 
right corner, and select Edit Configurations. 
    - For the 'Active Profiles' field, put 'local'
    - You may also need to set the JDK to openjdk-23 or openjdk-21
    - Click on Modify options -> Choose Environment Variables
    - For the 'Environment Variables' field, put path to .env file
    - Apply these changes
4. Click the run button.

Or, to run the application using the command line:

setup environment variables:

run setup script
```bash
./scripts/create-env.sh
```
and then
```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Then visit [http://localhost:8080/health](hhttp://localhost:8081/health).


#### Calling endpoints

As part of getting the HMPPS Auth service running locally, 
[the in-memory database is seeded with data including a number of clients](https://github.com/ministryofjustice/hmpps-auth/blob/main/src/main/resources/db/dev/data/auth/V900_0__clients.sql). 
A client can have different permissions i.e. read, write, reporting, although strangely the column name is called `​​autoapprove`.

If you wish to call an endpoint of the API, an access token must be provided that is generated from the HMPPS Auth service.

##### Generate a token for a HMPPS Auth client

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
