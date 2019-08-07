package es2

import akka.actor.{Actor, Stash}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class InitActor extends Actor{
  // Extending actor trait
  def receive: PartialFunction[Any, Unit] = {                 //  Receiving message

    case msg:String => {
      var rand = new Random()
      var planets: ArrayBuffer[PositionMessage] = new ArrayBuffer[PositionMessage]()
      for (i <- 1 to Constants.PLANET_NUMBER) {
        var pos = new PositionMessage {
          override var positionX: Double = rand.nextDouble() * 2 * 10E9
          override var positionY: Double = rand.nextDouble() * 2 * 10E9
          override var mass: Double = rand.nextDouble() * Constants.MAX_MASS + 1.2 * 10E20
          override var speedX: Double = rand.nextDouble() * 50 + 50
          override var speedY: Double = rand.nextDouble() * 50 + 50
        }
        planets.append(pos)
      }
      context.actorSelection("../Post") ! new PlanetListMessage {
        override val planetListInMessage: ArrayBuffer[PositionMessage] = planets.clone()
      }

    }
    //case _ =>println("Unknown message")      // Default case
  }
}
