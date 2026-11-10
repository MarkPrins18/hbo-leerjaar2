package models

import shared.models.ReservationStatus

val ACTIVE_RESERVATION_STATUSES = setOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)