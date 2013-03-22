package com.imcode
package imcms.mapping

import scala.collection.JavaConverters._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{WordSpec}
import DocumentStoringVisitor.getFilenameForFileDocumentFile
import imcms.api.DocumentVersion.WORKING_VERSION_NO
import com.imcode.imcms.api.DocRef

@RunWith(classOf[JUnitRunner])
class FilenameSpec extends WordSpec {

  "the result of getFilenameForFileDocumentFile invocation" when {
    "doc version is a working version and file id is a blank" should {
      "be just 'docId'" in {
        expectResult("1001") {
          getFilenameForFileDocumentFile(DocRef.of(1001, WORKING_VERSION_NO), null)
        }

        expectResult("1111") {
          getFilenameForFileDocumentFile(DocRef.of(1111, WORKING_VERSION_NO), "")
        }
      }
    }

    "doc version is a working version and file id is *not* a blank" should {
      "be 'docId.fileId'" in {
        expectResult("1001.10") {
          getFilenameForFileDocumentFile(DocRef.of(1001, WORKING_VERSION_NO), 10.toString)
        }

        expectResult("1234.56") {
          getFilenameForFileDocumentFile(DocRef.of(1234, WORKING_VERSION_NO), 56.toString)
        }

        expectResult("1212.ok") {
          getFilenameForFileDocumentFile(DocRef.of(1212, WORKING_VERSION_NO), "ok")
        }
      }
    }

    "doc version is not a working version and file id is a blank" should {
      "be 'docId_docVersionNo'" in {
        expectResult("1001_30") {
          getFilenameForFileDocumentFile(DocRef.of(1001, 30), null)
        }

        expectResult("1111_5") {
          getFilenameForFileDocumentFile(DocRef.of(1111, 5), "")
        }
      }
    }

    "doc version is not a working version and file id is *not* a blank" should {
      "be 'docId_docVersionNo.fileId'" in {
        expectResult("1001_4.2") {
          getFilenameForFileDocumentFile(DocRef.of(1001, 4), 2.toString)
        }

        expectResult("3210_6.11") {
          getFilenameForFileDocumentFile(DocRef.of(3210, 6), 11.toString)
        }

        expectResult("3412_2.abc") {
          getFilenameForFileDocumentFile(DocRef.of(3412, 2), "abc")
        }
      }
    }
  }
}