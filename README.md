# Лабораторная работа 4 — Вариант 16

Асинхронная многопоточная обработка изображений (обмен цветовых каналов **R↔B** с сохранением альфа-канала).

## Стек

- Java 21, Maven
- Swing (GUI)
- Lombok, Log4j2
- JUnit 5, Mockito

## Слои

- `ui` — Swing GUI  
- `model` — DTO / сущности  
- `service` — вся обработка:
  - `ImageProcessingService` — фасад для UI  
  - `io` — асинхронное чтение/запись  
  - `pipeline` — очередь, пул, сборка, прогресс  
  - `processing` — фильтр R↔B  

Синхронизация: `BlockingQueue`, `Semaphore`, `CountDownLatch`, `synchronized`.

## Запуск

```bash
mvn test
mvn -q exec:java
```

Или:

```bash
mvn -q package
java -jar target/lab-distributed-inf-syst-1.0.0.jar
```

## Параметры в GUI

- **Пул потоков** — размер пула / семафора  
- **Высота тайла** — высота полосы фрагмента  
