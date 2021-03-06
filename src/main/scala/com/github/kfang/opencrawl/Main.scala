package com.github.kfang.opencrawl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.github.kfang.opencrawl.routing.V1Routes

import scala.concurrent.ExecutionContext
import scala.util.Failure

object Main extends App {

  private val config = new Configuration
  implicit val system = ActorSystem("open-crawl", config.CONFIG)
  implicit val materializer = ActorMaterializer()

  implicit val __ctx: ExecutionContext = system.dispatcher

  private val _start = for {
    database  <- Database.connect(config)
    services  = new Services(system, database)
    routes    = new V1Routes(database, services).routes
    binding   <- Http().bindAndHandle(routes, "0.0.0.0", 8080)
  } yield {
    system.log.info(s"Successfully bound ${binding.localAddress}")
    binding
  }

  _start.andThen({
    case Failure(err) =>
      system.log.error(err, "System Failed to Start")
      system.terminate()
  })

}
