# TP 26 — Microservice observable & resilient
### MySQL + Actuator + Profiles + Wait Strategy + Resilience4j + Multi-instances

---

## 1. pricing-service : observable et panne simulable

<p align="center">
  <img src="docs/screenshots/01-pricing-health.png" alt="Actuator health - status UP" width="100%">
</p>

<p align="center">
  <img src="docs/screenshots/02-pricing-api.png" alt="API pricing - prix en JSON" width="100%">
</p>

---

## 2. Multi-instances : 3 conteneurs, 3 hostnames

<p align="center">
  <img src="docs/screenshots/03-instance-1.png" alt="book-service-1" width="100%">
</p>

<p align="center">
  <img src="docs/screenshots/04-instance-2.png" alt="book-service-2" width="100%">
</p>

<p align="center">
  <img src="docs/screenshots/05-instance-3.png" alt="book-service-3" width="100%">
</p>

---

## 3. Donnees partagees (meme MySQL)

<p align="center">
  <img src="docs/screenshots/06-books-partages.png" alt="Livres vus depuis une autre instance" width="100%">
</p>

---

## 4. Console d observabilite (fonctionnalite additionnelle)

<p align="center">
  <img src="docs/screenshots/07-console-observabilite.png" alt="Console - etat nominal" width="100%">
</p>

<p align="center">
  <img src="docs/screenshots/08-console-circuit-open.png" alt="Console - circuit breaker OPEN" width="100%">
</p>

