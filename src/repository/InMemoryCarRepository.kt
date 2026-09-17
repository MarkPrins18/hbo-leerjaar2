package repository

import models.BEVCar
import models.Car
import models.FCEVCar
import models.ICECar

//Let op!! gehele class is tijdelijk gemaakt met ID. verwijder zodra db beschikbaar is.

class InMemoryCarRepository : CarRepository {

    private val cars = mutableMapOf<Int, Car>()
    private var nextId = 1

    override suspend fun getAllCars(): List<Car> = cars.values.toList()

    override suspend fun getCarById(carId: Int): Car? = cars[carId]

    override suspend fun createCar(car: Car): Car {
        val newId = nextId++
        val carWithId = withId(car, newId)
        cars[newId] = carWithId
        return carWithId
    }

    override suspend fun updateCar(car: Car): Car? {
        if (!cars.containsKey(car.id)) return null
        cars[car.id] = car
        return car
    }

    override suspend fun deleteCar(carId: Int): Boolean {
        return cars.remove(carId) != null
    }

    private fun withId(car: Car, id: Int): Car = when (car) {
        is ICECar -> car.copy(id = id)
        is BEVCar -> car.copy(id = id)
        is FCEVCar -> car.copy(id = id)
    }
}