# sylon-wealth-management-api
Wealth Management Platform

## Dependencies

* Java 11
* Maven 3

## Build

```
mvn clean package
```

## Run

### Real Backend

API Key available on request
```
java -jar /path/to/sylon-wealth-management-api-0.0.1-SNAPSHOT.jar --app.backend.api.key <API_KEY>
```
or
```
mvn springboot:run -Dapp.backend.api.key=<API_KEY>
```

### Mock Backend

See https://github.com/jmamcallister/sylon-wealth-management-backend-mock for details
on how to run mock backend server locally (port 8081 is an example)
```
java -jar /path/to/sylon-wealth-management-api-0.0.1-SNAPSHOT.jar --app.backend.url=http://localhost:8081
```
or
```
mvn springboot:run -Dapp.backend.url=http://localhost:8081
```

## Test

Swagger UI available at http://sylondemo-env.eba-iuk4hjc4.eu-west-1.elasticbeanstalk.com/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config

Swagger also locally available at http://localhost:8080/swagger-ui.html if running on
standard Springboot configuration.

Credentials available on request.

A typical flow:

* GET /search?query=<stock or company name>
  * Note symbols of interesting stocks
* GET /watchlists
  * Note id values of watchlist(s)
* PUT /watchlists/:id
  * Add symbol(s) to your watchlist
* GET /watchlists/:id/quotes
  * Get daily quote for all stocks in watchlist

## Design

### Search

This is essentially a pass-through to a backend service provider.

### Watchlists

The feature request for a user to be able to follow and unfollow stocks they are
interested in seemed to lend itself to a CRUD style service, so a RESTful implementation
seemed appropriate, with an additional feature to be able to call out to a backend
service to get detailed information on followed stocks on each request.

### Domain

From the start, the idea was to design a set of core business domain models which
would be independent of both any backend service types and frontend response types.
These core models can then be used in service-layer interface definitions, so the endpoint
implementation (in this case Springboot controller) can operate on these objects
without having to make changes should any backend service implementations be changed.
This applies to both the search and watchlist features.

The core model is also used in interface definitions of what would be a persistence
implementation for the same reason - to keep the core business domain free from any
constraints imposed by specific technologies.

### Development Process

A Behaviour Development Driven approach was taken after the initial skeleton project and search
functionality were completed (this was in part to get a basic structure in place to run
things like `mvn test`, and partly to learn a bit about the capabilities of the Alpha Vantage
backend service in the context of this application).

The method of this was to define actual test methods using Springboot Test, leave them blank,
then fix failing tests with each new use case.

### Trade-Offs

Spring 5 WebClient works well with HTTP response bodies and can easily map to a Java type, and
can have error-handling behaviour easily defined on HTTP response codes of 4xx or 5xx. Given that
the Alpha Vantage API creators have chosen to respond with 200 for all scenarios required by
our application with widely-varying response content for the same resource, it was necessary
to take a simpler modelling approach with the WebClient itself and grab everything as a string,
then perform our own "manual" marshalling of the JSON object and interrogating the response
to decide on the next step.

I would have liked to provide the facility for users to easily register themselves and create Basic
Auth credentials, however since there is only minimal security, no built-in rate limiting, and the
cloud hosting provider was not chosen till later (which may provide rate-limiting), the user credentials
are hard-coded and thus visible in the source code, and also requires a rebuild and redeploy
for new credentials to be added (and reaching out to the developer to request new credentials).
Not great for a demo scenario, but it should suffice.

## Wishlist

What to implement/improve with more time:

* Real authentication (currently relying on hard-coded Basic Auth
  credentials), e.g., JWT or Bearer token, with CSRF prevention
* API Gateway implementation, e.g., Springboot gateway to implement
  things like rate limiting
* More unit tests of error handling is rarely a bad thing
* Functional API tests including performance and concurrency tests, e.g. using Gatling
* More separation of backend-related models to a specific Java package, to enable easier
  refactoring (i.e., deleting) if a new backend provider is chosen (just moving
  them to the appropriate java package)
* Remote configuration, in line with 12-factor app principals, e.g., Spring Cloud Config  
* Verify core model design against a candidate persistence technology, e.g., H2, or an
  im-memory Mongo (to see if extensive mapping is required between core model and
  persistence model, and if common issues were observed in mapping to an SQL and a NoSQL
  Object Relational Mapper)
* Version the APIs, most likely with a "version" in the path, e.g. `/api/v1/resource` (content
  negotiation based on vendor-specific headers might also be investigated)
* More custom validators to define API responses to bad requests with helpful messages
* Conditional deployment of service implementations, e.g., ability to deploy "Alpha Vantage"
  backend implementation, or some other backend implementation with just configuration
  parameter (crude form of feature toggling)