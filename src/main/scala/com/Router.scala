package com

import akka.actor.Actor
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContextExecutor
// import play.api.libs.json.Json



class Router extends Actor{
    
    implicit val system: ActorSystem = context.system
    implicit val ec: ExecutionContextExecutor = system.dispatcher

  override def receive: Receive = {
      case message => {
          print(message)
          println(new Object())
      }
  }
}
