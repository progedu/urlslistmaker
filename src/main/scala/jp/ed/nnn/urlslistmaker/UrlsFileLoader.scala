package jp.ed.nnn.urlslistmaker

import akka.actor.Actor

import scala.io.{Codec, Source}

class UrlsFileLoader(config: Config) extends Actor {

  val urlsFileSource = Source.fromFile(config.urlsFilePath)(Codec.UTF8)
  val urlsIterator = urlsFileSource.getLines()

  override def receive = {

    case LoadUrlsFile =>
      if (urlsIterator.hasNext) {
        val line = urlsIterator.next()
        val webPageUrl = WebPageUrl(line)
        sender() ! webPageUrl
      } else {
        sender() ! Finished
      }
  }

  override def postStop(): Unit =  {
    super.postStop()
    urlsFileSource.close()
  }
}
