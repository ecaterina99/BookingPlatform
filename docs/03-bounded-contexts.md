# Bounded Contexts

## Booking Context (Core)
Responsibility - The Booking context is the core bounded context of the system. It is responsible for managing the lifecycle of bookings and enforcing all critical business rules.

### Key Responsibilities:

-create and manage bookings

-enforce non-overlapping bookings

-handle booking state transitions

-manage slot holding and expiration

-publish booking-related domain events

### Owned Data

-Booking

-BookingStatus

-TimeRange

-SlotHold

### Published Events

-BookingCreated

-BookingCancelled

-BookingRescheduled

-SlotHeld


## Scheduling Context

The Scheduling context manages specialist availability and working schedules. It provides availability information but does not make booking decisions.

### Key Responsibilities

-define working hours and working days

-manage breaks and vacations

-calculate available time slots

-notify other contexts when availability changes

### Owned Data

-Schedule

-WorkingHours

-AvailabilityOverride

### Published Events

-ScheduleChanged

-Consumed Events

-BookingCreated

-BookingCancelled



## Organization Context

The Organization context manages organizations and their internal structure. It defines who can offer services and who can be booked.

### Key Responsibilities

-register and manage organizations

-manage specialists

-manage services and service durations

-assign organizational roles

### Owned Data

-Organization

-Specialist

-Service

-OrganizationRole

### Published Events

-SpecialistAdded

-ServiceCreated

-Consumed Events

-OrganizationRegistered


## Notification Context

The Notification context reacts to domain events and performs side effects. It does not influence domain decisions.

### Key Responsibilities

-send notifications

-create audit logs

-schedule reminders

### Owned Data

-Notification

-NotificationStatus

-AuditLog

### Published Events

-NotificationSent

-Consumed Events

-BookingCreated

-BookingCancelled

-BookingRescheduled


