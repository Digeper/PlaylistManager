# PlaylistManager

## What it does

Manages user playlists. Provides CRUD operations for creating, reading, updating, and deleting playlists and playlist songs.

## Local Setup

1. Ensure PostgreSQL is running on `localhost:5432`
2. Create database `postgres` (or update `application.properties`)
3. Update `application.properties` with database credentials
4. Ensure Kafka is running on `localhost:9092`
5. Run: `mvn spring-boot:run`
6. Service starts on port `8092`

## Deployment

Deploy to Kubernetes namespace `muzika`:
```bash
kubectl apply -k k8s/
```

Image: `${ACR_NAME}.azurecr.io/muzika/playlistmanager:latest`

Requires: PostgreSQL database, Kafka cluster, Azure Key Vault secrets, ConfigMap
