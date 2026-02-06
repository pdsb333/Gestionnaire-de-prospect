# Model API – Requests / Responses & Flux
Ce diagramme décrit les DTOs exposés par l’API REST ainsi que les principaux flux entre requêtes et réponses.
Il ne représente pas le modèle de persistance.


```mermaid
---
title: API flux
---
classDiagram
direction RL

%% DTOs d'entrée
class UserLoginRequest{
<<request>>
-String email
-String password
}
class UserRegisterRequest{
<<request>>
-String pseudo
-String email
-String password
}

class BusinessRequest{
<<request>>
-String name
-String description
-String recruitmentServiceContact
}

class JobOfferRequest{
<<request>>
-String name
-String link
-Integer relaunchFrequency
}

class ProfessionalRequest{
<<request>>
-String lastName
-String firstName
-String job
-String contact
}

class ApplicationRequest {
<<request>>
}

class ProspectRequest {
<<request>>
-LocalDateTime initialApplicationDate
-LocalDateTime dateRelaunch
-List~LocalDateTime~ historyOfRelaunch
}

%% DTOs de sortie
class UserResponse{
<<response>>
-String pseudo
-String email
}

class BusinessResponse{
<<response>>
-Long id
-String name;
-String description;
-String recruitmentServiceContact;
-List~JobOfferResponse~ jobOffersList;
-List~ProfessionalResponse~ professionalsList;
}

class JobOfferResponse{
<<response>>
-Long id
-String name
-String link
-Integer relaunchFrequency
-ApplicationResponse application
}

class ProfessionalResponse{
<<response>>
-Long id
-String lastName
-String firstName
-String job
-String contact
}

class ApplicationResponse {
<<response>>
}

class ProspectResponse {
<<response>>
-Long id
-LocalDateTime initialApplicationDate
-LocalDateTime dateRelaunch
-List~LocalDateTime~ historyOfRelaunch
}


%% Flux API 

%% 1. COMPOSITIONS (Business contient ses enfants)
BusinessResponse "1" *-- "*" ProfessionalResponse
BusinessResponse "1" *-- "*" JobOfferResponse

%% 2. COMPOSITION JobOffer → Application (1:1)
JobOfferResponse "1" *-- "1" ApplicationResponse

%% 3. FLUX API (Request → Response)
BusinessRequest ..> BusinessResponse : "POST /businesses"
BusinessRequest ..> BusinessResponse : "PUT /businesses/{id}"

JobOfferRequest ..> JobOfferResponse : "POST /joboffers"
JobOfferRequest ..> JobOfferResponse : "PUT /joboffers/{id}"

ProfessionalRequest ..> ProfessionalResponse : "POST /professionals"
ProfessionalRequest ..> ProfessionalResponse : "PUT /professionals/{id}"

ApplicationRequest ..> ApplicationResponse : "POST /applications"
ApplicationRequest ..> ApplicationResponse : "PUT /applications/{id}"

UserLoginRequest ..> UserResponse : "POST /auth/login"
UserRegisterRequest ..> UserResponse : "POST /auth/register"

%% 4. Héritage (Prospect → Application)
ProspectResponse --> ApplicationResponse

%% 5. Flux entités (optionnel)
BusinessRequest ..> Business : "→ mapTo()"



                 
               