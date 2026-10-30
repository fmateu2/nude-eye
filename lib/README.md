# Local dependencies

Binary dependencies are intentionally excluded from the public source package.

To build the Java application, place authorized copies of `ApiHelper.jar` and a configurable `CoordinatesAPI.jar` in this directory, and obtain `json-simple` 1.1.1 from its official distribution. Follow each provider's license terms. Do not commit these files or credentials.

The course service address is not included. Supply an authorized endpoint with `-Dnudeeye.api.url=YOUR_ENDPOINT` when launching `Main`. The default is a local development address. `LocalMain` uses file-backed storage; an in-person sample avoids geolocation requests.

The source still depends on these interfaces at compile time. This is a source and documentation package, not a self-contained Java distribution. The portfolio pricing demo runs independently.
