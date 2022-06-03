package com

import akka.actor.ActorSystem
import akka.actor.Props

object Main {
  def main (args: Array[String]) : Unit = {
      val system = ActorSystem("main")
      val router = system.actorOf(Props[Router], "router")
      val connector = system.actorOf(Props(new Connector(router)), name = "connector")
      connector ! "first"
  }
}
