package com

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.alpakka.sse.scaladsl.EventSource
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContextExecutor
import akka.compat.Future
import scala.concurrent.Future
import akka.Done
import akka.stream.scaladsl.Source
import akka.NotUsed
import scala.collection.immutable
import akka.stream.ThrottleMode
import akka.stream.scaladsl.Sink
import scala.concurrent.duration.DurationInt
import java.util.UUID.randomUUID
import akka.actor.ActorRef
import akka.actor.Props

class Connector(router : ActorRef) extends Actor{

    implicit val system: ActorSystem = context.system
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    // val routerScaler: ActorRef = system.actorOf(Props[Router], "router")

  override def receive: Receive = {
      case  "first" =>
        val send: HttpRequest => Future[HttpResponse] = Http().singleRequest(_)

        val eventSource: Future[Done] =
            EventSource(
            uri = Uri(s"http://localhost:4000/tweets/1"),
            send,
            ).runForeach(event=>{
                    
                        val temp = event.getData()
                        val id = randomUUID().toString
                        router ! (temp, id)
                        // autoScaler ! getCurrentMinute
                    }
            )

        val eventSource2: Future[Done] =
            EventSource(
            uri = Uri(s"http://localhost:4000/tweets/2"),
            send,
            ).runForeach(event=>event)
    }

    def executeEvents(eventSource: Source[ServerSentEvent, NotUsed], eventSource2: Source[ServerSentEvent, NotUsed]): Unit = {
    val eventsList: List[Source[ServerSentEvent, NotUsed]] = List(eventSource, eventSource2)

    val list: List[Future[immutable.Seq[ServerSentEvent]]] = eventsList.toStream.map(sse => {
      sse.throttle(elements = 1, per = 500.milliseconds, maximumBurst = 1, ThrottleMode.Shaping)
        .take(20)
        .runWith(Sink.seq)
    }).toList
}
  
}