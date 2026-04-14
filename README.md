# Gestionnaire-de-prospect

Application de gestion de prospects, pensée pour accompagner les demandeurs d’emploi dans l’organisation et le suivi de leurs démarches, en leur offrant un outil simple et efficace pour piloter leur recherche.

## 🚀 Fonctionnalités

- **Authentification** : Gestion sécurisée des comptes via JWT.
- **Gestion des Entreprises** : Création, modification et suivi des entreprises ciblées.
- **Suivi d'Offres** : Centralisation et organisation des offres d'emploi.
- **Gestion des Candidatures** : Suivi précis de l'état d'avancement pour chaque prospect.

## 🛠️ Stack Technique

- **Backend** : Java 21, Spring Boot 4, Spring Security, Spring Data JPA.
- **Base de données** : PostgreSQL.
- **Outils** : Docker & Docker Compose, Lombok, Maven.
- **Documentation API** : OpenAPI 3 / Swagger UI.
- **Qualité** : JUnit 5, JaCoCo (Couverture de tests).

## 📋 Prérequis

- **Java 21** ou supérieur.
- **Docker** et **Docker Compose** pour la base de données.
- **Maven** (inclus via le wrapper `./mvnw`).

## ⚙️ Installation & Lancement

1. **Clonage du projet** :
   ```bash
   git clone https://github.com/votre-compte/Gestionnaire-de-prospect.git
   cd Gestionnaire-de-prospect
   ```

2. **Lancer la base de données** :
   ```bash
   cd GDP
   docker-compose up -d
   ```

3. **Configuration** :
   - Les paramètres par défaut sont dans `GDP/src/main/resources/application.properties`.
   - **Important** : Pensez à modifier les secrets JWT (`application.security.jwt.secret-key`) pour la production.

4. **Lancer l'application** :
   Depuis le dossier `GDP` :
   ```bash
   ./mvnw spring-boot:run
   ```

## 🧪 Tests & Qualité

Pour exécuter les tests et générer le rapport de couverture JaCoCo :
```bash
cd GDP
./mvnw test
```
Le rapport sera disponible dans `GDP/target/site/jacoco/index.html`.

## 📖 Documentation

- **Swagger UI** : Accédez à `http://localhost:8080/swagger-ui/index.html` une fois l'application lancée.
- **Modèle API** : [Consulter api-model.md](./docs/api-model.md)
- **Diagramme ERD** : [Consulter erd.md](./docs/erd.md)

---
*Projet développé pour faciliter la recherche d'emploi et l'organisation professionnelle.*
