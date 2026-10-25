# Stratégie de Chargement (Fetch) et Propagation (Cascade) — AutoLoc API

## 1. Contexte et Objectifs

Ce document formalise les décisions d'architecture et de conception JPA pour le modèle relationnel de la plateforme **AutoLoc**.  
Dans une application d'entreprise bâtie sur Spring Data JPA et Hibernate, le choix des stratégies de chargement (`FetchType`) et des règles de propagation du cycle de vie (`CascadeType`, `orphanRemoval`) est déterminant pour :
- **Les performances et l'empreinte mémoire** : éviter les requêtes N+1 et l'explosion cartésienne des données chargées.
- **L'intégrité référentielle et métier** : respecter le cycle de vie réel des entités (composition vs agrégation) et prévenir la corruption ou la perte accidentelle de données.
- **La maintenabilité et la robustesse** : éliminer les effets de bord tels que les `StackOverflowError` ou les dépendances de persistance cachées.

---

## 2. Matrice Récapitulative des Associations

| Association | Multiplicité | Côté Propriétaire | Fetch | Cascade / orphanRemoval | Justification Métier & Technique |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Contrat → Paiement** | `@OneToMany` / `@ManyToOne` (Bidirectionnelle) | `Paiement` (`contrat_id_contrat`) | **LAZY** | **ALL** + **orphanRemoval = true** | **Composition stricte** : un paiement n'a aucun sens sans son contrat d'origine. Supprimer un contrat doit obligatoirement supprimer ses paiements (`REMOVE`). Retirer un paiement de la collection doit le purger en base (`orphanRemoval`). Chargement différé pour alléger les lectures de contrat. |
| **Agence → Vehicule** | `@OneToMany` / `@ManyToOne` (Bidirectionnelle) | `Vehicule` (`agence_id_agence`) | **LAZY** | **Aucune** | **Agrégation** : un véhicule est un actif physique indépendant qui survit à son agence (il peut être réaffecté à une autre agence). La fermeture d'une agence ne doit en aucun cas supprimer la flotte de véhicules. |
| **Agence → Employe** | `@OneToMany` / `@ManyToOne` (Bidirectionnelle) | `Employe` (`agence_id_agence`) | **LAZY** | **Aucune** | **Agrégation** : un employé est une personne autonome dans le système RH. La suppression ou réorganisation d'une agence ne doit pas supprimer les employés de la base de données. |
| **Vehicule ↔ Equipement** | `@ManyToMany` (Bidirectionnelle) | `Vehicule` (`vehicule_equipement`) | **LAZY** | **Aucune** | **Données de référence partagées** : les équipements (GPS, siège bébé, etc.) sont mutualisés entre de multiples véhicules. Supprimer un véhicule ne doit jamais supprimer l'équipement de référence. L'usage d'un `Set` garantit l'unicité et optimise les requêtes de jointure. |
| **Client → Reservation** | `@OneToMany` / `@ManyToOne` (Bidirectionnelle) | `Reservation` (`client_id_client`) | **LAZY** | **PERSIST** | **Cycle de vie asymétrique** : lors de l'onboarding d'un nouveau client avec réservation immédiate, `PERSIST` propage l'enregistrement sans requêtes manuelles intermédiaires. En revanche, aucune cascade de suppression (`REMOVE`) n'est appliquée pour préserver l'historique comptable et contractuel. |
| **Reservation → Vehicule** | `@ManyToOne` (Unidirectionnelle) | `Reservation` (`vehicule_id_vehicule`) | **LAZY** | **Aucune** | **Référence opérationnelle** : une réservation fait référence à un véhicule du parc. Aucune cascade de suppression : le véhicule doit absolument subsister lors de l'annulation ou suppression d'une réservation. Association unidirectionnelle car un véhicule n'a pas besoin de maintenir en mémoire la liste de toutes ses réservations historiques. |
| **Reservation ↔ Contrat** | `@OneToOne` (Bidirectionnelle) | `Contrat` (`reservation_id_reservation`) | **LAZY** | **ALL** (côté `Reservation`) | **Couplage fort 1-to-1** : le contrat est l'officialisation juridique d'une réservation validée. Propager `ALL` depuis `Reservation` permet d'automatiser le cycle de vie du contrat rattaché (création, mise à jour, annulation/suppression). |
| **Vehicule → Maintenance** | `@OneToMany` / `@ManyToOne` (Bidirectionnelle) | `Maintenance` (`vehicule_id_vehicule`) | **LAZY** | **PERSIST** | **Journal de bord technique** : planifier ou renseigner une maintenance lors de l'enregistrement d'un véhicule bénéficie de `PERSIST`. Aucune cascade de suppression automatique (`REMOVE`) pour garantir la conservation de l'historique d'entretien obligatoire (traçabilité, revente, sécurité). |

---

## 3. Analyse Détaillée des Stratégies de Chargement (FetchType)

