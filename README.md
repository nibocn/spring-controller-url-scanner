# Spring Controller URL Scanner

An IntelliJ IDEA plugin that scans project source code and dependency JARs for Spring MVC controller mappings.

Plugin ID: `me.nibo.spring-url-scanner`

Source: <https://github.com/nibocn/spring-controller-url-scanner>

## Features

- Scans Project + Libraries using IntelliJ PSI/indexes.
- Recognizes:
  - `@RestController`
  - `@Controller`
  - `@RequestMapping`
  - `@GetMapping`
  - `@PostMapping`
  - `@PutMapping`
  - `@DeleteMapping`
  - `@PatchMapping`
- Combines class-level and method-level paths.
- Shows HTTP method, URL, controller, handler method, and source JAR.
- Filters by URL / class / handler / JAR name.
- Copies the selected endpoint cell, or the whole endpoint row from the context menu.
- Shows scan progress in the tool window.
- Exports all scanned endpoints to CSV.
- Double-click a row to navigate to source or decompiled class.

## Build

Requires JDK 21. Use the Gradle Wrapper for reproducible builds:

```bash
./gradlew buildPlugin
```

The ZIP plugin package will be under:

```text
build/distributions/
```

## Run in a development IDE

```bash
./gradlew runIde
```

Then open a Java/Spring project in the development IDE and open:

```text
View -> Tool Windows -> Spring URLs
```

Click **Scan Controllers**.

## Notes / current limitations

This is intentionally an MVP static scanner. It does not fully resolve every dynamic Spring mapping case, for example:

- custom composed annotations built on top of Spring mapping annotations;
- paths assembled by complex constants/expressions;
- runtime path changes from servlet context path, gateway/proxy routing, or programmatic handler mappings;
- inherited/interface mappings in every edge case.

For runtime truth, Spring Boot Actuator `/actuator/mappings` remains the most authoritative option.

## Privacy

The scanner runs locally inside the IDE. It does not upload source code, collect telemetry, or contact external services.

## License

MIT License. See [LICENSE](LICENSE).

## FeignClient scanning

The tool window includes an **Include @FeignClient** checkbox. It is **disabled by default**.

When enabled, the scanner also includes interfaces/classes annotated with:

- `org.springframework.cloud.openfeign.FeignClient`
- legacy `org.springframework.cloud.netflix.feign.FeignClient`

Spring mapping annotations declared on Feign methods (`@GetMapping`, `@PostMapping`, `@RequestMapping`, etc.) are collected in the same result table.


## Result columns

The table includes `Type`, `Method`, `URL`, `Controller`, `Handler`, and `Source`. `Type` distinguishes `Controller` from `FeignClient`.
