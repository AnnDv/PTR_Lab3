package com

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString
import play.api.libs.json.Json
import akka.actor.ActorRef
import akka.actor.Props
import com.Queue

class SimplisticHandler(multiplier : ActorRef) extends Actor{
  import Tcp._
  import context.system

  var clientAddress : ActorRef = null
  var queueAddress : ActorRef = null

  override def receive = {

    case Received(data) => {
      // extracts command and topic received from the client
      // JsUndefined
        
        var dataString = data.utf8String
        val json = Json.parse(dataString)
        val command = (json \ "command").as[String]

        if (command == "sendFromProducer") {
          val receiveData = (json \ "data").as[String]
          val receiveId = (json \ "id").as[String]
          multiplier ! (receiveData, receiveId)
          // println(data)
        }
        else {
          val topic = (json \ "topic").as[String]
          println(command + ", " + topic) 

          val response = ByteString("okkk")
          sender() ! Write(response)

          if(command == "connect") {
            // create queue (message scheme)
            queueAddress = context.actorOf(Props[Queue])
            clientAddress = sender
            // tells multiplier to send message to handler
            multiplier ! command
          }
          else if (command == "subscribe") {
            queueAddress ! (command, topic)
          }
          else if (command == "unsubscribe"){
            queueAddress ! (command, topic)

          }
        }
        
        
    }

    // sends messages to Client
    case ("send_message", topic: String, message: String) => {
      // println(clientAddress)
      queueAddress ! ("send_message", topic, message)
    }
    // sends message with neccessary topic
    case ("send_message_client", message: String) => {
      clientAddress ! Write(ByteString(message))
    }

    case PeerClosed     => 
    {
        println("Connection Closed ....")
        context.stop(self)
    }
  }
}
