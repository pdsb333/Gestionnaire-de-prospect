# Diagramme entité relation 


```mermaid
---
title: GDP
---
classDiagram 
	direction RL

	class User{
	 +String email
	 +String pseudo
	 +String password
	 +Role role
	 +login()
   +register()
	 +logout()
	}
	class Role{
		<<enumeration>>
		ROLE_ADMIN
		ROLE_USER
	}
	class Business{
		+String name
		+String description
		+String recruitmentServiceContact
		+List~Professional~ professionalsList
		+List~JobOffer~ jobOffersList
	}
	class Professional{
		+String lastName
		+String firstName
		+String job
		+String contact
	}
	class JobOffer{
		+String name
		+String link
	}
	class Application{
		+JobOffer offer
	}
	class Prospect {
		+LocalDateTime initialApplicationDate
		+LocalDateTime dateRelaunch
		+List~LocalDateTime~ historyOfRelaunches
	}
	<<abstract>> Prospect
	
	class Canvassing
	
	Role --> User : "1"
	User "1" --"*" Business
	Business "1" o-- "*" JobOffer
	Business "1" o-- "*" Professional
	Application "1" *-- "1" JobOffer
    Prospect "1" --> "1" Application

	
                 
               