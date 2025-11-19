Event-Driven Onboarding System

This project is a complete, event-driven microservices system that simulates a new user onboarding flow. It is built using Spring Boot, Java 17, Kafka, MongoDB, and DynamoDB, and is designed to be fully containerized with Docker.

Core Services

user-api-service (Port 8080): A Spring Boot REST API for user registration and authentication.

Database: MongoDB (for users)

Events: Publishes user-created-topic messages to Kafka using the Outbox Pattern.

profile-api-service (Port 8081): A Spring Boot REST API for managing user profiles.

Database: DynamoDB

notification-service (Lambda): An AWS Lambda function that listens to the Kafka topic.

Action: When a user-created-topic event is received, it calls the profile-api-service to create the new user profile.

lambda-invoker: A helper service that consumes from Kafka and invokes the notification-service container, simulating the AWS Kafka-to-Lambda trigger.

Prerequisites

Java 17 (or higher)

Maven 3.8 (or higher)

Docker & Docker Compose

How to Run (The Easy Way)

I have included a simple script that builds all services and starts the entire system.

1. Make the script executable (On macOS/Linux):

chmod +x build-and-run.sh


2. Run the script:

./build-and-run.sh


This script will:

Clean up any old Docker containers.

Build the .jar files for all three services (user-api, profile-api, notification-service).

Start all services using docker-compose up --build.

Your system is ready when you see logs from all containers, especially the lambda-invoker.

Local Admin Tools

Your docker-compose.yml file starts several admin tools to help you "see" the data:

MongoDB: http://localhost:8082 (Mongo Express)

Login: admin / admin

Kafka: http://localhost:9001 (Kafdrop)

DynamoDB: http://localhost:8001 (DynamoDB Admin)

Monitoring: http://localhost:3000 (Grafana)

Login: admin / admin