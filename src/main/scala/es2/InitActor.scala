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
          override var positionX: Int = (rand.nextDouble() * Constants.DRAWING_PANEL_SIZE_X).toInt
          override var positionY: Int = (rand.nextDouble() * Constants.DRAWING_PANEL_SIZE_Y).toInt
          override var mass: Double = rand.nextDouble() * Constants.MAX_MASS
          override var speedX: Double = rand.nextDouble() - 0.5
          override var speedY: Double = rand.nextDouble() - 0.5
        })
      }
      context.actorSelection("../Post") ! new PlanetListMessage {
        override val planetListInMessage: ArrayBuffer[PositionMessage] = planets.clone()
      }

    }
    //case _ =>println("Unknown message")      // Default case
  }
}
