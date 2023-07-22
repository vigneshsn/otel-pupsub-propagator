# otel-pupsub-propagator

## Context
This repository attempts to create an Open telemetry(OTEL) for Google pubsub.
OTEL does not currently have support for this feature.

## About this app
This app provides a REST endpoint which can be used to post greetings.
The greeting will be posted in pubsub and consumer by the same app.

Quarkus framework is used.
Gradle is used as build tool


## How to run this app locally

### Pre-requisite
You need to run the Google pubsub emulator locally first.
https://cloud.google.com/pubsub/docs/emulator

Follow the steps to run the pubsub locally. During the app startup the topics and subscription
will be created automatically.

### run locally
```./gradlew quarkusDev```