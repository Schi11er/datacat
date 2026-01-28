# GraphQL Performance Optimierung für große Datenmengen

## Problem

Bei der Abfrage von `concepts` aus einem Dictionary mit ~1000 oder mehr Objekten trat folgender Fehler auf:

```
org.springframework.transaction.TransactionSystemException: Could not open a new Neo4j session: 
Connection pool pending acquisition queue is full.
```

### Ursache

Das Problem entstand durch das GraphQL N+1-Problem in Kombination mit parallelen Batch-Anfragen:

1. `getConcepts` liefert eine Page mit vielen `XtdObject`-Instanzen (z.B. 1000)
2. Für jedes dieser Objekte wird das `@BatchMapping` in `ObjectController.getName()` aufgerufen
3. Jede BatchMapping-Operation öffnet eine neue Transaktion
4. Bei 1000+ Objekten werden gleichzeitig zu viele Transaktionen geöffnet
5. Der Neo4j Connection Pool (max 50 Connections) wird überlastet
6. Die Acquisition Queue läuft voll → Fehler

## Implementierte Lösungen

### 1. Connection Pool Vergrößerung (`application.yml`)

```yaml
spring:
  neo4j:
    pool:
      max-connection-pool-size: 150           # Erhöht von 50
      connection-acquisition-timeout: 90s     # Erhöht von 60s
      max-acquisition-queue-size: 1000        # NEU: Größere Queue
```

**Warum:** Mehr parallele Connections ermöglichen, Queue-Kapazität erhöhen.

### 2. Read-Only Transaktionen

Alle Lesevorgänge wurden mit `@Transactional(readOnly = true)` annotiert:

- `CatalogServiceImpl.getNamesForMultipleIds()`
- `DictionaryRecordServiceImpl.getConcepts()`
- `ObjectController.getName()` (BatchMapping)
- `DictionaryController.getName()` (BatchMapping)

**Warum:** 
- Read-Only Transaktionen sind performanter
- Weniger Locking
- Optimierte Connection-Nutzung

### 3. GraphQL DataLoader Konfiguration (`GraphQLConfig.java`)

```java
registry.setDefaultBatchSize(200);              // Batch-Größe begrenzen
registry.setDefaultScheduleDelay(Duration.ofMillis(10));  // Batching optimieren
```

**Warum:**
- Statt 1000 Objekte auf einmal → 5 Batches à 200 Objekte
- Reduziert gleichzeitige Connection-Anforderungen
- Schedule Delay bündelt mehrere Anfragen zusammen

### 4. Virtual Threads & Task Execution

```yaml
spring:
  threads:
    virtual:
      enabled: true
  task:
    execution:
      simple:
        concurrency-limit: 200
```

**Warum:** Virtual Threads (Java 21) ermöglichen besseres paralleles Arbeiten bei I/O-Operationen.

## Best Practices für zukünftige Entwicklung

### 1. Immer BatchMapping verwenden

✅ **Gut:**
```java
@BatchMapping
public Map<XtdObject, Optional<String>> getName(List<XtdObject> objects) {
    List<String> ids = objects.stream().map(XtdObject::getId).toList();
    Map<String, String> namesById = service.getNamesForMultipleIds(ids);
    return objects.stream().collect(Collectors.toMap(
        obj -> obj,
        obj -> Optional.ofNullable(namesById.get(obj.getId()))
    ));
}
```

❌ **Schlecht:**
```java
@SchemaMapping
public String getName(XtdObject object) {
    return service.getNameForSingleId(object.getId());  // N+1 Problem!
}
```

### 2. Read-Only für Abfragen

Alle GraphQL-Query-Methoden sollten `@Transactional(readOnly = true)` haben:

```java
@QueryMapping
@Transactional(readOnly = true)
public Connection<XtdObject> findObjects(@Argument FilterInput input) {
    // ...
}
```

### 3. Paginierung nutzen

Bei großen Datenmengen immer Paginierung verwenden:

```java
@SchemaMapping
public Connection<XtdObject> getConcepts(
    XtdDictionary dictionary,
    @Argument Integer pageSize,     // Default: 20
    @Argument Integer pageNumber    // Default: 0
) {
    if (pageSize == null) pageSize = 20;
    if (pageNumber == null) pageNumber = 0;
    
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    Page<XtdObject> page = service.getConcepts(dictionary, pageable);
    return Connection.of(page);
}
```

### 4. Batch-Größe monitoring

Bei Bedarf Batch-Größe anpassen:

```java
// Für sehr große Datensätze (>5000):
registry.setDefaultBatchSize(100);

// Für kleine bis mittlere Datensätze (<500):
registry.setDefaultBatchSize(500);
```

## Monitoring & Troubleshooting

### Logging aktivieren

Zum Debuggen in `application.yml`:

```yaml
logging:
  level:
    org.neo4j.driver: DEBUG                    # Connection Pool Monitoring
    org.springframework.graphql: DEBUG         # GraphQL Execution
    org.springframework.data.neo4j: DEBUG      # Neo4j Queries
```

### Typische Probleme

1. **Zu kleine Batch-Size:** Viele kleine Batches → viele DB-Aufrufe
2. **Zu große Batch-Size:** Wenige große Batches → Connection Pool überlastet
3. **Fehlende @Transactional(readOnly = true):** Unnötiges Locking, schlechtere Performance

### Performance-Metriken prüfen

Neo4j Driver Metriken sind aktiviert:
```yaml
spring:
  neo4j:
    pool:
      metricsEnabled: true
```

Diese können über Actuator abgerufen werden (falls konfiguriert).

## Zusammenfassung

Die Kombination aus:
- ✅ Größerem Connection Pool (150 statt 50)
- ✅ Read-Only Transaktionen
- ✅ Optimierter Batch-Size (200)
- ✅ Virtual Threads

sollte das Problem mit "Connection pool pending acquisition queue is full" beheben und auch bei 1000+ Objekten stabile Performance bieten.

## Test-Empfehlung

1. Anwendung neu starten
2. Dictionary mit 1000+ Konzepten abfragen
3. Performance bei verschiedenen Seitengrößen testen (20, 50, 100, 200)
4. Bei Bedarf Batch-Size in `GraphQLConfig.java` anpassen
