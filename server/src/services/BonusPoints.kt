package services

const val MAX_CALM_ACCELERATION = 2.78   // m/s², Powerfleet: hard optrekken vanaf 10 km/h per s
const val MAX_CALM_DECELERATION = 3.43   // m/s², AAA Foundation: hard remmen vanaf 0,35 g
const val METERS_PER_BONUS_POINT = 1000.0

fun calculateBonusPoints(distanceM: Double, avgAcceleration: Double, avgDeceleration: Double): Int {
    val isCalm = avgAcceleration < MAX_CALM_ACCELERATION && avgDeceleration < MAX_CALM_DECELERATION
    if (!isCalm) return 0
    return (distanceM / METERS_PER_BONUS_POINT).toInt()
}