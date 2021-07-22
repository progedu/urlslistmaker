package jp.ed.nnn.urlslistmaker

import java.io.{File, FileWriter, IOException}
import java.nio.file.{Files, Paths, StandardOpenOption}

import akka.actor.{Actor, ActorRef}
import okhttp3._

import scala.util.{Failure, Success, Try}

class DownloadFailException extends IOException

class WebPageLoader(config: Config,
                          client: OkHttpClient,
                         ) extends Actor {

  var originalSender = Actor.noSender

  val targetFile = new File(config.outputFile)
  if(!targetFile.exists()) targetFile.createNewFile()
  val fileWriter = new FileWriter(targetFile, true)

  override def receive = {

    case webPageUrl: WebPageUrl => {
      originalSender = sender()
      val url = "https://" + webPageUrl.domain + ".com"
      val request = new Request.Builder()
        .url(url)
        .build()


      client.newCall(request).enqueue(new Callback {
        override def onFailure(call: Call, e: IOException): Unit = originalSender ! DownloadFailure

        override def onResponse(call: Call, response: Response): Unit = {
          if (response.isSuccessful) {
            var titleR = """<title>(.+)</title>""".r
            var titleRM = titleR.findFirstMatchIn(response.body().string())
            var writeStr: String = "";
            titleRM match {
              case Some(p) => writeStr = p.group(1).toString
              case None => writeStr = "None"
            }

            Try {
              fileWriter.write(s"${webPageUrl.domain}.com\t${writeStr}\n")
            } match {
              case Success(v) => {
                originalSender ! DownloadSuccess
              }
              case Failure(e) => {
                originalSender ! DownloadFailure
              }
            }
            fileWriter.close()

          } else {
            originalSender ! DownloadFailure
          }
          response.close()
        }
      })
    }
  }
}
