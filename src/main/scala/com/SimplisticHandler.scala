package com

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString
import play.api.libs.json.Json
import akka.actor.ActorRef
import akka.actor.Props
import com.Queue
import akka.actor.PoisonPill
import akka.pattern.BackoffSupervisor
import akka.pattern.BackoffOpts
import scala.concurrent._
import scala.concurrent.duration._
import java.net.InetSocketAddress

class SimplisticHandler(multiplier : ActorRef, remoteAdress: InetSocketAddress) extends Actor{
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
          println("AAck")
          messageAck = command
        }
        else {
          val topic = (json \ "topic").as[String]
          println(command + ", " + topic) 

          val response = ByteString("okkk")
          sender() ! Write(response)

          if(command == "connect") {
            // create queue (message scheme)
            val childProps = Props(classOf[Queue])
            val supervisor = BackoffSupervisor.props(

              BackoffOpts.onStop(
                childProps,
                childName = "queue",
                minBackoff = 0.2.seconds,
                maxBackoff = 2.seconds,
                randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
              ))

            queueAddress = system.actorOf(supervisor, name="supervisor")
            
            // context.actorOf(supervisor, name = "mySupervisor")
            clientAddress = sender
            queueAddress ! ("set_client_address", clientAddress)
            // tells multiplier to send message to handler
            multiplier ! command
          }
          else if (command == "subscribe") {
            queueAddress ! (command, topic, self)
          }
          else if (command == "unsubscribe"){
            queueAddress ! (command, topic)
          }
        }
    }

    // sends messages to Client
    case ("send_message", topic: String, message: String) => {
      // println(queueAddress)
      queueAddress ! ("send_message", topic, message)
    }
    // sends message after ack with neccessary topic
    case ("send_message_client", message: String) => {
      if (messageAck == "Ack") {
        var messageToClient = message
        clientAddress ! Write(ByteString(messageToClient))

      }
    }

    // after receiving dead message sends its Address to Queue
    case "dead" => {
      Thread.sleep(500)
      queueAddress ! ("set_handler_adress", self)
    }
    

    case PeerClosed     => 
    {
        println("Connection Closed ....")
        
        context.stop(queueAddress)
        context.stop(self)
    }
  }
}
