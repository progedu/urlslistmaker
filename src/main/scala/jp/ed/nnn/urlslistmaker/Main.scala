package jp.ed.nnn.urlslistmaker

import akka.actor.{ActorSystem, Inbox, Props}

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {
  val urlsFilePath = "./urls.txt"
  val outputFile = "./com-sites.txt"
  val numOfDownloader = 2000
  val config = Config(
    urlsFilePath,
    outputFile,
    numOfDownloader)

  val system = ActorSystem("urlslistmaker")
  val inbox = Inbox.create(system)
  implicit val sender = inbox.getRef()

  val supervisor = system.actorOf(Props(new Supervisor(config)))
  supervisor ! Start

  inbox.receive(100.days)
  Await.ready(system.terminate(), Duration.Inf)
  println("Finished.")
}
