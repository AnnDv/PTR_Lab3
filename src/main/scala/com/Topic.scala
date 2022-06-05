package com

// creates topic object (name and list of messages)
class Topic(topicName: String, var listOfMessages: Array[String])
{
    def getTopicName() : String =
    {
        return this.topicName
    }

    def appendMessage (message:String) = {
        this.listOfMessages = this.listOfMessages.appended(message)
    }

    def listLength () : Int = {
        return this.listOfMessages.length
    }
}
