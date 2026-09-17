package repository

import models.Car

/**
 * let op! onderstaande is ai tekst.
 * Abstractie over waar Car-data vandaan komt. De huidige implementatie
 * (InMemoryCarRepository) is een tijdelijke stand-in tot de Exposed/MySQL-
 * implementatie klaar is. Routes en services praten alleen tegen deze
 * interface, nooit tegen een concrete implementatie.
 */
interface CarRepository {
    suspend fun getAllCars(): List<Car>

    suspend fun getCarById(carId: Int): Car?

    /**
     * Slaat een nieuwe auto op. Het `id`-veld van [car] wordt genegeerd;
     * de repository genereert zelf een nieuw id en geeft de opgeslagen
     * auto (mét gegenereerd id) terug.
     */
    suspend fun createCar(car: Car): Car

    /**
     * Geeft de bijgewerkte auto terug, of null als er geen auto bestaat
     * met het id van [car].
     */
    suspend fun updateCar(car: Car): Car?

    /** Geeft true terug als er iets verwijderd is. */
    suspend fun deleteCar(carId: Int): Boolean
}