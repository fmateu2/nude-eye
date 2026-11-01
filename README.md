# Nude Eye

### An optical shop, modeled in Java.

A two-person academic console application exploring object-oriented domain modeling, layered architecture and interchangeable persistence.

**Authors:** Francesc Mateu Vallespín and Jan Francesc Porcar Guillen. Developed for Object-Oriented Design and Programming at La Salle — Universitat Ramon Llull.

## What it does

Users can register or select a client identity, browse optical products and provider offers, build a cart, complete a simulated purchase and inspect purchase history. The client-ID selection flow is an academic simplification, not secure authentication.

The later project phases introduce specialized client and product types, plus startup selection between a course REST API and local JSON/CSV storage.

## Architecture

| Layer | Responsibility |
| --- | --- |
| Presentation | Console menus, input collection and orchestration through `Controller` |
| Business | Clients, products, providers, carts, sales and their managers |
| Persistence | Four DAO contracts, with API and local-file implementations |

The persistence contracts cover products, clients, providers and sales. Client/product/provider data use JSON locally; sales use CSV. Managers can receive DAO implementations through constructors, making the dependencies explicit.

## Design artifact

The original UML class diagram is included in `docs/class-diagram.pdf`. It maps the presentation, business and persistence layers, class hierarchies and DAO relationships. The portfolio provides a cropped vector preview and the original PDF for detailed inspection. The project was co-developed by the two authors credited above.

## Domain behavior

The `Product` hierarchy includes glasses, contact lenses, consumables and services. Each implements or inherits `calculatePrice`, allowing the caller to use the common product contract while behavior changes by subtype.

The client model includes in-person, online and corporate clients. Shipping and VAT decisions depend on client type and the project's configured rules.

| Product | Implemented pricing behavior |
| --- | --- |
| Glasses | Base price with optional 1.21 multiplier |
| Contact lenses | Base price with optional 1.10 multiplier |
| Consumables | Optional 1.21 multiplier, then 0.60 near-expiry multiplier |
| Services | Hourly rate × duration, with optional 1.21 multiplier |

These are the constants used by the assignment. The near-expiry check uses the current date and an exclusive three-month threshold; it also classifies already-expired items as near expiry.

## Persistence selection

The original `Main` launches a controller that probes the course API. If it is unavailable, or API initialization fails, the controller attempts file-backed managers. If local initialization also fails, the application stops.

This is startup mode selection. It is not replication, transactional failover or automatic synchronization between API and file storage.

## Build and run

**Availability:** this GitHub repository is a source and design portfolio. Building it requires separately obtained course dependencies; the independent browser pricing demo is available in the portfolio.

Use a JDK compatible with your dependency copies. The course binaries supplied with the original project require **JDK 25** (class-file version 69); JDK 21 cannot load those binaries. Binary dependencies are not distributed here. First obtain authorized copies of the course API and geolocation libraries and the official `json-simple` 1.1.1 distribution; see [`lib/README.md`](lib/README.md). Place them in `lib/` locally. The source will not compile without these dependencies. Run from the project root so relative data paths resolve correctly.

macOS/Linux:

```bash
mkdir -p build
find src -name '*.java' > sources.txt
printf '%s\n' Main.java LocalMain.java >> sources.txt
javac -proc:none -cp 'lib/*' -d build @sources.txt
java -cp 'build:lib/*' LocalMain
```

PowerShell:

```powershell
New-Item -ItemType Directory -Force build | Out-Null
$sources = @(Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName) + @('Main.java', 'LocalMain.java')
javac -proc:none -cp 'lib/*' -d build $sources
java -cp 'build;lib/*' LocalMain
```

`LocalMain` is a packaging addition that uses the existing injection constructor to select file persistence without probing the course API. The included fixture has an in-person client with ID **1**, four products and one provider. It is deliberately illustrative; it does not include the original customer or sales records.

Online-client flows may require geolocation. Supply your own authorized, configurable dependency and credential outside the source code. The in-person `LocalMain` example avoids that remote flow.

The original course endpoint is omitted. `Main` defaults to `http://localhost:8080`; provide an authorized service address with `-Dnudeeye.api.url=YOUR_ENDPOINT` only when you intend to use remote storage. Remote actions can read or write the service you configure.

## Source guide

- `Main.java`: original entry point.
- `LocalMain.java`: added local-persistence entry point.
- `src/business/`: domain classes, managers and business exceptions.
- `src/presentation/`: controller and console interface.
- `src/persistence/dao/`: four persistence interfaces.
- `src/persistence/impl/`: eight API/file implementations.
- `lib/README.md`: instructions for obtaining dependencies locally; no JARs or credentials are distributed.
- `docs/class-diagram.pdf`: original vector UML class diagram, presented as part of Francesc’s work on the collaborative project.
- `docs/technical-report.pdf`: original academic report with UML and design decisions.

## Verification and boundaries

The portfolio case study is based on source inspection and the report. The interactive pricing illustration reproduces the source formulas in JavaScript; it is not the Java application running in the browser.

Compilation and full application execution with the required dependencies and JDK 25 have not been verified. Source inspection and the independent pricing demo do not establish that the complete Java application runs. Build commands and the additional local entry point should be checked in a JDK-equipped environment before relying on them. No remote API operations were performed.

Further engineering work would include unit tests for pricing and persistence, a controllable clock for expiry rules, decimal money handling instead of floating-point values, and stronger authentication and transaction handling for any real deployment.

## Credits and packaging

The original Java implementation is retained. Packaging adds this README, `LocalMain.java`, a clean ignore file and fresh sample data. IDE state, compiled outputs, duplicated files and original customer/sales records are omitted. Course binaries and remote service addresses are omitted from the public package. The website download includes a separately prepared dependency bundle, while this GitHub repository intentionally omits course binaries. Use the instructions for the package you obtain; compatibility and reuse permissions must be checked for any supplied dependencies.

No individual component ownership is claimed beyond the joint authorship in the report. No new licence is assigned to the project or the separately obtained course dependencies; retain their original terms and obtain author permission for reuse.
