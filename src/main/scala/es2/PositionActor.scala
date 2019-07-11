package es2

import akka.actor.{Actor, Stash}


class PositionActor extends Actor with Stash{
  var collisionsChecked: Int = 0
  var totalCollisionsToCheck: Int = _
  var totalDistanceRequestMessageReceived: Int = 0
  var totalDistanceRequestMessageToReceive: Int = _
  var positionMessage : PositionMessage = _
  var accelerationForCalculatePositionMessagesReceived: Int = 0
  var totalAccelerationX: Double = 0
  var totalAccelerationY: Double = 0
  var initialPositionReceived: Boolean = false
  var updatePosition:Boolean = true
  var positionUpdated: Boolean = false
  var newPositionX: Int = _
  var newPositionY: Int = _

  private def resetVariables(): Unit = {
    updatePosition = true
    positionUpdated = false
    initialPositionReceived = false
    accelerationForCalculatePositionMessagesReceived = 0
    totalAccelerationX = 0
    totalAccelerationY = 0
    collisionsChecked = 0
    //positionMessage = _
    //totalCollisionsToCheck = _
    //totalDistanceRequestMessageToReceive = _
    totalDistanceRequestMessageReceived = 0
  }

  private def calculateNewPosition(initialPosition: Double, acceleration: Double, initialSpeed: Double): Int =
    (initialPosition + 0.5 * acceleration * Math.pow(Constants.DELTA_TIME, 2) + initialSpeed * Constants.DELTA_TIME).toInt

  private def calculateNewSpeed(acceleration: Double, initialSpeed: Double): Double =
    acceleration * Constants.DELTA_TIME + initialSpeed

  private def requestDistance(xParam: Int, yParam: Int, planetNumber: Int): Unit ={
    this.context.actorSelection("../Pos"  + planetNumber) ! new DistanceRequestMessage {
      override var x: Int = xParam
      override var y: Int = yParam
    }
  }

  private def calculateDistance(x1: Int , x2: Int, y1: Int, y2: Int): Double = {
    math.sqrt(math.pow(x1-x2, 2) + math.pow(y1-y2, 2))
  }

  private def calculateAndSend(): Unit = {
    newPositionX = calculateNewPosition(positionMessage.positionX, totalAccelerationX, positionMessage.speedX)
    newPositionY = calculateNewPosition(positionMessage.positionY, totalAccelerationY, positionMessage.speedY)
  }

  private def sendToGraphic(): Unit = {
    positionUpdated = true
    unstashAll()
    println("unstashing" + self.path.name)
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
      //TODO remove
      println("DistanceResponseMessage received" + self.path.name)

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
      //TODO remove
      println("CollisionsToCheckMessage received"+ self.path.name + " " + msg.planetsToCheck.length)
      totalCollisionsToCheck = msg.planetsToCheck.length
      totalDistanceRequestMessageToReceive = Constants.PLANET_NUMBER - totalCollisionsToCheck - 1
      if (totalCollisionsToCheck == 0) {
        sendToGraphic()
      }
      else {
        //TODO remove
        println(msg)
        println(msg.planetsToCheck)
        msg.planetsToCheck.foreach(element => {
          println(element.name)
          this.context.actorSelection("../" + element.name) ! new DistanceRequestMessage {
            override var x: Int = newPositionX
            override var y: Int = newPositionY
          }
        })
      }
    }

    case msg: DistanceRequestMessage => {
      //TODO remove
      println("DistanceRequestMessage received"+ self.path.name)

      if (!positionUpdated) {
        println("stashing")
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
      //TODO remove
      println("AccelerationForCalculatePositionMessage received"+ self.path.name)

      accelerationForCalculatePositionMessagesReceived  += 1
      totalAccelerationX += msg.accelerationX
      totalAccelerationY += msg.accelerationY
      if (accelerationForCalculatePositionMessagesReceived == Constants.PLANET_NUMBER - 1 && initialPositionReceived){
        this.context.actorSelection("../Syn") ! new PlanetName {
          println("to syn")
          override var name: String = self.path.name
        }
      }
    }

    case msg: PositionMessage => {
      initialPositionReceived = true
      positionMessage = msg
      if (accelerationForCalculatePositionMessagesReceived == Constants.PLANET_NUMBER - 1) {
        this.context.actorSelection("../Syn") ! new PlanetName {
          override var name: String = self.path.name
        }
      }
    }
  }
}