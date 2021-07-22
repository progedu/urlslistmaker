package jp.ed.nnn.urlslistmaker

import akka.actor.Actor

import scala.io.{Codec, Source}

class UrlsFileLoader(config: Config) extends Actor {
  override def receive = {

    case LoadUrlsFile =>
      val urlsFileSource = Source.fromFile(config.urlsFilePath)(Codec.UTF8)
      val urlsIterator = urlsFileSource.getLines()
      urlsIterator.foreach((line) => {
        val webPageUrl = WebPageUrl(line)
        sender() ! webPageUrl
      })
      urlsFileSource.close()
  }
}
