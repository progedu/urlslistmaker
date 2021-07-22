package jp.ed.nnn.urlslistmaker

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import okhttp3._

import scala.io.Source


class Supervisor(config: Config) extends Actor {

  var originalSender = Actor.noSender

  var successCount = 0
  var failureCount = 0
  var fileLoadedUrlCount = 0

  val client = new OkHttpClient.Builder()
    .connectTimeout(1, TimeUnit.SECONDS)
    .writeTimeout(1, TimeUnit.SECONDS)
    .readTimeout(1, TimeUnit.SECONDS)
    .build()

  val router = {
    val downloaders = Vector.fill(config.numOfDownloader) {
      ActorRefRoutee(context.actorOf(
        Props(new WebPageLoader(
          config,
          client
        ))))
    }
    Router(RoundRobinRoutingLogic(), downloaders)
  }

  override def receive = {

    case Start => {
      originalSender = sender()
      val urlsFileLoader = context.actorOf(Props(new UrlsFileLoader(config)))
      urlsFileLoader ! LoadUrlsFile
    }

    case webPageUrl: WebPageUrl => {
      fileLoadedUrlCount += 1
      router.route(webPageUrl, self)
    }

    case DownloadSuccess => {
      successCount += 1
      printConsoleAndCheckFinish()
    }

    case DownloadFailure => {
      failureCount += 1
      printConsoleAndCheckFinish()
    }
  }

  private[this] def printConsoleAndCheckFinish(): Unit = {
    val total = successCount + failureCount
    println(s"total: ${total}, successCount: ${successCount}, failureCount: ${failureCount}")
    if (total == fileLoadedUrlCount) originalSender ! Finished
  }
}
