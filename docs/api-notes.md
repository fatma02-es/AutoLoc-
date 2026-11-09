# Atelier 5 : Spring MVC et SpringDoc — Documentation de l'API REST AutoLoc

Ce document regroupe les notes d'architecture, la cartographie des endpoints REST, les choix de sérialisation Jackson, l'analyse comparative des outils de test et de documentation, ainsi que le retour d'expérience SonarQube et les réponses aux questions de compréhension.

---

## A. Endpoints

L'ensemble des endpoints est exposé sous le préfixe commun configuré dans `application.properties` :  
`server.servlet.context-path=/api` (port standard : 8089 / 8080).

| Contrôleur | Verbe | Route | Méthode Service appelée | Code de succès | Codes d'erreur | Description / Rôle |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ContratController** | `GET` | `/api/contrats` | `IContratService.findAll()` | `200 OK` | — | Lister tous les contrats de location |
| **ContratController** | `GET` | `/api/contrats/{id}` | `IContratService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un contrat par identifiant |
| **ContratController** | `POST` | `/api/contrats` | `IContratService.create(contrat)` | `201 Created` | `400 Bad Request` | Créer un contrat (cascade optionnelle sur paiements) |
| **ContratController** | `PUT` | `/api/contrats/{id}` | `IContratService.update(id, contrat)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Modifier les attributs modifiables d'un contrat |
| **ContratController** | `DELETE` | `/api/contrats/{id}` | `IContratService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un contrat et ses paiements (cascade REMOVE) |
| **PaiementController** | `GET` | `/api/paiements` | `IPaiementService.findAll()` | `200 OK` | — | Lister tous les paiements enregistrés |
| **PaiementController** | `GET` | `/api/paiements/{id}` | `IPaiementService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un paiement par identifiant |
| **PaiementController** | `POST` | `/api/paiements` | *Non exposé (lecture seule)* | — | `405 Method Not Allowed` | Rejet de la création directe d'un paiement |
| **AgenceController** | `GET` | `/api/agences` | `IAgenceService.findAll()` | `200 OK` | — | Lister toutes les agences |
| **AgenceController** | `GET` | `/api/agences/{id}` | `IAgenceService.findById(id)` | `200 OK` | `404 Not Found` | Consulter une agence par identifiant |
| **AgenceController** | `POST` | `/api/agences` | `IAgenceService.create(agence)` | `201 Created` | `400 Bad Request` | Créer une agence (validation attributs obligatoires) |
| **AgenceController** | `PUT` | `/api/agences/{id}` | `IAgenceService.update(id, agence)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Modifier une agence existante |
| **AgenceController** | `DELETE` | `/api/agences/{id}` | `IAgenceService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer une agence |
| **EmployeController** | `GET` | `/api/employes` | `IEmployeService.findAll()` | `200 OK` | — | Lister tous les employés |
| **EmployeController** | `GET` | `/api/employes/{id}` | `IEmployeService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un employé par identifiant |
| **EmployeController** | `POST` | `/api/employes` | `IEmployeService.create(employe)` | `201 Created` | `400 Bad Request`, `404 Not Found` | Créer un employé (agence imbriquée par ID) |
| **EmployeController** | `PUT` | `/api/employes/{id}` | `IEmployeService.update(id, employe)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Modifier un employé existant |
| **EmployeController** | `DELETE` | `/api/employes/{id}` | `IEmployeService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un employé |
| **VehiculeController** | `GET` | `/api/vehicules` | `IVehiculeService.findAll()` | `200 OK` | — | Lister tous les véhicules de la flotte |
| **VehiculeController** | `GET` | `/api/vehicules/{id}` | `IVehiculeService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un véhicule par identifiant |
| **VehiculeController** | `POST` | `/api/vehicules` | `IVehiculeService.create(vehicule)` | `201 Created` | `400 Bad Request`, `404 Not Found` | Créer un véhicule (tarif journalier >= 0) |
| **VehiculeController** | `PUT` | `/api/vehicules/{id}` | `IVehiculeService.update(id, vehicule)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Modifier un véhicule existant |
| **VehiculeController** | `DELETE` | `/api/vehicules/{id}` | `IVehiculeService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un véhicule |
| **EquipementController** | `GET` | `/api/equipements` | `IEquipementService.findAll()` | `200 OK` | — | Lister tous les équipements disponibles |
| **EquipementController** | `GET` | `/api/equipements/{id}` | `IEquipementService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un équipement par identifiant |
| **EquipementController** | `POST` | `/api/equipements` | `IEquipementService.create(equipement)` | `201 Created` | `400 Bad Request` | Créer un équipement (libellé non vide) |
| **EquipementController** | `PUT` | `/api/equipements/{id}` | `IEquipementService.update(id, equipement)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Modifier un équipement existant |
| **EquipementController** | `DELETE` | `/api/equipements/{id}` | `IEquipementService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un équipement |
| **ClientController** | `GET` | `/api/clients` | `IClientService.findAll()` | `200 OK` | — | Lister tous les clients |
| **ClientController** | `GET` | `/api/clients/{id}` | `IClientService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un client par identifiant |
| **ClientController** | `POST` | `/api/clients` | `IClientService.create(client)` | `201 Created` | `400 Bad Request` | Créer un client (identification obligatoire) |
| **ClientController** | `PUT` | `/api/clients/{id}` | `IClientService.update(id, client)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Modifier un client existant |
| **ClientController** | `DELETE` | `/api/clients/{id}` | `IClientService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un client |
| **ReservationController** | `GET` | `/api/reservations` | `IReservationService.findAll()` | `200 OK` | — | Lister toutes les réservations |
| **ReservationController** | `GET` | `/api/reservations/{id}` | `IReservationService.findById(id)` | `200 OK` | `404 Not Found` | Consulter une réservation par identifiant |
| **ReservationController** | `POST` | `/api/reservations` | `IReservationService.create(reservation)` | `201 Created` | `400 Bad Request`, `404 Not Found` | Créer une réservation (dates cohérentes, client/véhicule) |
| **ReservationController** | `PUT` | `/api/reservations/{id}` | `IReservationService.update(id, reservation)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Modifier une réservation existante |
| **ReservationController** | `DELETE` | `/api/reservations/{id}` | `IReservationService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer une réservation |
| **MaintenanceController** | `GET` | `/api/maintenances` | `IMaintenanceService.findAll()` | `200 OK` | — | Lister toutes les maintenances |
| **MaintenanceController** | `GET` | `/api/maintenances/{id}` | `IMaintenanceService.findById(id)` | `200 OK` | `404 Not Found` | Consulter une maintenance par identifiant |
| **MaintenanceController** | `POST` | `/api/maintenances` | `IMaintenanceService.create(maintenance)` | `201 Created` | `400 Bad Request`, `404 Not Found` | Créer une maintenance (dates cohérentes, véhicule lié) |
| **MaintenanceController** | `PUT` | `/api/maintenances/{id}` | `IMaintenanceService.update(id, maintenance)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Modifier une maintenance existante |
| **MaintenanceController** | `DELETE` | `/api/maintenances/{id}` | `IMaintenanceService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer une maintenance |

