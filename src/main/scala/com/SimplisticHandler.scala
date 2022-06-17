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
  var messageAck : String = null

  override def receive = {
    case Received(data) => {
      // extracts command and topic received from the client
        messageAck = null
        var dataString = data.utf8String
        val json = Json.parse(dataString)
        val command = (json \ "command").as[String]

        // verifies if message are from Producer and receives data from it
        if (command == "sendFromProducer") {
          val receiveData = (json \ "data").as[String]
          val receiveId = (json \ "id").as[String]
          multiplier ! (receiveData, receiveId)
          // println(data)
        }

        // message acknowledgment from the client
        else if (command == "Ack") {
          // println("AAck")
          messageAck = command
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
            queueAddress ! ("set_client_address", clientAddress)
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
    // sends message after ack with neccessary topic
    case ("send_message_client", message: String) => {
      if (messageAck == "Ack") {
        var messageToClient = "[next after ack]  " + message
        clientAddress ! Write(ByteString(messageToClient))

      }
    }
    

    case PeerClosed     => 
    {
        println("Connection Closed ....")
        context.stop(queueAddress)
        context.stop(self)
    }
  }
}
