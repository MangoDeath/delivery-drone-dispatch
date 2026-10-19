# Defence guide

## One-minute project explanation

This is a Java 17 console application that dispatches one drone delivery in
three independent steps:

- **A — Planning:** `PlanningService` validates weather and creates a
  `FlightPlan`.
- **B — Drone preparation:** `DronePreparationService` checks payload and
  battery, then creates a `DroneBooking`.
- **C — Landing:** `LandingService` reserves a `LandingPad` and creates a
  `DeliveryConfirmation`.

`DispatchFacade` is the only class that coordinates A → B → C. Every state
change is written to SQLite (`delivery-drone.db`). A `DispatchChain` records
where the workflow is: `PENDING_B`, `PENDING_C`, `COMPLETED`, or
`COMPENSATED`.

If a later step fails, `CompensationService` reverses only work that actually
completed: B failure cancels A; C failure releases B and then cancels A.
`AdminFacade` can recover pending chains after a restart.

## Before you start

Run this from the project root:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -q exec:java "-Dexec.mainClass=com.deliverydrone.app.ConsoleApp"
```

In the menu, option `5` resets the SQLite file and restores seed data. Use it
before each demonstration so old orders do not affect the result.

## 1. Make B fail after A committed

Say: “I reset the database. `D1` can carry only 5 kg, so 8 kg fails in B. A
has already created and confirmed the flight plan.”

Enter:

```text
5
1
demo-b
D1
North
8
2
```

Point to the stored result in **Show all**:

- `FlightPlan demo-b` is `CANCELLED` — A was compensated.
- There is no booking and no confirmation — B failed before creating either.
- `DispatchChain demo-b` is `COMPENSATED`.

The relevant method is `DispatchFacade.dispatch()`: it saves the chain as
`PENDING_B` after A, catches the B/C runtime failure, and calls
`CompensationService.compensate(orderId)`.

For an optional second failure example, use `BusyZone` and payload `3`. C then
fails because both pads are busy; the booking becomes `RELEASED` and the plan
becomes `CANCELLED`.

## 2. Restart in the middle of a chain

Menu item `6` is a deliberate interruption simulator. It performs A and B,
saves `PENDING_C`, and does not execute C or compensate. This represents the
process stopping between commits.

First application run:

```text
5
6
demo-restart
D1
North
3
3
0
```

You should see `demo-restart: PENDING_C` before exiting. Start the application
again with the same command. Then enter:

```text
3
4
2
```

Explain: “A new `ConsoleMenu`, `AdminFacade`, and repositories were created.
They did not receive the old Java objects. `ChainRepository.findPending()`
reads `PENDING_C` from the SQLite file. Recovery releases B, cancels A, and
stores `COMPENSATED`.”

The policy here is **safe recovery by compensation**, not continuing a
possibly stale delivery. That is intentional: after an unknown interruption,
releasing reserved resources is safer than dispatching a drone without a new
decision.

For a live rollback decision without restarting, use menu item `7`. It calls
`DispatchFacade.dispatchAAndBOnly(...)`, then asks whether to compensate now.
Entering `yes` releases B and cancels A; entering `no` keeps `PENDING_C` for
the restart demonstration.

## 3. Why Facade, not Adapter

Use this answer:

> An Adapter changes one incompatible interface into another interface that a
> client already expects. My planning, drone-preparation, and landing APIs are
> already usable; they have no interface mismatch. The problem is coordinated
> workflow: call A, then B, then C, persist progress, and compensate in reverse
> order if a later step fails. `DispatchFacade` gives the client one high-level
> method, `dispatch(...)`, so it is a Facade.

Point to `ConsoleMenu.dispatch()`: it makes one call to
`dispatchFacade.dispatch(...)`. Without the facade, the menu would have to use
three repositories and services, know the order, and implement rollback.

## 4. Live coding: add and run one genuinely new unit test

This scenario is not in the existing test class: a chain must not become
`COMPLETED` before B has finished. Add this method to
`DispatchFacadeTest.java`:

```java
@Test
void cannotCompleteChainBeforeBIsDone() {
    DispatchChain chain = new DispatchChain("order-7", ChainStatus.PENDING_B);

    assertThrows(IllegalStateTransitionException.class, chain::complete);
}
```

The imports are already in that file. Run only this test:

```powershell
.\mvnw.cmd -q "-Dtest=DispatchFacadeTest#cannotCompleteChainBeforeBIsDone" test
```

Then say: “The entity owns its status rules. `DispatchChain.complete()` only
allows `PENDING_C → COMPLETED`; it rejects `PENDING_B → COMPLETED`.”

If asked why this is a unit test, answer: “It tests one class and no database,
network, or console. It is fast, deterministic, and has one behavior.”

## 5. Principle of Least Knowledge

The potential violation would be in the client if `ConsoleMenu` directly did
all of this: find a zone, create a plan, save it, find a drone, create a
booking, find a pad, create a confirmation, and then manually roll back on an
error. That client would know too many unrelated classes and their ordering.

Our `ConsoleMenu` does not do that. Its normal dispatch path calls only:

```java
dispatchFacade.dispatch(orderId, droneId, zone, payloadKg);
```

The facade intentionally knows the three subsystems because it is the boundary
that coordinates them. The client only knows the facade; repositories and
entities remain behind it. This is how the facade applies the Principle of
Least Knowledge.

## Quick map of files

| Question | Read these classes |
| --- | --- |
| Main workflow and rollback | `facade/DispatchFacade`, `facade/CompensationService` |
| Persisted workflow status | `facade/DispatchChain`, `facade/ChainRepository` |
| Restart/recovery | `facade/AdminFacade`, `shared/db/Database` |
| A / B / C rules | `planning/PlanningService`, `droneprep/DronePreparationService`, `landing/LandingService` |
| Tests | `src/test/.../facade/DispatchFacadeTest` and `DispatchTestDsl` |
