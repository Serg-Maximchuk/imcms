package com.imcode
package imcms
package admin.system.file

import scala.collection.JavaConversions._
import com.vaadin.ui._
import com.imcode.imcms.vaadin._
import java.util.concurrent.atomic.AtomicReference
import com.imcode.util.event.Publisher
import java.io._
import com.vaadin.ui.Window.Notification

case class UploadedData(filename: String, mimeType: String, content: Array[Byte])

sealed trait UploadStatus
case object UploadNew extends UploadStatus
case class UploadStarted(event: Upload.StartedEvent) extends UploadStatus
case class UploadProgress(readBytes: Long, contentLength: Long) extends UploadStatus
case class UploadSucceeded(event: Upload.SucceededEvent, data: UploadedData) extends UploadStatus
case class UploadFailed(event: Upload.FailedEvent) extends UploadStatus



class FileUploadDialog(caption: String = "") extends OkCancelDialog(caption) {
  val upload = new FileUpload

  mainContent = upload.ui

  upload.listen { btnOk setEnabled _.isInstanceOf[UploadSucceeded] }

  btnCancel addClickHandler { upload.ui.upload.interruptUpload }
}

class FileUpload extends Publisher[UploadStatus] {
  private val dataRef = new AtomicReference(Option.empty[UploadedData])

  /** Creates file save as name from original filename. */
  var fileNameToSaveAsName = identity[String]_
  val ui = letret(new FileUploadUI) { ui =>
    val receiver = new Upload.Receiver {
      val out = new ByteArrayOutputStream
      def receiveUpload(filename: String, mimeType: String) = out
    }

    ui.upload.setReceiver(receiver)
    ui.upload.addListener(new Upload.StartedListener {
      def uploadStarted(ev: Upload.StartedEvent) = {
        reset()
        ui.txtSaveAsName.setEnabled(true)
        ui.txtSaveAsName.value = fileNameToSaveAsName(ev.getFilename)
        ui.txtSaveAsName.setEnabled(false)
        ui.pi.setEnabled(true)
        notifyListeners(UploadStarted(ev))
      }
    })
    ui.upload.addListener(new Upload.ProgressListener {
      def updateProgress(readBytes: Long, contentLength: Long) {
        ui.pi.setValue(Float.box(readBytes.toFloat / contentLength))
        notifyListeners(UploadProgress(readBytes, contentLength))
      }
    })
    ui.upload.addListener(new Upload.FailedListener {
      def uploadFailed(ev: Upload.FailedEvent) {
        ui.txtSaveAsName.setEnabled(true)
        ui.txtSaveAsName.value = ""
        ui.txtSaveAsName.setEnabled(false)
        ui.pi.setEnabled(false)
        ui.getApplication.showWarningNotification("file.upload.interrupted.warn.msg".i)
        notifyListeners(UploadFailed(ev))
      }
    })
    ui.upload.addListener(new Upload.SucceededListener {
      def uploadSucceeded(ev: Upload.SucceededEvent) {
        ui.txtSaveAsName.setEnabled(true)
        ui.chkOverwrite.setEnabled(true)
        ui.pi.setValue(1f)

        let(UploadedData(ev.getFilename, ev.getMIMEType, receiver.out.toByteArray)) { data =>
          dataRef.set(Some(data))
          notifyListeners(UploadSucceeded(ev, data))
        }
      }
    })
  }

  reset()

  def reset() {
    dataRef.set(None)
    ui.chkOverwrite.setEnabled(true)
    ui.chkOverwrite.value = false
    ui.chkOverwrite.setEnabled(false)
    ui.txtSaveAsName.setEnabled(true)
    ui.txtSaveAsName.value = ""
    ui.txtSaveAsName.setEnabled(false)
    ui.pi.setEnabled(true)
    ui.pi.setValue(0f)
    ui.pi.setPollingInterval(500)
    ui.pi.setEnabled(false)
    notifyListeners(UploadNew)
  }

  def data = dataRef.get

  def saveAsName = ui.txtSaveAsName.value

  def isOverwrite = ui.chkOverwrite.booleanValue
}

class FileUploadUI extends FormLayout with UndefinedSize {
  val upload = new Upload("file.upload.dlg.frm.fld.select".i, null) with Immediate
  val txtSaveAsName = new TextField("file.upload.dlg.frm.fld.save_as".i)
  val pi = new ProgressIndicator; pi.setCaption("file.upload.dlg.frm.fld.progress".i)
  val chkOverwrite = new CheckBox("file.upload.dlg.frm.fld.overwrite".i)

  upload.setButtonCaption("...")
  addComponents(this, upload, pi, txtSaveAsName, chkOverwrite)
}