package es2

import akka.actor.{Actor, Stash}


class PositionActor extends Actor with Stash{
  var collisionsChecked: Int = 0
  var totalCollisionsToCheck: Int = _
  var totalDistanceRequestMessageReceived: Int = 0
  var totalDistanceRequestMessageToReceive: Int = _
  var positionMessage : PositionMessage = _
  var accelerationForCalculatePositionMessagesReceived: Int = 0
  var totalAccelerationX: BigDecimal = 0
  var totalAccelerationY: BigDecimal = 0
  var initialPositionReceived: Boolean = false
  var updatePosition:Boolean = true
  var positionUpdated: Boolean = false
  var newPositionX: Double = _
  var newPositionY: Double = _

  private def resetVariables(): Unit = {
    updatePosition = true
    positionUpdated = false
    initialPositionReceived = false
    accelerationForCalculatePositionMessagesReceived = 0
    totalAccelerationX = 0
    totalAccelerationY = 0
    collisionsChecked = 0
    totalDistanceRequestMessageReceived = 0
  }

  private def calculateNewPosition(initialPosition: Double, acceleration: BigDecimal, initialSpeed: Double): Double = {
    initialPosition + 0.5 * acceleration.toDouble * Math.pow(Constants.DELTA_TIME, 2) + initialSpeed * Constants.DELTA_TIME
  }

  private def calculateNewSpeed(acceleration: BigDecimal, initialSpeed: Double): Double =
    acceleration.toDouble * Constants.DELTA_TIME + initialSpeed

  /*
  private def requestDistance(xParam: Double, yParam: Double, planetNumber: Int): Unit ={
    this.context.actorSelection("../Pos"  + planetNumber) ! new DistanceRequestMessage {
      override var x: Double = xParam
      override var y: Double = yParam
    }
  }
   */

  private def calculateDistance(x1: Double , x2: Double, y1: Double, y2: Double): Double = {
    math.sqrt(math.pow(x1-x2, 2) + math.pow(y1-y2, 2))
  }

  private def calculatePositions(): Unit = {
    newPositionX = calculateNewPosition(positionMessage.positionX, totalAccelerationX, positionMessage.speedX)
    newPositionY = calculateNewPosition(positionMessage.positionY, totalAccelerationY, positionMessage.speedY)
  }

  private def sendSynchronizationPlanetRequest(): Unit = {
    calculatePositions()
    this.context.actorSelection("../Syn") ! new PlanetName {
      override var name: String = self.path.name
    }
  }

  private def sendToGraphic(): Unit = {
    positionUpdated = true
    unstashAll()
    if(updatePosition) {
      positionMessage.positionX = newPositionX
      positionMessage.positionY = newPositionY
      positionMessage.speedX = calculateNewSpeed(totalAccelerationX, positionMessage.speedX)
      positionMessage.speedY = calculateNewSpeed(totalAccelerationY, positionMessage.speedY)
    }

    this.context.actorSelection("../Graphic") ! positionMessage
  }

  def receive = {                 //  Receiving message
    case msg: LockMessage =>
      context.become({
        case msg: LockMessage => {
          unstashAll()
          context.unbecome()
        }
        case _ => stash()
      })


    case msg: DistanceResponseMessage => {
      collisionsChecked +=1
      if(msg.distance < Constants.MIN_DISTANCE_BETWEEN_PLANETS) {
        updatePosition = false
      }
      if (collisionsChecked == totalCollisionsToCheck) {
        sendToGraphic()
        if(totalDistanceRequestMessageToReceive == 0) {
          resetVariables()
        }
      }
    }

    case msg:CollisionsToCheckMessage => {
      totalCollisionsToCheck = msg.planetsToCheck.length
      totalDistanceRequestMessageToReceive = Constants.PLANET_NUMBER - totalCollisionsToCheck - 1
      if (totalCollisionsToCheck == 0) {
        sendToGraphic()
      }
      else {
        msg.planetsToCheck.foreach(element => {
          this.context.actorSelection("../" + element.name) ! new DistanceRequestMessage {
            override var x: Double = newPositionX
            override var y: Double = newPositionY
          }
        })
      }
    }

    case msg: DistanceRequestMessage => {
      if (!positionUpdated) {
        stash()
      }
      else {
        totalDistanceRequestMessageReceived += 1
        sender() ! new DistanceResponseMessage {
          override var distance: Double = calculateDistance(msg.x, positionMessage.positionX, msg.y, positionMessage.positionY)
        }
        if (totalDistanceRequestMessageReceived == totalDistanceRequestMessageToReceive) {
          resetVariables()
        }
      }
    }

    case msg:AccelerationForCalculatePositionMessage => {
      accelerationForCalculatePositionMessagesReceived  += 1
      totalAccelerationX = totalAccelerationX + msg.accelerationX
      totalAccelerationY = totalAccelerationY +  msg.accelerationY
      if (accelerationForCalculatePositionMessagesReceived == Constants.PLANET_NUMBER - 1 && initialPositionReceived){
        sendSynchronizationPlanetRequest()
      }
    }

    case msg: PositionMessage => {
      initialPositionReceived = true
      positionMessage = msg

      if (accelerationForCalculatePositionMessagesReceived == Constants.PLANET_NUMBER - 1) {
        sendSynchronizationPlanetRequest()
      }
    }
  }
}