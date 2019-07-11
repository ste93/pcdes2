package es2

import akka.actor.{Actor, Stash}

import scala.collection.mutable.ArrayBuffer

class SynchronizationActor extends Actor  with Stash{
  var planetsUpdated: ArrayBuffer[PlanetName] = new ArrayBuffer[PlanetName]()
  def receive = { //  Receiving message
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

    case msg: PlanetName => {
      //TODO delete
      println( "planetName received" + msg.name + self.path.name)
      println(planetsUpdated.length)
      sender() ! new CollisionsToCheckMessage {
        override var planetsToCheck: ArrayBuffer[PlanetName] = planetsUpdated.clone()
      }
      planetsUpdated.append(msg)
      if (planetsUpdated.length == Constants.PLANET_NUMBER) {
        planetsUpdated.clear()
      }
    }
  }
}