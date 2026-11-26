package nl.carsharing.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed

@Serializable
sealed interface Route : NavKey {
    // Tab roots
    @Serializable data object Search : Route
    @Serializable data object Rides : Route
    @Serializable data object Account : Route

    // Account
    @Serializable data object MyCars : Route
}

@OptIn(ExperimentalSerializationApi::class)
val carSharingSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Route>()
        }
    }
}
