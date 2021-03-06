package com

import akka.actor.ActorSystem
import akka.actor.Props

object Main {
  def main (args: Array[String]) : Unit = {
    println("Starrt")
    // creates actor system
      val system = ActorSystem("main")
    // creates new actor multiplier
      val multiplier = system.actorOf(Props[Multiplier], "multiplier")
    // tells actor to start working (receive messages from server)
      val tcpServer =  system.actorOf(Props(new TCPServer(multiplier)), "server")
  }
}
