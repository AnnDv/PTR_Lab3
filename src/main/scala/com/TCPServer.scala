package com

import akka.actor.Actor
import akka.io.IO
import akka.io.Tcp
import java.net.InetSocketAddress
import akka.actor.Props

class TCPServer extends Actor{
    import Tcp._
    import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 8080))

  def receive = {
    case b @ Bound(localAddress) =>
      context.parent ! b

    case CommandFailed(_: Bind) => context.stop(self)

    case c @ Connected(remote, local) =>
    //   println(local)

      val handler = context.actorOf(Props[SimplisticHandler]())
      val connection = sender()
      connection ! Register(handler)
      
  }
  
}
