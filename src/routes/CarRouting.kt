package routes

import models.Car
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

//Broken!
fun Route.carRoutes() {
    val cars = mutableListOf(
        Car(id = 1, brand = "Volkswagen", model = "ID.3", year = 2023, trim = "Pro Performance"),
        Car(id = 2, brand = "Toyota", model = "Mirai", year = 2022, trim = "Launch Edition"),
        Car(id = 3, brand = "Tesla", model = "Model 3", year = 2024, trim = "Long Range"),
        Car(id = 4, brand = "BMW", model = "i4", year = 2023, trim = "eDrive40"),
        Car(id = 5, brand = "Audi", model = "e-tron GT", year = 2023, trim = "RS"),
        Car(id = 6, brand = "Volvo", model = "EX30", year = 2024, trim = "Single Motor Extended Range"),
        Car(id = 7, brand = "Hyundai", model = "IONIQ 5", year = 2023, trim = "Lounge"),
        Car(id = 8, brand = "Porsche", model = "Taycan", year = 2024, trim = "4S"),
        Car(id = 9, brand = "Mercedes-Benz", model = "EQE", year = 2023, trim = "350+"),
        Car(id = 10, brand = "Kia", model = "EV6", year = 2024, trim = "GT-Line"),
        Car(id = 11, brand = "Polestar", model = "2", year = 2023, trim = "Long Range Dual Motor"),
        Car(id = 12, brand = "Peugeot", model = "e-208", year = 2022)
    )
    var nextID = 13

    get("/cars") {
        call.respond(cars)
    }

    post("/cars") {
        val newCar = call.receive<Car>()
        val carWithId = newCar.copy(id = nextID)
        nextID++
        cars.add(carWithId)
        call.respond(HttpStatusCode.Created, carWithId)
    }
}