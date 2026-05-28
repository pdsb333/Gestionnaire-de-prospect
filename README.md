# 💼 Gestionnaire de Prospect (GDP)

[![Spring Boot](https://shieldcn.dev/badge/Spring%20%20Boot-22c55e.svg?variant=outline&size=default&logo=springboot&logoColor=22c55e)](https://spring.io/projects/spring-boot)
[![Java](https://shieldcn.dev/badge/Java-ff0000.svg?variant=outline&size=default&logo=ri%3AFaJava&logoColor=ff0000)](https://openjdk.org/)
[![PostgreSQL](https://shieldcn.dev/badge/PostgreSQL-4673be.svg?variant=outline&size=default&logo=postgresql&logoColor=4673be)](https://www.postgresql.org/)
[![Docker](https://shieldcn.dev/badge/Docker-3b82f6.svg?variant=outline&size=default&logo=docker&logoColor=3b82f6)](https://www.docker.com/)
[![Nextjs](https://shieldcn.dev/badge/Next.js.svg?variant=outline&size=default&logo=nextdotjs&logoColor=ffffff)](https://nextjs.org/)
[![React](https://shieldcn.dev/badge/React-06b6d4.svg?variant=outline&size=default&logo=react&logoColor=06b6d4)](https://react.dev/)

**GDP** est une solution web moderne et intuitive conçue pour accompagner les demandeurs d’emploi dans l’organisation, le suivi et le pilotage quotidien de leurs démarches de recherche de poste. L'outil simplifie la prospection en centralisant les entreprises, les offres d'emploi, les candidatures et les contacts professionnels.

---

## ✨ Fonctionnalités Clés

*   **🔒 Authentification Sécurisée** : Inscription et connexion avec gestion de session sécurisée par JWT stocké dans un cookie `HttpOnly`, assurant une protection robuste contre les attaques XSS.
*   **🏢 Gestion des Entreprises** : Création, mise à jour, suppression et centralisation des informations des entreprises ciblées (description, contacts généraux, etc.).
*   **👥 Contacts Professionnels** : Association de contacts clés (recruteurs, managers, collaborateurs) au sein des entreprises ciblées, avec détails sur leurs rôles et coordonnées.
*   **📋 Suivi des Offres d'Emploi** : Enregistrement des offres d'emploi avec leurs liens, titres et gestion des relances.
*   **🚀 Candidatures & Prospection (Prospect)** : Suivi rigoureux de l'état des candidatures, des dates d'envoi, des dates de relance automatique prévues, et historique des interactions.

---

## 🛠️ Stack Technique

### 🖥️ Frontend
*   **Next.js 16.2.6** (React 19, TypeScript)
*   **Tailwind CSS v4** pour des styles performants et modernes
*   **Lucide React** pour des icônes épurées
*   **React Hook Form** pour la validation et la gestion des formulaires

### ⚙️ Backend
*   **Java 21** / **Spring Boot 3.2.5**
*   **Spring Security** pour le contrôle d'accès sécurisé
*   **JJWT (Java JWT)** pour la gestion et la vérification des tokens d'authentification
*   **Spring Data JPA** pour la persistance des données
*   **Springdoc OpenAPI 2.5.0** pour l'auto-génération de la documentation Swagger UI
*   **Testcontainers** pour l'exécution fluide de tests d'intégration avec une vraie base de données PostgreSQL

### 📦 Base de données & Outils
*   **PostgreSQL 15** (Base relationnelle principale)
*   **Docker & Docker Compose** pour l'orchestration locale des services
*   **Maven** pour la gestion de projet et de build

---

## 📁 Organisation des Dossiers

Le dépôt est organisé de la manière suivante :
*   [`/backend`](./backend) (voir sa [documentation détaillée](./backend/README.md)) : Code source de l'API Spring Boot, configuration Docker Compose et tests.
*   [`/frontend`](./frontend) : Code de l'application Next.js (pages, composants, styles).
*   [`/docs`](./docs) : Schémas et spécifications techniques (modèle API, diagramme ERD).

---

## ⚙️ Installation & Lancement Rapide

### 📋 Prérequis
*   **Java 21** (ou version supérieure)
*   **Node.js v20+** et **npm**
*   **Docker** et **Docker Compose**

---

### 1️⃣ Lancement de la Base de Données (Docker)
Pour démarrer l'instance locale PostgreSQL :
```bash
cd backend
docker-compose up -d
```
*La base de données sera démarrée sur le port `5432`.*

---

### 2️⃣ Configuration des Environnements

#### ☕ Backend
Les configurations par défaut sont disponibles dans `backend/src/main/resources/application.properties`. 
> [!IMPORTANT]
> En environnement de production, assurez-vous de surcharger la clé secrète JWT (`jwt.secret`) ainsi que les identifiants de la base de données.

#### 🖥️ Frontend
Assurez-vous qu'un fichier `.env` ou `.env.local` est présent à la racine du dossier `frontend` avec la configuration suivante :
```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:3000
NEXT_PUBLIC_APP_URL=http://localhost:3000
API_URL=http://localhost:8080/api/
```

---

### 3️⃣ Lancement du Serveur Backend (Spring Boot)
Depuis la racine du dossier `/backend` :
```bash
cd backend
# Sous Linux/macOS
./mvnw spring-boot:run

# Sous Windows
mvnw.cmd spring-boot:run
```
L'API démarre et écoute sur le port **`8080`**.

---

### 4️⃣ Lancement de l'Application Frontend (Next.js)
Depuis la racine du dossier `/frontend` :
```bash
cd frontend
npm install
npm run dev
```
L'application frontend démarre et est accessible sur [**http://localhost:3000**](http://localhost:3000).

---

## 🧪 Tests & Couverture de Code

### Exécution des Tests Backend
Pour lancer les tests unitaires et d'intégration (utilisant Testcontainers) :
```bash
cd backend
./mvnw clean test
```

### Rapport de Couverture JaCoCo
Après avoir exécuté les tests, un rapport HTML détaillé de couverture est automatiquement généré à l'emplacement suivant :
`backend/target/site/jacoco/index.html`

---

## 📖 Spécifications & Documentation

*   **☕ Documentation Backend** : [Consulter backend/README.md](./backend/README.md) pour les détails techniques de l'API Spring Boot, de ses Beans et de sa configuration.
*   **🔍 Swagger UI (Interactif)** : Accédez à [**http://localhost:8080/swagger-ui/index.html**](http://localhost:8080/swagger-ui/index.html) lorsque le backend est lancé.
*   **📋 Modèle des Flux API** : [Consulter docs/api-model.md](./docs/api-model.md)
*   **📊 Diagramme de Base de Données (ERD)** : [Consulter docs/erd.md](./docs/erd.md)

---
*Développé avec passion pour simplifier l'organisation et optimiser les candidatures.*
