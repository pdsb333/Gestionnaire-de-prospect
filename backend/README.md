# 🚀 GDP Backend - API du Gestionnaire de Prospects

Bienvenue dans la documentation technique du backend de **GDP (Gestionnaire De Prospects)**. Ce projet est une API REST robuste développée avec **Spring Boot 3.2.5** et **Java 21**, conçue pour simplifier le suivi des démarches de prospection d'emploi.

Cette documentation s'adresse aux développeurs reprenant le projet et fournit une vue exhaustive sur l'architecture des beans, la configuration du `pom.xml`, les tests d'intégration (basés sur Testcontainers), la configuration Docker, et les ressources de référence.

---

## 🛠️ 1. Configuration Docker, Base de Données & Variables d'Environnement

Le backend s'appuie sur une base de données **PostgreSQL 15** configurée via Docker Compose, et sa configuration est gérée de manière dynamique à l'aide de variables d'environnement.

### 🔑 Variables d'Environnement (`.env`)

Le projet utilise **`spring-dotenv`** pour charger automatiquement les variables d'environnement depuis un fichier `.env` local (qui est exclu de Git pour des raisons de sécurité).

Pour configurer votre environnement local :
1. Copiez le fichier d'exemple `.env.example` pour créer votre fichier `.env` :
   ```bash
   cp .env.example .env
   ```
2. Modifiez le fichier `.env` avec vos accès. Pour le développement local avec le conteneur Docker par défaut, utilisez les valeurs suivantes :
   ```env
   DB_URL=jdbc:postgresql://localhost:5432/nom_de_la_base
   DB_USERNAME=utilisateur
   DB_PASSWORD=motdepasse
   ```

Ces variables d'environnement sont automatiquement injectées dans le fichier `backend/src/main/resources/application.properties` au démarrage :
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

### Fichier `docker-compose.yml`
```yaml
services:
  db:
    image: postgres:15
    container_name: postgres_dev
    environment:
      POSTGRES_USER: utilisateur
      POSTGRES_PASSWORD: motdepasse
      POSTGRES_DB: nom_de_la_base
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    networks:
      - dev-network

networks:
  dev-network:

volumes:
  pgdata:
```

### Commandes Utiles Docker
* **Démarrer la base de données (arrière-plan) :**
  ```bash
  docker compose up -d
  ```
* **Arrêter le conteneur et préserver les données :**
  ```bash
  docker compose down
  ```
* **Arrêter le conteneur et supprimer les volumes :**
  ```bash
  docker compose down -v
  ```

---

## 📦 2. Dépendances & Écosystème (`pom.xml`)

Le projet s'appuie sur un ensemble de dépendances Java robustes déclarées dans le fichier `pom.xml` :

### 🚀 Starters Spring Boot (Version 3.2.5)
*   **`spring-boot-starter-web`** : Fournit le moteur MVC pour concevoir les APIs REST et inclut le serveur embarqué Tomcat.
*   **`spring-boot-starter-data-jpa`** : Gère la persistance objet-relationnelle (ORM) avec Hibernate et simplifie l'accès aux données grâce aux interfaces de dépôt.
*   **`spring-boot-starter-security`** : Gère la sécurité (authentification stateless via jeton, gestion de session et autorisations).
*   **`spring-boot-starter-validation`** : Valide automatiquement les requêtes DTO entrantes via les annotations Jakarta Bean Validation (ex. `@NotBlank`, `@Email`, etc.).
*   **`spring-boot-starter-test`** (Scope: `test`) : Fournit les outils de test standards (JUnit 5, AssertJ, Mockito, MockMvc).
*   **`spring-security-test`** (Scope: `test`) : Permet d'injecter des contextes de sécurité fictifs dans MockMvc pour simuler des utilisateurs connectés.

### 🔐 Authentification & Documentation API
*   **`jjwt` (Java JWT - Version 0.12.6)** :
    *   `jjwt-api` : Fournit les interfaces standardisées pour la création et le traitement des JWT.
    *   `jjwt-impl` (Scope: `runtime`) : Implémentation concrète de JJWT.
    *   `jjwt-jackson` (Scope: `runtime`) : Sérialisation et désérialisation JSON du payload JWT à l'aide de Jackson.
*   **`springdoc-openapi-starter-webmvc-ui` (Version 2.5.0)** : Génère automatiquement la spécification OpenAPI 3 de vos contrôleurs et fournit une interface graphique interactive Swagger UI pour tester les endpoints.

### 🐳 Base de Données & Environnement de Tests d'Intégration
*   **`postgresql`** (Scope: `runtime`) : Pilote JDBC officiel permettant à Spring Data JPA de communiquer avec PostgreSQL.
*   **`testcontainers` (Version 1.20.4 dans le BOM)** :
    *   `junit-jupiter` (Scope: `test`) : Intégration de Testcontainers avec le moteur de test JUnit 5.
    *   `postgresql` (Scope: `test`) : Module de conteneurisation éphémère d'une vraie base PostgreSQL pour exécuter les tests d'intégration sans perturber la base locale.

