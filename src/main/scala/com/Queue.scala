package com 

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer
import akka.actor.Timers
import scala.concurrent._
import scala.concurrent.duration._

object Queue {
  private case object TickKey
  private case object FirstTick
  private case object Tick
}

class Queue extends Actor with Timers{
    import Queue._
    var listOfTopics = ArrayBuffer[String]()
    var addressHandler : ActorRef = null
    // create list of messages
    var listOfMessages = ArrayBuffer[String]()

    var clientAddress : ActorRef = null

    // add topic to list
    override def receive: Receive = {
        case ("subscribe", topic: String) => {
            listOfTopics = listOfTopics.appended(topic)
            addressHandler = sender
            println(topic)
        }
        // filters the topics 
        case ("send_message", topic: String, message: String) => {
            // println(topic)
            println(topic)
            for (item <- listOfTopics) {
                if (item == topic) {
                    listOfMessages = listOfMessages.appended(message)

                    timers.startSingleTimer(TickKey, FirstTick, 500.millis)

                    // println("ia message")
                    // Thread.sleep(1000)
                    
                    // println(topic)
                }
            }
        }
        // unsubscribe from the topic
        case ("unsubscribe", topic: String) => {
            listOfTopics -= topic
        }

        case ("set_client_address", receivedClientAddress : ActorRef) => {
            clientAddress = receivedClientAddress
        }

        case FirstTick => {
            var message = listOfMessages(0)
            addressHandler ! ("send_message_client", message)
            listOfMessages -= message
        }
      // do something useful here
           
   }
}
