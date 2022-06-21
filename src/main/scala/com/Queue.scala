package com 

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer
import akka.actor.Timers
import scala.concurrent._
import scala.concurrent.duration._
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import akka.actor.PoisonPill
import java.net.InetSocketAddress
import com.Persistence
import play.api.libs.json.Writes
import play.api.libs.json.Json
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.StandardCharsets
import java.io._
import akka.actor.Kill
import akka.actor.FSM
import akka.persistence.SnapshotOffer
import akka.persistence.SaveSnapshotSuccess
import akka.persistence.SnapshotSelectionCriteria
import akka.persistence.Recovery

object Queue {
  private case object TickKey
  private case object FirstTick
  private case object Tick
}

class Queue extends Timers with PersistentActor{
    import Queue._
    var listOfTopics = ArrayBuffer[String]()
    var addressHandler : ActorRef = null
    // create list of messages
    var listOfMessages = ArrayBuffer[String]()
    var socketAdress : InetSocketAddress = null
    var messageCounter : Int = 0
    var messagesRecovered : Int = 0
    var state: Any = _

    var clientAddress : ActorRef = null
    override def persistenceId = "my-stable-persistence-id"
    override def receiveRecover: Receive = {
        case s"handle-$message" => {
            if(messagesRecovered < 2) {
                messagesRecovered += 1
            }
            
        }
        case SnapshotOffer(_, snapshot: String) =>
        {
            println("Snapshot:" + snapshot)
            if(snapshot != null && snapshot != " ")
            {
                var arr = snapshot.split("!!!!!")
                var arrListOfMessages = arr(0).split(">>>")
                var arrListOfTopics = arr(1).split("~~~~~~")
                for(item <- arrListOfMessages)
                {
                    listOfMessages = listOfMessages.appended(item)
                    // self ! ("send_recovery_message", item)
                }
                for(item <- arrListOfTopics)
                {
                    listOfTopics = listOfMessages.appended(item)
                    // self ! ("send_recovery_message", item)
                }
                // listOfTopics = listOfTopics.appendAll(arrListOfTopics)
            }
        }
        case RecoveryCompleted => {
            println("Recovery DONE")
        }
            
    }

    // add topic to list
    override def receiveCommand: Receive = {
        case SaveSnapshotSuccess(metadata) => {
            println("Snapshot Saved " + metadata.sequenceNr + " : " + metadata.timestamp)
            // deleteMessages(metadata.sequenceNr - 3)
        }
        case ("subscribe", topic: String, recievedAdressHandler : ActorRef) => {
            listOfTopics = listOfTopics.appended(topic)
            addressHandler = recievedAdressHandler
            // println(topic)
        }
        // filters the topics 
        case ("send_message", topic: String, message: String) => {

            for (item <- listOfTopics) {
                if (item == topic) {
                    listOfMessages = listOfMessages.appended(message)
                    timers.startSingleTimer(TickKey, FirstTick, 500.millis)
                }
            }
        }
        // unsubscribe from the topic
        case ("unsubscribe", topic: String) => {
            listOfTopics -= topic
        }

        case ("set_client_address", receivedClientAddress : ActorRef, recievedSocketAdress : InetSocketAddress) => {
            clientAddress = receivedClientAddress
            socketAdress = recievedSocketAdress
        }

        // recovers messages
        case ("send_recovery_message", message) => {
            println("REEEEEECCCCOOOOVERY")
            // addressHandler ! ("send_message_client", message)
        }

        // handler sends its address after its death
        case ("set_handler_adress", recievedAdressHandler : ActorRef) => {
            println("Handler Adress After Restart : " + recievedAdressHandler)
            addressHandler = recievedAdressHandler
        }

        // after 500 millisecinds calls FirstTick
        case FirstTick => {
            
            if(listOfMessages.length > 0)
            {
                var message = listOfMessages(0)
                if(addressHandler != null)
                {
                    addressHandler ! ("send_message_client", message)
                }

                // 
                listOfMessages -= message
                messageCounter += 1
                // every 3rd message snapshots are made 
                if(messageCounter == 3)
                {
                    var persistMessage = " "
                        if(listOfMessages.length > 0)
                        {
                            var counter = 0
                            // save last 3 messages in queue
                            while(counter < 3){
                                persistMessage += listOfMessages(counter) + ">>>"
                                counter += 1
                            }
                        }
                        // save a snapshot as a String, the message is divided by >>> (message) and ~~~~~~ (topic)
                        persistMessage += "!!!!!"

                        if(listOfTopics.length > 0)
                        {
                            for(message <- listOfTopics)
                            {
                                persistMessage += message + "~~~~~~"
                            }
                        }
                        
                        persist(s"handle-$persistMessage"){event =>
                            saveSnapshot(persistMessage)}
                }
                else if(messageCounter == 10)
                {
                    messageCounter = 0
                    self ! Kill
                }
            }
        }
      // do something useful here
   }

   override def preRestart(reason: Throwable, message: Option[Any]) = {
    println("I am restarting...")
    super.preRestart(reason, message)
   }

   override def postRestart(reason: Throwable) = {
    println("...restart completed!")
    super.postRestart(reason)
   }

   override def preStart() = println("I am alive")

   // after Kill, it sends dead command to address handler
   override def postStop() = {
    println("DEAD:" + listOfMessages.length)
    addressHandler ! "dead"
}

}