### ⚙️ Configuration & Gestion de l'Environnement
*   **`spring-dotenv` (Version 4.0.0)** : Charge automatiquement les variables définies dans un fichier `.env` local dans l'environnement Spring. Cela évite de stocker des informations sensibles (comme les identifiants de base de données) en clair dans les fichiers de configuration de l'application.

### ⚙️ Plugins de Build Importants
*   **`spring-boot-maven-plugin`** : Emballe l'application sous forme de fichier JAR exécutable prêt pour la production.
*   **`jacoco-maven-plugin` (Version 0.8.11)** : Génère un rapport HTML d'analyse de couverture du code suite à l'exécution de la commande de test.
*   **`maven-failsafe-plugin` (Version 3.2.5)** : Permet de séparer les tests unitaires des tests d'intégration lourds, en exécutant ces derniers durant la phase `verify` de Maven.

---

## 🏗️ 3. Architecture logicielle : Les Beans Spring

L'architecture suit les principes de conception de Spring Boot (Architecture en couches contrôleur -> service -> dépôt). Voici le détail complet des beans déclarés et injectés dans l'application :

### 🎛️ Contrôleurs REST (`@RestController`)
Ces composants exposent les points de terminaison (endpoints) de l'API et gèrent la désérialisation, la sérialisation, et les validations initiales.

| Nom du Bean | Rôle principal | Endpoints Exposés |
| :--- | :--- | :--- |
| `AuthController` | Authentification des utilisateurs (création de compte & connexion). | `/api/auth/register`, `/api/auth/login` |
| `BusinessController` | Gestion complète des entreprises et structures cibles. | `/api/business/**` |
| `JobOfferController` | Gestion des offres d'emploi publiées par les entreprises. | `/api/job-offers/**` |
| `ProfessionalController` | Gestion des contacts professionnels (recruteurs, managers). | `/api/professionals/**` |
| `ApplicationController` | Gestion du cycle de vie des candidatures et relances. | `/api/application/**` |

### ⚙️ Services Métier (`@Service`)
Les beans de service implémentent la logique métier stricte, incluant la gestion transactionnelle.

* **`AuthService`** : Orchestre le processus d'inscription et valide les identifiants pour générer le token JWT.
* **`JwtService`** : Bean utilitaire d'extraction, génération et validation des JSON Web Tokens (JJWT 0.12.6).
* **`CustomUserDetailsService`** : Implémente `UserDetailsService` de Spring Security pour charger l'identité de l'utilisateur depuis la base de données.
* **`BusinessServiceImpl`** (implémente `BusinessService`) : Gère les entreprises avec une vérification stricte de propriété d'objet (ownership).
* **`JobOfferServiceImpl`** (implémente `JobOfferService`) : Gère les fiches de postes et leur rattachement aux entreprises.
* **`ProfessionalServiceImpl`** (implémente `ProfessionalService`) : Centralise l'affectation des contacts recruteurs à des entreprises.
* **`ApplicationServiceImpl`** (implémente `ApplicationService`) : Gère les candidatures en appliquant des contraintes d'unicité métier.

### 💾 Dépôts de Données JPA (`@Repository`)
Beans générés par Spring Data JPA fournissant les interfaces CRUD et requêtes personnalisées vers PostgreSQL.

* **`UserRepository`** : Requêtes sur les comptes utilisateurs (`User`).
* **`BusinessRepository`** : Requêtes sur les entreprises, gérant les requêtes par utilisateur authentifié.
* **`JobOfferRepository`** : Accès aux offres d'emploi.
* **`ProfessionalRepository`** : Accès aux professionnels cibles.
* **`ApplicationRepository`** : Accès aux candidatures (démarches).

