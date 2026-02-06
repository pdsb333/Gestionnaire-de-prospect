# Gestionnaire-de-prospect
Application de gestion de prospects, pensée pour accompagner les demandeurs d’emploi dans l’organisation et le suivi de leurs démarches, en leur offrant un outil simple et efficace pour piloter leur recherche.

## Prérequis

- Java 21 installé

## Installation

- Cloner le dépôt
- Lancer la base de données PostgreSQL avec Docker Compose (voir docker-compose.yml)
- Configurer les paramètres dans 'application.properties'
- Penser à changer les secrets JWT en production 

## Dépendance du projet 

- Spring web
- Spring Data JPA
- PostgreSQL driver
- Spring security
- Validation
- Spring Boot devtools
- Lombok
- Hibernate

## Lancement

Lancer l’application avec :  
./mvnw spring-boot:run
