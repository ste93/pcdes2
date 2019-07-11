package es2

import akka.actor.{ActorSystem, Props}
import es2.{SynchronizationActor, AccelerationActor}



object Main{
  def main(args:Array[String]) {

    var actorSystem = ActorSystem("ActorSystem"); // Creating ActorSystem
    var actorInit = actorSystem.actorOf(Props[InitActor], "Init") //Creating actor
    var actorSyn = actorSystem.actorOf(Props[SynchronizationActor], "Syn") //Creating actor
    //var actorGraphic = actorSystem.actorOf(Props[GraphicActor], "Graphic") //Creating actor

    for (i <- 0 until Constants.PLANET_NUMBER){
      for (j <- i + 1 until Constants.PLANET_NUMBER) {
        actorSystem.actorOf(Props[AccelerationActor], "Acc" + i + "_" + j)
        println (i + " " + j)
      }
      actorSystem.actorOf(Props[PositionActor], "Pos" + i )
      //actorSystem.actorOf(Props[AccelerationForCalculatePositionMessage], "Pos" + i )
    }
    actorInit ! "Init"                                                // Sending messages by using !
  }
}