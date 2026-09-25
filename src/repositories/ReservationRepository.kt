package repositories

import models.Reservation
import models.ReservationStatus

interface ReservationRepository {
    suspend fun getAllReservations(): List<Reservation>

    suspend fun getReservationById(reservationId: Int): Reservation?

    suspend fun getReservationsForRenter(renterId: Int): List<Reservation>

    /**
     * Maakt een nieuwe reservering aan. Het `id`-, `status`- en `createdAt`-veld
     * van [reservation] worden genegeerd (net als bij Car: de repository bepaalt
     * die zelf). Geeft null terug als de auto in de gevraagde periode al een
     * (PENDING of CONFIRMED) reservering heeft die overlapt.
     */
    suspend fun createReservation(reservation: Reservation): Reservation?

    /** Geeft de bijgewerkte reservering terug, of null als het id niet bestaat. */
    suspend fun updateReservationStatus(reservationId: Int, status: ReservationStatus): Reservation?

    /** Geeft true terug als er iets verwijderd is. */
    suspend fun deleteReservation(reservationId: Int): Boolean
}
