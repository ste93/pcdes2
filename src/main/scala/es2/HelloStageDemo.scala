package es2

import akka.actor.{Actor, ActorSystem, Props, Stash}
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Button
import scalafx.scene.layout.BorderPane
import scalafx.scene.paint.Color._

import scala.collection.mutable.ArrayBuffer

 object HelloStageDemo extends JFXApp {


   class GraphicActor extends Actor with Stash{
     var count: Double = 0
     var minX: Double = 0
     var minY: Double = 0
     var maxX: Double = 0
     var maxY: Double = 0
     var first = true
     var planetList: ArrayBuffer[PositionMessage] = new ArrayBuffer[PositionMessage]()
     def receive: PartialFunction[Any, Unit] = { //  Receiving message
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

       case msg:PositionMessage => {
         count+= 1
         if (first){
           maxX = msg.positionX
           minX = msg.positionX
           minY = msg.positionY
           maxY = msg.positionY
           first=false
         }
         else{
           if (msg.positionX > maxX) maxX = msg.positionX
           else if (msg.positionX < minX) minX = msg.positionX
           if (msg.positionY > maxY) maxY = msg.positionY
           else if (msg.positionY < minY) minY = msg.positionY
         }
         planetList.append(msg)
         if (count == Constants.PLANET_NUMBER) {

           minX -= 10E5
           minY -= 10E5
           maxX += 10E5
           maxY += 10E5
           val rescaleX: Double =Constants.DRAWING_PANEL_SIZE_X / (maxX - minX)
           val rescaleY: Double = Constants.DRAWING_PANEL_SIZE_Y / (maxY - minY)

           Platform.runLater {
                var planetLocal: ArrayBuffer[PositionMessage] = planetList.clone()
                canvas.graphicsContext2D.clearRect(0, 0, Constants.DRAWING_PANEL_SIZE_X, Constants.DRAWING_PANEL_SIZE_Y)
                planetLocal.foreach( element => {
                 canvas.graphicsContext2D.fillOval((element.positionX - minX) * rescaleX + 15,
                   (element.positionY - minY) * rescaleY + 15,
                   10, 10)
                  print((element.positionX - minX) * rescaleX + 15)
                  println(" " + (element.positionY - minY) * rescaleY + 15)
             })
           }
           //Thread.sleep(100) // wait for 1000 millisecond

           context.actorSelection("../Post") ! new PlanetListMessage {
             override val planetListInMessage: ArrayBuffer[PositionMessage] = planetList.clone()
           }
           planetList.clear()
           count = 0
           first = true
         }
       }
     }
   }

   var actorSystem = ActorSystem("ActorSystem"); // Creating ActorSystem
   var actorInit = actorSystem.actorOf(Props[InitActor], "Init") //Creating actor
   actorSystem.actorOf(Props[SynchronizationActor], "Syn") //Creating actor
   actorSystem.actorOf(Props[GraphicActor], "Graphic") //Creating actor
   var actorPost = actorSystem.actorOf(Props[PostActor], "Post") //Creating actor


   for (i <- 0 until Constants.PLANET_NUMBER){
     for (j <- i + 1 until Constants.PLANET_NUMBER) {
       actorSystem.actorOf(Props[AccelerationActor], "Acc" + i + "_" + j)
     }
     actorSystem.actorOf(Props[PositionActor], "Pos" + i )
     //actorSystem.actorOf(Props[AccelerationForCalculatePositionMessage], "Pos" + i )
   }
   val button = new Button("start/stop")
   button.onAction = (event: ActionEvent) =>  {
     actorPost ! new LockMessage {}
   }
   var canvas = new Canvas(Constants.DRAWING_PANEL_SIZE_X, Constants.DRAWING_PANEL_SIZE_Y)

   canvas.graphicsContext2D.fill = Red
   stage = new JFXApp.PrimaryStage {
     title.value = "planet calculator"
     width = Constants.PANEL_SIZE_X
     height = Constants.PANEL_SIZE_Y
     scene = new Scene {
       root = new BorderPane {
         padding = Insets(15)
         right = button
         left = canvas
       }
     }
   }
   actorInit ! "Init"                                                // Sending messages by using !
 }


