package models

import contracts.models.ReservationStatus

val ACTIVE_RESERVATION_STATUSES = setOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)