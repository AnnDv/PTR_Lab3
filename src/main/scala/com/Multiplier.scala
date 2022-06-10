package com

import akka.actor.Actor
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContextExecutor
import play.api.libs.json.Json
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.ArrayList
// import play.api.libs.json.Json



class Multiplier extends Actor{
    
    implicit val system: ActorSystem = context.system
    implicit val ec: ExecutionContextExecutor = system.dispatcher

  //cretes list of data object (topic and list of messages)
    var listOfData = Array[Topic]()

  override def receive: Receive = {
      // receive data and id from connector
      case (data : String, id: String) => {
        val (topic, message) = extractTopicAndMessageJson(data)

        // verify if topic exists in list of Data object
        val (ifExist, referenceData) = isTopicExist(topic)

        // appends new object to the list of objects or appends message to existed object
        if(ifExist)
        {
          appendMessageToTopic(message, referenceData)
        } 
        else {
          val newObject = createNewDataObject(topic, message)
          listOfData = listOfData.appended(newObject)
        }
        // println(ifExist)
      }

  }

  // ignores panic messages and extracts topic "lang"
  def extractTopicAndMessageJson (data : String) : (String, String)= {
    if(!data.contains("panic"))
    {
        val json = Json.parse(data)
        val message = (json \ "message")
        val tweet = (message \ "tweet")
        val user = (tweet \ "user")
        val lang = (user \ "lang")
        val text = (tweet \ "text")
        return (lang.as[String], text.as[String])
        // println(data)
    }
    return (null, null)
  } 
// if topic exist it return true and data object(topic and listOfMessages), if false -> false and null
  def isTopicExist (topic:String) : (Boolean, Topic) = {
    if (listOfData.length > 0)
      for (item <- listOfData) {
        if (item.getTopicName() == topic) 
          return (true, item)
      }
    return (false, null)
  }

  // creates new object
  def createNewDataObject (topic:String, message:String) : Topic = {
    val data = new Topic(topic, Array(message))
    return data
  }

  // appends message to existed object
  def appendMessageToTopic (message:String, dataReference:Topic) = {
    //access existed reference to topicName and add new message 
    dataReference.appendMessage(message)
  }
}
