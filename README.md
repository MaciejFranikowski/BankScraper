# Bank Scraper

## Description

This project is a Spring Boot application for accessing your MBank accounts.
It includes a service for user authentication, 
which uses two-factor authentication and handles various authentication steps such as fetching CSRF tokens, and Strong Customer Authentication ID.

## Installation

This project uses Maven for dependency management. To install the project, you can use the following command:

```bash
./mvnw clean install
```

## Usage
To run the application, use Maven Wrapper and the following command:

```bash
./mvnw spring-boot:run
```

## Testing
The project includes unit tests, which can be run with the following command:

```bash
./mvnw test
```
