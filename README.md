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

## Assignment 5: Strategy Pattern

This assignment is independent from the SQLite delivery chain above. It uses
in-memory data in `com.deliverydrone.strategy`.

Run the demo:

```powershell
.\mvnw.cmd -q exec:java "-Dexec.mainClass=com.deliverydrone.strategy.StrategyDemo"
```

### Classes and responsibilities

| Class | Responsibility |
| --- | --- |
| `DeliveryRequest` | Stores delivery input values. |
| `DroneOption` | Stores a candidate drone profile. |
| `DroneSelectionStrategy` | Defines drone selection. |
| `NearestDroneStrategy` | Chooses the closest suitable drone. |
| `HighestBatteryDroneStrategy` | Chooses the most charged suitable drone. |
| `LeastBusyDroneStrategy` | Chooses the least loaded suitable drone. |
| `DroneSelectionContext` | Delegates selection to the active strategy. |
| `DroneSelectionSelector` | Chooses a selection strategy from request properties. |
| `FlightTimeStrategy` | Defines flight-time estimation. |
| `StandardFlightTimeStrategy` | Estimates time at normal speed. |
| `ExpressFlightTimeStrategy` | Estimates time with launch preparation. |
| `WeatherFlightTimeStrategy` | Estimates time from wind data. |
| `FlightTimeContext` | Delegates estimation to the active strategy. |
| `FlightTimeSelector` | Chooses a time strategy from current conditions. |
| `FlightTimeStrategies` | Creates flight-time strategy instances. |
| `WindSource` | Defines wind data access. |
| `SimulatedWindSource` | Supplies fixed wind values. |
| `StrategyDemo` | Prints Strategy behavior for the console. |

### Why Strategy, not State or Template Method

Strategy fits this task because drone selection and flight-time estimation are
alternative algorithms for the same operation. The caller can choose an
algorithm manually through a context, replace it with `setStrategy()`, or ask a
selector to choose from current request conditions. For example, an urgent
request uses `NearestDroneStrategy`, while a heavy request uses
`HighestBatteryDroneStrategy`. The context delegates work to the selected
object. It has no knowledge of concrete strategy types.

This is not State. State represents an object lifecycle where behavior changes
because the object moves through internal states. `FlightPlan` status in
Assignment 4 is an example of lifecycle state: a plan becomes held, confirmed,
or cancelled. In Assignment 5, a delivery request does not transition through
algorithm states. The active strategy is a deliberate runtime choice and can
be replaced at any time.

This is not Template Method. Template Method uses inheritance with one fixed
algorithm skeleton in a parent class. Subclasses replace selected steps. Here
the algorithms have no useful shared step sequence: nearest selection compares
pickup distance, battery selection compares charge, and least-busy selection
compares workload. Composition through a strategy interface keeps them
independent. `WeatherFlightTimeStrategy` also receives `WindSource` through
its constructor, allowing a simulated source in the demo and a fixed fake in
tests without changing the strategy.

### Refactoring history

- [Before: one class with a switch](https://github.com/MangoDeath/delivery-drone-dispatch/commit/236e6f5bd18960e22ca7f320d5311ea677a6a3f4)
- [After: separate Strategy classes](https://github.com/MangoDeath/delivery-drone-dispatch/commit/9b9adaa2f12cb50ca2b874a0a39d57d25ba4734aa)
