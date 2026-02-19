# Logging Review Guidelines for Pull Requests

## Objective

The purpose of logging improvements is to enhance the quality, clarity, and effectiveness of application logs.

Well-designed and adequately placed logs are critical for:

- Improving observability and operational visibility.
- Simplifying troubleshooting and root cause analysis.
- Supporting production monitoring and incident response.
- Reducing debugging time during development and maintenance.
- Providing meaningful audit trails for significant system actions.
- Enabling end users and support teams to effectively debug product flows when issues occur.

Adequate logging ensures that important execution paths, state transitions, handled failures, and key decision points in product flows are visible without exposing sensitive information or creating unnecessary noise.

## Scope of Review

The following files are exempt from the logging enhancement guidelines.

- configuration files (such as XML, YAML, JSON).
- files that:
    * Contain only constants (e.g., `Constants`, `Config`, `Defaults`).
    * Contain only static final declarations.
    * Are test files (filename contains `"Test"`).
    * Do not use a known logging library.

## Logging Principles

### Log Levels

* **INFO**

    * For major actions and significant state transitions.
    * Avoid repetitive or trivial messages.
    * Do not log errors as INFO.

* **DEBUG**

    * For troubleshooting and diagnostic information.
    * Must be disabled by default.
    * Guard all non-trivial debug logs with:

      ```java
      if (log.isDebugEnabled()) {
          log.debug("...");
      }
      ```

* **WARN**

    * For deviations from expected behavior.
    * The system remains functional.

* **ERROR**

    * Only when a feature fails and the exception is handled.
    * Do NOT log if the exception is rethrown or wrapped.
    * Log only the error message. Do NOT include stack traces or error objects.
    * Do NOT log client errors as ERROR logs. Use WARN or DEBUG logs for logging client errors.

## What Not to Log

* Do not log information such as:
    * Emails
    * Passwords
    * Tokens
    * Secrets
    * Personally identifiable information
* If necessary, log only a safe identifier that is neither sensitive or PII.
* Do not log entire objects. Extract only relevant safe fields required for clarity.
* Do not add logs to:
    * Getters and setters without business logic.
    * Enums.
    * Pure variable declarations.
    * Constant-only files.

## Where to Add Logs

Add logs around:

* Major method executions.
* Important business logic decisions.
* Exceptional but handled scenarios.
* Branches with meaningful functional impact.

Do not add logs if:

* The function only sets a variable and returns it.
* The catch block rethrows the exception.
* The change is purely structural or configuration-related (eg: retrieving a configuration value etc).

## Log Quality Requirements

* Maximum log line length: **120 characters**.
* Logs must be:
    * Context-aware
    * Clear
    * Concise
    * Non-repetitive
* Avoid potential `NullPointerException`.
* Maintain original indentation.
* Do not change the code structure.
* Do not format or refactor existing code.
