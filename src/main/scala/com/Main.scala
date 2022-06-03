package com

import akka.actor.ActorSystem
import akka.actor.Props

object Main {
  def main (args: Array[String]) : Unit = {
    println("Starrt")
      val system = ActorSystem("main")
      val multiplier = system.actorOf(Props[Multiplier], "multiplier")
      val connector = system.actorOf(Props(new Connector(multiplier)), name = "connector")
      connector ! "first"
  }
}
