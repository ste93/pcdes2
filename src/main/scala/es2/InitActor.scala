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
        planets.append(new PositionMessage {
          override var positionX: Int = (rand.nextDouble() * 2 * 10E9).toInt
          override var positionY: Int = (rand.nextDouble() * 2 * 10E9).toInt
          override var mass: Double = rand.nextDouble() * Constants.MAX_MASS + 1.2 * 10E20
          override var speedX: Double = rand.nextDouble() * 1 * 10E6 + 2* 10E6
          override var speedY: Double = rand.nextDouble() * 1 * 10E6 + 2* 10E6
        })
      }
      context.actorSelection("../Post") ! new PlanetListMessage {
        override val planetListInMessage: ArrayBuffer[PositionMessage] = planets.clone()
      }

    }
    //case _ =>println("Unknown message")      // Default case
  }
}
