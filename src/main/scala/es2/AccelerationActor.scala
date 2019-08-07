package es2

import akka.actor.{Actor, Stash}


class AccelerationActor extends Actor with Stash {
  // Extending actor trait
  def receive = {                 //  Receiving message
    case msg: LockMessage =>{
      context.become({
        case msg: LockMessage => {
          unstashAll()
          context.unbecome()
        }
        case _ =>{
          stash()
        }
      })
    }

    case msg:CalculateAccelerationMessage => {
      var deltaX: Int = msg.positionAX - msg.positionBX
      var deltaY: Int  = msg.positionAY - msg.positionBY
      var lim: Int  = 2
      //TODO
      println("deltax "  + deltaX)
      //TODO
      println("deltay "  + deltaY)

      if (deltaX >= 0 && deltaX < lim) deltaX = lim
      if (deltaY >= 0 && deltaY < lim) deltaY = lim
      if (deltaX < 0 && deltaX > -lim) deltaX = -lim
      if (deltaY < 0 && deltaY > -lim) deltaY = -lim
      var squaredDeltax: Int  = deltaX * deltaX
      var squaredDeltay: Int  = deltaY * deltaY
      var forceOverMassSquared: BigDecimal = Constants.GRAVITY / (squaredDeltay + squaredDeltax)
      var accelerationA: BigDecimal = forceOverMassSquared * msg.massB
      var accelerationB: BigDecimal = forceOverMassSquared * msg.massA
      var messageToSendPlanetA: AccelerationForCalculatePositionMessage = new AccelerationForCalculatePositionMessage {
        override var accelerationX: BigDecimal = _
        override var accelerationY: BigDecimal = _
      }
      var messageToSendPlanetB: AccelerationForCalculatePositionMessage = new AccelerationForCalculatePositionMessage {
        override var accelerationX: BigDecimal = _
        override var accelerationY: BigDecimal = _
      }
      if (deltaY != 0) {
        var var1: Double = Math.sqrt(1 / (1 + squaredDeltax / squaredDeltay))
        var var2: Double = Math.abs(deltaX / deltaY)
        messageToSendPlanetA.accelerationX = accelerationA * var1 * var2 * (if (deltaX > 0) -1 else 1)
        messageToSendPlanetA.accelerationY = accelerationA * var1 * (if (deltaY > 0) -1 else 1)

        messageToSendPlanetB.accelerationX = accelerationB * var1 * var2 * (if (deltaX < 0) -1 else 1)
        messageToSendPlanetB.accelerationY = accelerationB * var1 * (if (deltaY < 0) -1 else 1)
      }
      else {
        messageToSendPlanetA.accelerationX = accelerationA * (if (deltaX > 0) -1 else 1)
        messageToSendPlanetB.accelerationX = accelerationB * (if (deltaX < 0) -1 else 1)
      }
      this.context.actorSelection("../Pos"  + msg.numA) ! messageToSendPlanetA
      this.context.actorSelection("../Pos"  + msg.numB) ! messageToSendPlanetB
    }
    //case _ =>println("Unknown message")      // Default case
  }

}

