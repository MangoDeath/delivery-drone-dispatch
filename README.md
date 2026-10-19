# Delivery Drone Dispatch

Java 17 console project demonstrating the Facade pattern. It coordinates a
three-step drone delivery and stores state in SQLite so unfinished work can be
recovered after restarting the application.

## Run

```powershell
.\mvnw.cmd -q exec:java "-Dexec.mainClass=com.deliverydrone.app.ConsoleApp"
```

Maven is downloaded automatically on the first run. The first launch creates
`delivery-drone.db`; menu item `5` resets it to clean seed data.

For the live demo, use `Windy` to fail before step A, payload `8` with `D1` to
fail step B, and `BusyZone` to fail step C after A and B have completed. Menu
item `6` intentionally stops after B: exit and restart the app, then use item
`4` to demonstrate recovery from the persisted `PENDING_C` chain.
Menu item `7` runs only A and B, then lets the user compensate them immediately
or leave the chain pending.

## Test

```powershell
.\mvnw.cmd test
```

The command runs the DSL-style tests and checks at least 80% line coverage.

## Design

- **Planning (A):** creates and confirms a flight plan.
- **Drone preparation (B):** checks payload and battery, then loads a booking.
- **Landing (C):** reserves a landing pad and confirms the delivery.
- **Facade:** `DispatchFacade` runs A -> B -> C; `AdminFacade` inspects data and
  recovers incomplete chains.

Each subsystem has its own entity, business service, repository, and status.
Subsystems do not call one another; only the facades know all three.

### Principle of Least Knowledge

Without the facade, the client must find a zone and drone, calculate and save a
flight plan, load a booking, reserve a pad, and save a confirmation in the
right order. Through the facade it makes one call:

```java
dispatchFacade.dispatch("order-1", "D1", "North", 3);
```

### Why Facade, not Adapter

An Adapter translates an incompatible interface into one a client already
expects. Here the subsystem APIs are already usable. The problem is that one
delivery requires several ordered operations across independent subsystems,
with compensation if a later operation fails. `DispatchFacade` provides this
single high-level workflow without changing an incompatible API, so Facade is
the appropriate pattern.

### Compensation and recovery

The persisted chain status is `PENDING_B`, `PENDING_C`, `COMPLETED`, or
`COMPENSATED`. If B fails, the facade cancels A. If C fails, it releases B and
then cancels A. `AdminFacade.recoverPending()` reads the saved status after a
restart and applies the same compensation.

See [the UML diagram](docs/class-diagram.png).
For the defence walkthrough, see [DEFENSE_GUIDE.md](docs/DEFENSE_GUIDE.md).
