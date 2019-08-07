package es2

import akka.actor.{Actor, Stash}

import scala.collection.mutable.ArrayBuffer

class PostActor extends Actor with Stash{


  def receive = {
    case msg: LockMessage => {
      context.become({
        case msg: LockMessage => {
          unstashAll()
          context.unbecome()
        }

        case msg => {
          stash()
        }

      })
    }

    case msg: PlanetListMessage => {
      var planets = msg.planetListInMessage
      for (i <- 0 until Constants.PLANET_NUMBER) {
        for (j <- i + 1 until Constants.PLANET_NUMBER) {
          var msgToSend = new CalculateAccelerationMessage {
            override var numA: Int = i
            override var positionAX: Double = planets.apply(i).positionX
            override var positionAY: Double = planets.apply(i).positionY
            override var massA: Double = planets.apply(i).mass

            override var numB: Int = j
            override var positionBX: Double = planets.apply(j).positionX
            override var positionBY: Double = planets.apply(j).positionY
            override var massB: Double = planets.apply(j).mass

          }

          this.context.actorSelection("../Acc" + i + "_" + j) ! msgToSend
        }
        this.context.actorSelection("../Pos" + i) ! planets.apply(i)
      }
    }
  }
}