---

## B. Annotations Jackson et Sérialisation

### 1. Tableau synthétique des associations

| Association | Entité & Attribut annoté | Annotation retenue | Justification |
| :--- | :--- | :--- | :--- |
| **Contrat ↔ Paiement** | `Contrat.paiements` | `@JsonManagedReference("contrat-paiements")` | **Composition stricte** : Les paiements font partie intégrante du document JSON du contrat. Cette annotation indique à Jackson de sérialiser la liste des paiements enfants. |
| **Contrat ↔ Paiement** | `Paiement.contrat` | `@JsonBackReference("contrat-paiements")` | Complémentaire de `@JsonManagedReference` : Jackson ignore la référence inverse vers le parent lors de la sérialisation, ce qui brise la boucle infinie sans perdre la relation en mémoire. |
| **Agence → Vehicule** | `Agence.vehicules` | `@JsonIgnore` | **Agrégation** : Un véhicule a son cycle de vie autonome. Évite de charger et sérialiser l'ensemble du parc automobile lors d'une simple requête sur l'agence. |
| **Agence → Employe** | `Agence.employes` | `@JsonIgnore` | **Agrégation** : Évite d'embarquer les listes RH dans le payload de l'agence et brise la boucle bidirectionnelle avec `Employe.agence`. |
| **Vehicule ↔ Equipement** | `Equipement.vehicules` | `@JsonIgnore` | **Données partagées (`@ManyToMany`)** : L'équipement est une table de référence. On ignore la liste inverse des véhicules pour prévenir la récursion cyclique. |
| **Vehicule → Maintenance** | `Vehicule.maintenances` | `@JsonIgnore` | Évite la récursion cyclique avec `Maintenance.vehicule` tout en conservant la consultation directe via `/api/maintenances`. |
| **Client → Reservation** | `Client.reservations` | `@JsonIgnore` | Évite d'embarquer tout l'historique des réservations dans la fiche client et élimine le cycle avec `Reservation.client`. |
| **Reservation ↔ Contrat** | `Reservation.contrat` | `@JsonIgnore` | Évite le cycle bidirectionnel 1-to-1 avec `Contrat.reservation`. |

### 2. Différence fondamentale entre `@JsonBackReference` et `@JsonIgnore`