### 3.1 Pourquoi la recommandation globale est `FetchType.LAZY` ?
Dans Hibernate / JPA :
- Les relations `@ManyToOne` et `@OneToOne` sont chargées en **EAGER par défaut**.
- Les relations `@OneToMany` et `@ManyToMany` sont chargées en **LAZY par défaut**.

Dans **AutoLoc**, nous forçons explicitement `fetch = FetchType.LAZY` sur **toutes** les associations, y compris les relations To-One (`@ManyToOne`, `@OneToOne`).

#### Risques majeurs d'un chargement `EAGER` :
1. **Effet boule de neige (requêtes en cascade)** :
   Charger une `Reservation` en mode EAGER chargerait immédiatement son `Client`, son `Vehicule`, et son `Contrat`. Ce dernier chargerait à son tour tous ses `Paiements`. Le `Vehicule` chargerait son `Agence`, ses `Maintenances` et ses `Equipements`. Une simple consultation de réservation exécuterait alors des dizaines de requêtes `LEFT OUTER JOIN` ou `SELECT`, saturant la mémoire JVM et la bande passante de la base MySQL.
2. **Problème du N+1 SELECT** :
   Lorsqu'une liste d'entités est récupérée (ex: liste de 50 véhicules), une association EAGER déclenche 50 requêtes SQL supplémentaires pour rapatrier les entités liées.
3. **Impossibilité d'optimiser a posteriori** :
   Une association déclarée statiquement en `EAGER` ne peut plus jamais être déchargée à la demande. À l'inverse, une association configurée en `LAZY` peut être chargée au besoin de manière ciblée (requêtes avec `JOIN FETCH`, `@EntityGraph`, ou DTO projections dédiées).

### 3.2 Gestion de la `LazyInitializationException`
L'utilisation de `FetchType.LAZY` implique que l'accès aux collections ou mandataires (proxies) doit s'effectuer au sein d'une transaction active (`@Transactional`). Pour les cas d'usage nécessitant le chargement simultané d'entités liées (ex: affichage d'un contrat avec ses paiements), les requêtes personnalisées de repository avec `JOIN FETCH` seront déployées (Atelier 7).

---

## 4. Analyse du Cycle de Vie et Propagation (CascadeType & orphanRemoval)

### 4.1 Composition vs Agrégation
- **Composition (Cycle de vie dépendant)** :
  - Cas de `Contrat ↔ Paiement` : Un reçu de paiement est indissociable du contrat qu'il honore. `CascadeType.ALL` permet de propager `PERSIST`, `MERGE`, et `REMOVE`. `orphanRemoval = true` garantit que si une ligne de paiement est retirée de la liste Java `paiements`, Hibernate génère automatiquement le `DELETE` SQL correspondant afin d'éviter les enregistrements fantômes orphelins.
  - Cas de `Reservation ↔ Contrat` : La réservation pilote juridiquement le contrat. La cascade `ALL` depuis `Reservation` assure une cohérence parfaite de l'état du contrat.
- **Agrégation (Cycle de vie indépendant)** :
  - Cas de `Agence → Vehicule` et `Agence → Employe` : Les entités enfants possèdent leur propre existence métier. Appliquer `CascadeType.REMOVE` provoquerait la perte catastrophique d'actifs de valeur (véhicules) ou de données RH (employés).
  - Cas de `Vehicule ↔ Equipement` : Les équipements sont des entités partagées référentielles. Aucune cascade ne doit s'appliquer.

### 4.2 Utilisation ciblée de `CascadeType.PERSIST`
- Pour `Client → Reservation` et `Vehicule → Maintenance`, seul `PERSIST` est autorisé. Cela permet de créer commodément une entité parent avec ses entités associées lors d'un flux d'initialisation, sans risquer de suppressions accidentelles en cascade lors d'opérations ultérieures de maintenance de données.

---

## 5. Bonnes Pratiques d'Implémentation & Clean Code

1. **Exclusion stricte de `@Data`** :
   Toutes les entités utilisent uniquement `@Getter`, `@Setter`, `@NoArgsConstructor` et `@AllArgsConstructor`. L'utilisation de `@Data` génère automatiquement des méthodes `equals()`, `hashCode()` et `toString()` qui parcourent récursivement les associations bidirectionnelles, causant un dépassement de pile (`java.lang.StackOverflowError`).
2. **Initialisation des collections** :
   Les collections (`List<Paiement>`, `List<Vehicule>`, `Set<Equipement>`, etc.) sont explicitement initialisées avec `new ArrayList<>()` ou `new HashSet<>()`. Cela protège le code applicatif des `NullPointerException` avant la persistance de l'entité.
3. **Usage de `Set` pour `@ManyToMany`** :
   L'association `Vehicule ↔ Equipement` emploie un `java.util.Set` afin d'éviter la génération inefficace de multiples requêtes de suppression/réinsertion complète typiques des listes JPA lors de modifications de tables de jointure.
4. **Précision du `mappedBy`** :
   Le côté inverse de chaque relation bidirectionnelle utilise scrupuleusement la propriété `mappedBy` pointant vers l'attribut exact du côté propriétaire, empêchant la création involontaire de tables de jointure superflues.
