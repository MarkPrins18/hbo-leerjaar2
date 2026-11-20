# Database

- H2-bestand (`MODE=MySQL`) via Exposed. Bij elke start wordt het schema gereset en opnieuw geseed.
- Nieuwe tabel: toevoegen aan `allTables` in `TableRegistry.kt`, seeddata in `resources/data/seed/` en `seedFilePaths` in `DatabaseSeeder.kt`.
- Nieuwe tabel die naar `car` verwijst: ook opnemen in `deleteCar`.
