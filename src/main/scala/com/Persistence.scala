package com

import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer

class Persistence(var clientId : String, var messages : ArrayBuffer[String] = new ArrayBuffer())