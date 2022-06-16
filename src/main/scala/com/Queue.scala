package com 

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer

class Queue extends Actor{
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
            for (item <- listOfTopics) {
                if (item == topic) {
                    listOfMessages = listOfMessages.appended(message)
                    addressHandler ! ("send_message_client", message)
                    listOfMessages -= message
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
   }
}