### 🛡️ Beans de Sécurité & Validation (`@Configuration` / `@Component`)
* **`SecurityConfig`** : Configure le `SecurityFilterChain`, définit les règles d'autorisation HTTP, configure l'encodeur de mot de passe (`BCryptPasswordEncoder`) et gère le gestionnaire d'authentification.
* **`JwtAuthenticationFilter`** : Filtre d'interception HTTP (`OncePerRequestFilter`) extrayant et validant le header `Authorization: Bearer <token>` sur chaque requête protégée.
* **`VerifyBusinessForUser`** : Composant helper réutilisable assurant qu'un utilisateur n'accède qu'à des ressources lui appartenant légitimement, retournant une erreur `404 Not Found` en cas d'accès illicite (pour éviter la fuite d'informations).

---

## 🧪 4. Historique & Couverture des Éléments Testés

Le backend dispose d'une couverture de tests rigoureuse, combinant **tests unitaires** et **tests d'intégration réels**.

### 🐳 Stratégie d'intégration : Testcontainers
Pour s'affranchir des limites d'H2 (divergences de syntaxe SQL), les tests d'intégration utilisent **Testcontainers**. Un conteneur **PostgreSQL 15** est instancié automatiquement une unique fois pour l'intégralité de la suite de tests (via `AbstractIntegrationTest`).

### 📋 Scénarios Fonctionnels Couverts (Tests d'Intégration)

#### 🔹 Gestion des Entreprises & Contacts
* **`CreationEtPersistanceTest`** :
  * **TC-001** : Création d'une entreprise et rattachement de plusieurs offres d'emploi. Validation de la clé étrangère de l'utilisateur, vérification de la persistance en base et de l'imbrication dans les retours d'API (`GET /api/business`).
  * **TC-002** : Association de plusieurs contacts professionnels (`Professional`) à une entreprise. Validation des cas limites (absence d'authentification 401, champs obligatoires vides 400, entreprise inexistante 404).

#### 🔹 Contraintes Métier des Candidatures
* **`ApplicationUniquenessTest`** :
  * **TC-003** : Validation de l'unicité fonctionnelle de la candidature (création nominale réussie d'une démarche rattachée à une offre).
  * **TC-004** : Interdiction absolue de créer une seconde candidature pour une même offre. L'API retourne explicitement un code de statut HTTP `409 Conflict`.

#### 🔹 Sécurité & Isolation Inter-Utilisateurs
* **`OwnershipSecurityTest`** :
  * **TC-006 (Isolation en lecture)** : Un utilisateur B ne peut en aucun cas lire les entreprises ou ressources créées par l'utilisateur A. Les requêtes retournent systématiquement des codes `404 Not Found` (masquage de l'existence de la ressource).
  * **TC-007 (Isolation en écriture)** : Empêche toute mise à jour ou suppression de ressources appartenant à autrui (User B tentant de modifier/supprimer une ressource de User A). Les données d'origine restent inchangées en base.

#### 🔹 Cohérence Relationnelle
* **`CascadeDeleteTest`** :
  * **TC-008 (Suppression en cascade)** : La suppression d'un business déclenche la suppression automatique en cascade de tout son sous-graphe de données (Offres d'emploi, Contacts professionnels, Démarches de candidature) afin d'éviter les enregistrements orphelins et préserver l'intégrité référentielle. Les comptes utilisateurs associés, en revanche, restent intacts.

---

## 📚 5. Spécifications & Modélisation (Dossier `/docs`)

Le projet propose des documents d'architecture technique situés dans le dossier commun `/docs` à la racine :

*   **📊 [Modèle Conceptuel de Données (ERD) - erd.md](../docs/erd.md)** : Modélisation visuelle des tables relationnelles PostgreSQL (entités, contraintes, relations `@OneToMany`, cascades).
*   **📋 [Spécifications des Flux API - api-model.md](../docs/api-model.md)** : Documentation détaillée sur les requêtes HTTP, structures de payloads JSON et interactions client/serveur.

---

## 🔗 6. Liens Pratiques & Documentation Officielle

Voici les liens indispensables pour maîtriser les technologies utilisées et approfondir le sujet :

### 🚀 Frameworks & Socle Spring
* **[Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)** : Guide officiel complet.
* **[Spring Data JPA Guide](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)** : Tout sur les requêtes JPA, relations `@OneToMany`, cascades.
* **[Spring Security Docs](https://docs.spring.io/spring-security/reference/index.html)** : Gestion des filtres, configurations d'autorisation stateless.

### 🔐 Authentification & Normes API
* **[JSON Web Tokens (JWT) Draft](https://jwt.io/)** : Validations de jetons, débogage de payloads.
* **[Springdoc OpenAPI (Swagger)](https://springdoc.org/)** : Configuration de l'UI Swagger. L'interface Swagger locale est accessible une fois l'application démarrée à l'adresse : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

### 🐳 Conteneurs & Environnement de Tests
* **[PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)** : Documentation de l'image de base officielle de PostgreSQL.
* **[Testcontainers - PostgreSQL Module](https://java.testcontainers.org/modules/databases/postgres/)** : Guide d'intégration JUnit 5 et PostgreSQL dans les environnements de test Java.

---

## 🎮 7. Commandes de Prise en Main Rapide

Avant de lancer le projet, assurez-vous d'avoir démarré la base PostgreSQL locale via Docker et d'avoir configuré vos variables d'environnement.

1. **Initialiser les variables d'environnement :**
   ```bash
   cp .env.example .env
   ```
2. **Démarrer la base de données :**
   ```bash
   docker compose up -d
   ```
3. **Compiler et packager l'application (sans exécuter les tests) :**
   ```bash
   ./mvnw clean package -DskipTests
   ```
4. **Lancer l'intégralité des tests unitaires et d'intégration :**
   ```bash
   ./mvnw clean verify
   ```
5. **Démarrer l'API Spring Boot en mode développement :**
   ```bash
   ./mvnw spring-boot:run
   ```