* **`@JsonBackReference`** fonctionne exclusivement en binôme avec **`@JsonManagedReference`** :
  * Lors de la **sérialisation** (Java vers JSON), le champ annoté `@JsonBackReference` est omis.
  * Lors de la **désérialisation** (JSON vers Java), Jackson réinjecte automatiquement l'objet parent dans le champ enfant annoté `@JsonBackReference`.
  * **Cas d'usage** : Modèles en composition forte où l'enfant est créé ou lu imbriqué dans le JSON du parent (ex. `Contrat ↔ Paiement`).
* **`@JsonIgnore`** est une exclusion inconditionnelle :
  * Le champ est totalement ignoré lors de la sérialisation (et de la désérialisation par défaut, sauf configuration explicite).
  * Il ne lie aucun parent/enfant à la désérialisation.
  * **Cas d'usage** : Agrégations indépendantes où l'enfant référence son parent de manière isolée via un ID JSON `{ "agence": { "idAgence": 1 } }`.

---

## C. Postman et Swagger

### 1. Rôles et complémentarité

* **Postman** est un client HTTP d'exécution et de test :
  * Il permet de scénariser des flux complexes avec ordonnancement, d'injecter des variables dynamiques (`{{contratId}}`), d'automatiser des assertions JavaScript (`pm.test()`, codes de statut, structure de payload), et de rejouer des tests de régression.
  * **Exemple concret rencontré dans l'atelier** : Le scénario de cascade `Contrat ↔ Paiement`. Postman envoie un `POST /api/contrats` contenant deux paiements imbriqués, mémorise le `contratId` généré, vérifie par un `GET /api/paiements` que les paiements existent bien en base (cascade `PERSIST`), puis exécute un `DELETE /api/contrats/{{contratId}}` et valide par un `GET /api/paiements/{id}` que le paiement a bien été supprimé (cascade `REMOVE` avec statut 404).
* **Swagger UI / SpringDoc OpenAPI** est un outil de spécification et d'exploration interactive :
  * Il génère dynamiquement la spécification OpenAPI 3.0 à partir du code source Java et des annotations de contrôleur (`@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`).
  * Il offre une documentation vivante, auto-synchronisée avec le code, avec un bac à sable interactif (*Try it out*) directement accessible dans le navigateur à `/api/swagger-ui.html`.
  * **Exemple concret rencontré dans l'atelier** : Visualisation immédiate des contrats de requêtes et de réponses (schémas JSON de `Contrat`, codes `201`, `400`, `404`), avec affichage en clair des descriptions de paramètres d'URL (`id`).

---

## D. Anomalies SonarQube for IDE

Dans le cadre de l'audit de qualité avec SonarQube for IDE sur le package `web.controller`, les anomalies suivantes ont été identifiées et traitées :

