package com

import akka.actor.Actor
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContextExecutor
import play.api.libs.json.Json
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
// import play.api.libs.json.Json



class Multiplier extends Actor{
    
    implicit val system: ActorSystem = context.system
    implicit val ec: ExecutionContextExecutor = system.dispatcher

  override def receive: Receive = {
      case (data : String, id: String) => {
        //   println(id)
        // extractTopicJson(data)
        val topic = extractTopicJson(data)
        println(topic)
      }

  }

  def extractTopicJson (data : String) : String = {
    if(!data.contains("panic"))
    {
        val json = Json.parse(data)
        val message = (json \ "message")
        val tweet = (message \ "tweet")
        val user = (tweet \ "user")
        val lang = (user \ "lang")
        return lang.as[String]
        println(data)
    }
    

    return null
  } 
}
