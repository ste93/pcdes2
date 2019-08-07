package es2

import scala.collection.mutable.ArrayBuffer

abstract class DistanceRequestMessage{
  var x: Double
  var y: Double
}

abstract class DistanceResponseMessage{
  var distance: Double
}

abstract class CollisionsToCheckMessage{
  var planetsToCheck: ArrayBuffer[PlanetName]
}

abstract class PlanetName {
  var name: String
}

abstract class CalculateAccelerationMessage{
  var numA : Int
  var numB : Int
  var positionAX: Double
  var positionAY: Double
  var positionBX: Double
  var positionBY: Double
  var massA: Double
  var massB: Double
}

abstract class PositionMessage {
  var positionX: Double
  var positionY: Double
  var mass: Double
  var speedX: Double
  var speedY: Double
}

abstract class AccelerationForCalculatePositionMessage {
  var accelerationX: BigDecimal
  var accelerationY: BigDecimal
}

abstract class LockMessage {
  val msg = "lock"
}

abstract class PlanetListMessage{
  val planetListInMessage: ArrayBuffer[PositionMessage]
}