### Anomalie 1 : Exposition directe des entités JPA dans les contrôleurs REST (Règle `java:S4684`)
* **Règle** : *"Persistent entities should not be used as arguments or return types of `@RequestMapping` methods"*.
* **Explication** : L'exposition directe des entités du domaine (`@Entity`) couple rigidement le contrat de l'API web avec le modèle relationnel interne de la base de données. Cela expose inutilement des structures internes, introduit des risques de sur-assignation d'attributs (*over-posting* / *mass assignment*) et peut déclencher des boucles de sérialisation ou des initialisations différées inattendues (`LazyInitializationException`).
* **Correction et justification** : Dans le périmètre de l'Atelier 5, le sujet impose expressément l'exposition directe des entités JPA à titre temporaire (le découplage strict par DTO et validation Bean Validation est l'objet du Chapitre 10). Pour neutraliser les risques immédiats :
  1. Les annotations Jackson (`@JsonManagedReference`, `@JsonBackReference`, `@JsonIgnore`) ont été déployées pour éliminer toute récursion infinie.
  2. Les méthodes de mise à jour (`update()`) recopient uniquement les attributs modifiables autorisés, empêchant l'écrasement intempestif des relations critiques.

### Anomalie 2 : Injection de dépendances par champ (Règle `java:S6813` / Code Smell)
* **Règle** : *"Dependency injection should not be performed via field injection"*.
* **Explication** : L'utilisation de `@Autowired` directement sur les champs privés empêche l'instanciation de la classe en dehors d'un conteneur Spring (compliquant les tests unitaires purs), dissimule les dépendances réelles et favorise l'apparition de dépendances circulaires.
* **Correction** : Remplacement de toute injection par champ par des attributs `private final` combinés avec l'annotation Lombok `@RequiredArgsConstructor`. Le constructeur généré assure l'injection par constructeur recommandée par l'équipe Spring, garantit l'immuabilité des références de service et permet une injection triviale dans les tests unitaires.

---

## E. Questions de Compréhension

### 1. Quel composant reçoit toutes les requêtes, et comment retrouve-t-il la méthode du contrôleur à appeler ?
Toutes les requêtes HTTP sont interceptées par le contrôleur frontal unique de Spring MVC : la **`DispatcherServlet`**.  
Pour déterminer la méthode cible, la `DispatcherServlet` consulte le registre **`HandlerMapping`** (implémentation principale : `RequestMappingHandlerMapping`). Ce composant inspecte l'URI demandée, le verbe HTTP, les en-têtes et le type MIME de la requête, puis fait correspondre ces critères avec les annotations déclarées sur les contrôleurs (`@RequestMapping`, `@GetMapping`, `@PostMapping`, etc.). Une fois la méthode trouvée, un `HandlerAdapter` exécute la méthode en désérialisant les paramètres.

### 2. Pourquoi ContratController dépend-il de IContratService et non de ContratRepository ni de ContratServiceImpl ?
* **Non-dépendance envers `ContratRepository`** : Respect du découpage en couches (architecture n-tiers). Le contrôleur appartient à la couche présentation/web ; il ne doit pas interagir directement avec la base de données ni court-circuiter la couche métier (`Service`), où sont centralisées les validations fonctionnelles, les règles d'intégrité et la gestion des transactions (`@Transactional`).
* **Non-dépendance envers `ContratServiceImpl`** : Respect du principe d'inversion des dépendances (**DIP** - SOLID). La couche web dépend d'un contrat d'interface abstrait (`IContratService`) et non d'une classe concrète. Cela assure un faible couplage, autorise le remplacement de l'implémentation sans toucher au contrôleur et simplifie le mocking dans les tests.

### 3. Pourquoi un POST renvoie-t-il 201 et un DELETE 204 ? Pourquoi une ressource absente donne-t-elle 404 et non 500 ?
* **POST → 201 Created** : La norme HTTP stipule que la création réussie d'une nouvelle ressource sur le serveur doit être notifiée par `201 Created`, souvent accompagnée de la représentation de la ressource créée et de son identifiant.
* **DELETE → 204 No Content** : La suppression s'étant déroulée avec succès, le serveur n'a aucune donnée ni corps de réponse à retourner au client (`204 No Content`).
* **404 Not Found vs 500 Internal Server Error** :  
  * Le code **404** signale une erreur côté client : l'identifiant demandé n'existe pas en base. C'est un cas nominal d'interrogation dans une API REST.
  * Le code **500** signale un bug inattendu ou une rupture technique non gérée côté serveur (exception non rattrapée, base injoignable, NullPointerException). Renvoyer un 500 pour une entité absente induirait en erreur les systèmes de surveillance et masquerait le diagnostic réel.

### 4. Pourquoi PaiementController n'expose-t-il que des GET ? Quelle réponse reçoit un POST /api/paiements ?
* **Raison** : Dans l'architecture métier d'AutoLoc, le paiement est en **composition stricte** avec le contrat (`Contrat ↔ Paiement` avec `CascadeType.ALL` et `orphanRemoval = true`). Un paiement n'a aucune justification d'exister de manière autonome sans son contrat de rattachement. Sa création, modification ou suppression s'effectue exclusivement par le biais de l'agrégat parent `Contrat`.
* **Réponse à un POST `/api/paiements`** : Spring MVC renvoie un code d'erreur **`405 Method Not Allowed`**, intercepté et formaté par le `GlobalExceptionHandler` en corps JSON lisible avec horodatage.

### 5. Quels risques présente l'exposition directe des entités JPA ? Pourquoi Contrat.paiements utilise-t-il @JsonManagedReference alors que Agence.vehicules utilise @JsonIgnore ?
* **Risques de l'exposition directe des entités JPA** :
  1. Couplage excessif entre l'API publique et le modèle relationnel en base de données.
  2. Risque de sur-assignation d'attributs (*over-posting*), permettant au client de modifier des champs non désirés.
  3. Boucles de sérialisation infinies (`StackOverflowError`) sur les relations bidirectionnelles.
  4. Déclenchement de requêtes intempestives N+1 ou d'erreurs de sérialisation sur les proxies LAZY (`No serializer found ... ByteBuddyInterceptor`).
* **Différence `@JsonManagedReference` vs `@JsonIgnore`** :
  * Pour **`Contrat.paiements`** : Il s'agit d'une composition où l'on souhaite que le payload JSON d'un contrat contienne explicitement la liste de ses paiements. L'annotation `@JsonManagedReference` sérialise les enfants tout en collaborant avec `@JsonBackReference` sur `Paiement.contrat` pour couper la boucle inverse vers le parent.
  * Pour **`Agence.vehicules`** : Il s'agit d'une agrégation. Les véhicules sont gérés et consultés via leur propre endpoint (`/api/vehicules`). Sérialiser tous les véhicules d'une agence alourdirait inutilement la réponse. L'annotation `@JsonIgnore` supprime la liste des véhicules de la sérialisation de l'agence, ce qui prévient toute récursion tout en permettant au véhicule de pointer vers son agence par son identifiant.
