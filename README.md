# 🔐 Spring Boot JWT — API REST sécurisée par jeton

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven&logoColor=white)
![H2](https://img.shields.io/badge/Base-H2%20(en%20m%C3%A9moire)-1021FF)

> Projet réalisé dans le cadre du module **[Sécurité des systèmes distribués](https://classroom.google.com/c/ODg4NTM4MTkzMzQ2)**.

| | |
|---|---|
| **Étudiant** | Mustapha ELMIFDALI |
| **Module** | Sécurité des systèmes distribués |
| **Encadrant** | Pr. Abdelmajid BOUSSELHAM |

---

## 📑 Table des matières

1. [Présentation](#-présentation)
2. [Fonctionnalités](#-fonctionnalités)
3. [Technologies utilisées](#-technologies-utilisées)
4. [Architecture du projet](#-architecture-du-projet)
5. [Principe de l'authentification JWT](#-principe-de-lauthentification-jwt)
6. [Prérequis](#-prérequis)
7. [Installation et lancement](#-installation-et-lancement)
8. [Configuration](#-configuration)
9. [Endpoints de l'API](#-endpoints-de-lapi)
10. [Exemples de test](#-exemples-de-test)
11. [Console H2](#-console-h2)
12. [Détails de l'implémentation](#-détails-de-limplémentation)
13. [Limites et améliorations possibles](#-limites-et-améliorations-possibles)
14. [Auteur](#-auteur)

---

## 📖 Présentation

Cette application est une **API REST stateless** développée avec **Spring Boot** et **Spring Security**. Elle illustre un mécanisme d'authentification et d'autorisation basé sur les **JSON Web Tokens (JWT)** :

- l'utilisateur crée un compte (`/auth/register`) ;
- il s'authentifie (`/auth/login`) et reçoit un **jeton JWT signé** ;
- il utilise ce jeton dans l'en-tête `Authorization: Bearer <token>` pour accéder aux ressources protégées (`/api/**`).

Aucune session HTTP n'est conservée côté serveur : chaque requête est authentifiée à partir du jeton qu'elle transporte, ce qui convient aux **architectures distribuées** et aux microservices.

---

## ✨ Fonctionnalités

- ✅ Inscription d'un utilisateur avec **hachage du mot de passe (BCrypt)**
- ✅ Vérification de l'unicité de l'email
- ✅ Authentification par email / mot de passe via `AuthenticationManager`
- ✅ Génération d'un **JWT signé (HMAC-SHA)** avec date d'expiration
- ✅ **Filtre JWT personnalisé** (`OncePerRequestFilter`) validant chaque requête
- ✅ Gestion **stateless** (`SessionCreationPolicy.STATELESS`)
- ✅ Protection des routes `/api/**`, routes `/auth/**` publiques
- ✅ Persistance des utilisateurs avec **Spring Data JPA** + base **H2** en mémoire
- ✅ Console H2 intégrée pour inspecter les données

---

## 🛠️ Technologies utilisées

| Technologie | Rôle |
|---|---|
| **Java 25** | Langage |
| **Spring Boot 4.1.1** | Framework applicatif |
| **Spring Web MVC** | API REST |
| **Spring Security** | Authentification / autorisation |
| **Spring Data JPA (Hibernate)** | Persistance |
| **H2 Database** | Base de données en mémoire |
| **jjwt 0.13.0** (`api`, `impl`, `jackson`) | Création et validation des JWT |
| **Lombok** | Réduction du code répétitif (`@Data`, constructeurs) |
| **Maven (wrapper `mvnw`)** | Build et gestion des dépendances |

---

## 🗂️ Architecture du projet

```text
spring-app-jwt
├── pom.xml
├── mvnw / mvnw.cmd
└── src
    ├── main
    │   ├── java/jwtapp
    │   │   ├── SpringAppJwtApplication.java     # Point d'entrée
    │   │   ├── config
    │   │   │   └── SecurityConfig.java          # Chaîne de filtres, BCrypt, AuthenticationManager
    │   │   ├── controller
    │   │   │   ├── AuthController.java          # /auth/register et /auth/login
    │   │   │   └── UserController.java          # /api/hello et /api/profile (protégés)
    │   │   ├── dtos
    │   │   │   ├── Registerrequest.java         # { email, password }
    │   │   │   ├── Loginrequest.java            # { email, password }
    │   │   │   └── AuthResponse.java            # { token }
    │   │   ├── entities
    │   │   │   └── AppUser.java                 # Entité JPA (table « users »)
    │   │   ├── repositories
    │   │   │   └── UserRepository.java          # findByEmail, existsByEmail
    │   │   ├── security
    │   │   │   └── JwtFilter.java               # Filtre de validation du jeton
    │   │   └── services
    │   │       ├── JwtService.java              # Génération / validation du JWT
    │   │       └── CustomUserDetailsService.java# Chargement de l'utilisateur
    │   └── resources
    │       └── application.properties           # Configuration (H2, JWT)
    └── test
        └── java/jwtapp/SpringAppJwtApplicationTests.java
```

---

## 🔄 Principe de l'authentification JWT

```text
 Client                                         Serveur (Spring Boot)
   │                                                    │
   │ 1. POST /auth/register {email, password}           │
   │ ─────────────────────────────────────────────────► │  mot de passe haché (BCrypt)
   │ ◄───────────────────────────────────────────────── │  « Utilisateur cree »
   │                                                    │
   │ 2. POST /auth/login {email, password}              │
   │ ─────────────────────────────────────────────────► │  AuthenticationManager vérifie
   │ ◄───────────────────────────────────────────────── │  JwtService génère le jeton signé
   │            { "token": "eyJhbGciOi..." }            │
   │                                                    │
   │ 3. GET /api/profile                                │
   │    Authorization: Bearer eyJhbGciOi...             │
   │ ─────────────────────────────────────────────────► │  JwtFilter : extrait + valide le jeton
   │ ◄───────────────────────────────────────────────── │  SecurityContext renseigné → accès OK
   │       « Utilisateur connecte : user@mail.com »     │
```

**Structure du jeton** : `header.payload.signature`

- **subject (`sub`)** : l'email de l'utilisateur
- **issuedAt (`iat`)** : date d'émission
- **expiration (`exp`)** : date d'expiration (1 heure par défaut)
- **signature** : HMAC-SHA calculée avec la clé secrète (`jwt.secret`)

---

## ✅ Prérequis

- **JDK 25** (conforme à `java.version` du `pom.xml`)
- **Maven** (facultatif : le wrapper `mvnw` est fourni)
- Un client HTTP : **cURL**, **Postman** ou **Insomnia**

Vérifier la version de Java :

```bash
java -version
```

---

## 🚀 Installation et lancement

```bash
# 1. Cloner / extraire le projet puis se placer dans le dossier
cd spring-app-jwt

# 2. Lancer l'application
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

Ou bien générer le JAR puis l'exécuter :

```bash
./mvnw clean package
java -jar target/spring-app-jwt-0.0.1-SNAPSHOT.jar
```

L'application démarre par défaut sur **http://localhost:8080**.

Lancer les tests :

```bash
./mvnw test
```

---

## ⚙️ Configuration

Fichier : `src/main/resources/application.properties`

```properties
spring.application.name=spring-app-jwt

# Base de données H2 en mémoire
spring.datasource.url=jdbc:h2:mem:securitydb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Console H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JWT
jwt.secret=<clé secrète encodée en Base64>
jwt.expiration=3600000
```

| Propriété | Description |
|---|---|
| `jwt.secret` | Clé de signature, **encodée en Base64** (au moins 256 bits pour HS256) |
| `jwt.expiration` | Durée de validité du jeton en **millisecondes** (`3600000` = 1 h) |

> ⚠️ **Sécurité** : la clé présente dans le dépôt est une clé de démonstration. En production, elle doit être injectée via une variable d'environnement (ex. `JWT_SECRET`) et ne jamais être versionnée.

---

## 🌐 Endpoints de l'API

| Méthode | URL | Accès | Description |
|:---:|---|:---:|---|
| `POST` | `/auth/register` | 🔓 Public | Crée un compte (rôle `USER`) |
| `POST` | `/auth/login` | 🔓 Public | Authentifie et retourne un JWT |
| `GET` | `/api/hello` | 🔒 JWT requis | Message de bienvenue |
| `GET` | `/api/profile` | 🔒 JWT requis | Retourne l'utilisateur connecté |
| `GET` | `/h2-console/**` | 🔓 Public | Console de la base H2 |

### Corps des requêtes

`POST /auth/register` et `POST /auth/login` :

```json
{
  "email": "mustapha@example.com",
  "password": "MonMotDePasse123"
}
```

### Réponses

| Endpoint | Cas | Réponse |
|---|---|---|
| `/auth/register` | Succès | `200` — `Utilisateur cree` |
| `/auth/register` | Email déjà pris | `400` — `Email deja utilise` |
| `/auth/login` | Succès | `200` — `{ "token": "eyJ..." }` |
| `/auth/login` | Identifiants invalides | Erreur d'authentification (accès refusé) |
| `/api/**` | Jeton absent / invalide / expiré | Accès refusé (`403`) |

---

## 🧪 Exemples de test

### 1. Créer un utilisateur

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"mustapha@example.com","password":"MonMotDePasse123"}'
```

### 2. Se connecter et récupérer le jeton

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"mustapha@example.com","password":"MonMotDePasse123"}'
```

Réponse :

```json
{ "token": "eyJhbGciOiJIUzM4NCJ9...." }
```

### 3. Appeler une route protégée

```bash
TOKEN="<coller_le_jeton_ici>"

curl http://localhost:8080/api/hello \
  -H "Authorization: Bearer $TOKEN"

curl http://localhost:8080/api/profile \
  -H "Authorization: Bearer $TOKEN"
```

Résultats attendus :

```text
Bienvenue dans l'API securisee
Utilisateur connecte : mustapha@example.com
```

### 4. Appel sans jeton (accès refusé)

```bash
curl -i http://localhost:8080/api/hello
```

### Avec Postman

1. `POST /auth/login` → copier la valeur de `token`.
2. Dans la requête vers `/api/...`, onglet **Authorization** → type **Bearer Token** → coller le jeton.

---

## 🗄️ Console H2

Accessible sur **http://localhost:8080/h2-console** une fois l'application lancée.

| Champ | Valeur |
|---|---|
| JDBC URL | `jdbc:h2:mem:securitydb` |
| User Name | `sa` |
| Password | *(vide)* |

Requête utile :

```sql
SELECT * FROM users;
```

Le mot de passe est stocké **haché en BCrypt**, jamais en clair.

---

## 🧩 Détails de l'implémentation

| Composant | Responsabilité |
|---|---|
| **`SecurityConfig`** | Désactive CSRF (API stateless), impose `STATELESS`, déclare les routes publiques/protégées, autorise l'affichage de la console H2 (`frameOptions.sameOrigin`), insère `JwtFilter` avant `UsernamePasswordAuthenticationFilter`, expose `BCryptPasswordEncoder` et `AuthenticationManager`. |
| **`JwtFilter`** | Lit l'en-tête `Authorization`, extrait le jeton après `Bearer `, récupère l'utilisateur, valide le jeton puis renseigne le `SecurityContext`. |
| **`JwtService`** | Génère le jeton (`subject`, `issuedAt`, `expiration`, signature), extrait les claims et vérifie la validité (signature, nom d'utilisateur, expiration). |
| **`CustomUserDetailsService`** | Charge un `AppUser` par email et le convertit en `UserDetails` Spring Security avec son rôle. |
| **`AuthController`** | Expose l'inscription (avec hachage BCrypt) et la connexion (retourne le JWT). |
| **`UserController`** | Expose deux ressources protégées sous `/api`. |
| **`AppUser` / `UserRepository`** | Entité JPA `users` (`id`, `email` unique, `password`, `role`) et son accès aux données. |

---

## 🔧 Limites et améliorations possibles

Ce projet est volontairement simple et pédagogique. Pistes d'amélioration :

- 🔑 Externaliser `jwt.secret` (variables d'environnement, coffre de secrets).
- 🧾 Ajouter la **validation des entrées** (`@Valid`, format d'email, complexité du mot de passe).
- 🚫 Renvoyer des réponses **401 / 403 JSON** explicites via un `AuthenticationEntryPoint` personnalisé et un gestionnaire d'exceptions global.
- 🪪 Inclure le **rôle** dans les claims du JWT et protéger les routes par rôle (`hasRole("ADMIN")`).
- ♻️ Mettre en place un **refresh token** et la révocation / déconnexion.
- 🗃️ Remplacer H2 par une base persistante (PostgreSQL, MySQL).
- 🔒 Désactiver la console H2 et utiliser **HTTPS** en production.
- 🧪 Ajouter des tests d'intégration (`MockMvc`, `spring-security-test`).

---

## 👤 Auteur

**Mustapha ELMIFDALI**
Module : [Sécurité des systèmes distribués](https://classroom.google.com/c/ODg4NTM4MTkzMzQ2)
Encadrant : **Pr. Abdelmajid BOUSSELHAM**

---

<p align="center">Réalisé avec ❤️ dans le cadre du module <i>Sécurité des systèmes distribués</i></p>
