# Core Domain

## Core Domain Definition

The core domain of this system is service booking and scheduling.

The primary business value of the platform lies in its ability to:

-manage availability of specialists

-allow clients to book time slots

-enforce booking rules consistently

-handle concurrent booking attempts safely



## Core Responsibilities

The core domain is responsible for:

-creating bookings based on service duration and availability

-preventing overlapping bookings for the same specialist

-handling booking lifecycle transitions

-enforcing cancellation and rescheduling rules

-maintaining consistency under concurrent access


## Supporting Domains

The following domains support the core domain but are not its focus:

### Organization Domain

Responsible for:

-managing organizations (clinics, salons, hospitals)

-managing specialists

-managing services offered by an organization

-assigning roles within an organization

This domain provides the context in which bookings exist.


### Scheduling Domain

Responsible for:

-defining working schedules for specialists

-managing working days, hours, and breaks

-handling exceptions such as vacations or special days

-calculating availability for a given time range

This domain supplies availability information to the core booking logic.


### Notification Domain

Responsible for:

-reacting to booking-related events

-sending notifications (email/logs)

-handling reminders and audit logging

This domain does not influence booking decisions and operates asynchronously.

