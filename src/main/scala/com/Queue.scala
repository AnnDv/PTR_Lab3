package com 

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer

class Queue extends Actor{
    var listOfTopics = ArrayBuffer[String]()
    var addressHandler : ActorRef = null

    // add topic to list
    override def receive: Receive = {
        case ("subscribe", topic: String) => {
            listOfTopics = listOfTopics.appended(topic)
            addressHandler = sender
            println(topic)
        }
        // filters the topics 
        case ("send_message", topic: String, message: String) => {
            for (item <- listOfTopics) {
                // item ! ("send_message", topic, message)
                if (item == topic) {
                    addressHandler ! ("send_message_client", message)
                    // println(topic)
                }
            }
        }
        // unsubscribe from the topic
        case ("unsubscribe", topic: String) => {
            listOfTopics -= topic
        }
   }
}
