package jp.ed.nnn.urlslistmaker

trait Message

sealed trait SupervisorMessage extends Message
case object Start extends SupervisorMessage
case object Finished extends SupervisorMessage with WebPageLoaderMessage
case class DownloadSuccess() extends SupervisorMessage
case class DownloadFailure() extends SupervisorMessage

sealed trait UrlsFileLoaderMessage extends Message
case object LoadUrlsFile extends UrlsFileLoaderMessage

sealed trait WebPageLoaderMessage extends Message

case object LoadWebPage extends WebPageLoaderMessage
case class WebPageUrl(domain: String)
  extends WebPageLoaderMessage
