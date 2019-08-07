package es2

import scala.collection.mutable.ArrayBuffer

abstract class DistanceRequestMessage{
  var x: Int
  var y: Int
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
  var positionAX: Int
  var positionAY: Int
  var positionBX: Int
  var positionBY: Int
  var massA: Double
  var massB: Double
}

abstract class PositionMessage {
  var positionX: Int
  var positionY: Int
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