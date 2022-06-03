package com

import akka.actor.Actor
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContextExecutor

class Router extends Actor{
    
    implicit val system: ActorSystem = context.system
    implicit val ec: ExecutionContextExecutor = system.dispatcher

  override def receive: Receive = {
      case message => {
          println(message)
      }
  }
}
