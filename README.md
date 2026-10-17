# Delivery Drone Dispatch

Small Java 17 university project that demonstrates the **Facade** design pattern.
It uses Maven, SQLite JDBC, JUnit 5, and JaCoCo. There is no Spring, ORM, Lombok,
or framework code.

## Run

Run the console application with one command. The included Maven Wrapper downloads
Maven automatically on its first use, so a global Maven installation is not required:

```powershell
.\mvnw.cmd -q exec:java "-Dexec.mainClass=com.deliverydrone.app.ConsoleApp"
```

The first run creates `delivery-drone.db` and saves seed data. Choose menu item
`5` to delete and recreate the database with the same seed data.

Useful inputs for failures:

| Failure | Input |
| --- | --- |
| `WeatherUnsafeException` | zone `Windy` |
| `PayloadTooHeavyException` | drone `D1`, payload `8` |
| `LowBatteryException` | drone `D3`, zone `East` |
| `NoFreeLandingPadException` | zone `BusyZone` |

Run tests and create the JaCoCo report:

```powershell
.\mvnw.cmd test
```

The report is at `target/site/jacoco/index.html`.

## Structure

- `planning` — route distance/time, wind check, zones, and `FlightPlan`.
- `droneprep` — payload/battery checks, drones, and `DroneBooking`.
- `landing` — landing pads and `DeliveryConfirmation`.
- `facade` — `DispatchFacade`, `AdminFacade`, persistent chain status, and compensation.
- `app` — database seed data and the Scanner menu.

Each subsystem only imports `shared` code. It does not import another subsystem.
The facade is the only place that knows all three subsystems.

## Principle of Least Knowledge

Without a facade, a client must know the order of calls, status changes, and many
low-level classes. This is too much knowledge for a console client.

```java
// Before: client knows every subsystem and its correct order.
Zone zone = flightPlanRepository.findZone("North");
weatherChecker.check(zone);
double distance = routePlanner.calculateDistanceKm(zone);
FlightPlan plan = new FlightPlan("order-1", "North", distance, 7.5, HELD);
flightPlanRepository.saveFlightPlan(plan);
plan.confirm();
payloadSensor.check(3, drone);
batteryManager.check(distance, drone);
landingPadController.reservePad("North");
// The client still has to save a DroneBooking and DeliveryConfirmation.
```

```java
// After: the client knows one clear operation only.
DispatchFacade dispatchFacade = new DispatchFacade(database);
dispatchFacade.dispatch("order-1", "D1", "North", 3);
```

`DispatchFacade` hides `RoutePlanner`, `WeatherChecker`, `BatteryManager`,
`PayloadSensor`, `LandingPadController`, the three repositories, and the status
changes. The client supplies only delivery facts. This follows the Principle of
Least Knowledge: an object should talk only to the small number of objects it
actually needs to know.

## Why Facade, not Adapter?

Facade and Adapter can both make client code simpler, but they solve different
problems. An **Adapter** changes one existing interface into another interface
that a client already expects. For example, if an old `LegacyDroneApi` had a
method called `bookMachine(int grams)` but the application expected
`reserveDrone(double kg)`, an adapter could translate the name, units, and
arguments. The main intent would be compatibility between two mismatched
interfaces. It would normally wrap one class or one external API.

This project has no incompatible interface to translate. `RoutePlanner`,
`WeatherChecker`, `PayloadSensor`, `BatteryManager`, and
`LandingPadController` already have understandable methods. The actual problem
is that dispatching one delivery needs several calls in a strict order across
three independent subsystems. It must also save a chain status and undo earlier
work if a later subsystem fails. That is orchestration, not translation.

`DispatchFacade.dispatch(...)` therefore gives the client one high-level
operation: dispatch a delivery. Inside, it calls A, then B, then C. It writes
`PENDING_B`, `PENDING_C`, `COMPLETED`, or `COMPENSATED` to SQLite. The separate
`AdminFacade` exposes high-level administration operations: print stored data,
compensate a chain, and recover unfinished chains after restart. These are both
facades because they simplify access to a group of classes without changing the
meaning of an incompatible API.

## Compensation and recovery

`dispatch_chains` belongs to the facade and stores a chain ID (the order ID) and
its status. `CompensationService` reads this table before deciding what to undo:

- `PENDING_B`: cancel the confirmed `FlightPlan`.
- `PENDING_C`: release the loaded `DroneBooking`, then cancel the `FlightPlan`.
- `COMPLETED` and `COMPENSATED`: do nothing.

`AdminFacade.recoverPending()` reads unfinished rows after an application restart
and uses exactly the same compensation rules.

See [the PlantUML class diagram](docs/class-diagram.puml) and
[the defense cheat sheet](DEFENSE_CHEAT_SHEET.md).
