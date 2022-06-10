package com

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString

class SimplisticHandler extends Actor{
  import Tcp._
  def receive = {
    case Received(data) => {
        println(data.utf8String)
        val response = ByteString("okkk")
        sender() ! Write(response)
    }
    case PeerClosed     => 
    {
        println("Connection Closed ....")
        context.stop(self)
    }
  }
}
