package com

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString
import play.api.libs.json.Json
import akka.actor.ActorRef

class SimplisticHandler(multiplier : ActorRef) extends Actor{
  import Tcp._
  import context.system

  var clientAddress : ActorRef = null
  override def receive = {

    case Received(data) => {
      // extracts command and topic received from the client
        val json = Json.parse(data.utf8String)
        val command = (json \ "command").as[String]
        val topic = (json \ "topic").as[String]
        println(command + ", " + topic)

        val response = ByteString("okkk")
        sender() ! Write(response)

        if(command == "connect") {
          clientAddress = sender
          multiplier ! command
        }
    }

    // sends messages to Client
    case ("send_message", message: String) => {
      // println(clientAddress)
      clientAddress ! Write(ByteString(message))
    }
    case PeerClosed     => 
    {
        println("Connection Closed ....")
        context.stop(self)
    }
  }
}
