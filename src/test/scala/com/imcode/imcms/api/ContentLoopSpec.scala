package com.imcode.imcms.api

import com.imcode._
import com.imcode.imcms.mapping.orm.{ContentLoopOps, ContentLoop, DocRef}
import scala.collection.JavaConverters._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterEach, WordSpec}
import org.junit.Assert._
import com.imcode.imcms.test.fixtures.{DocItemFX, DocRefFX}

@RunWith(classOf[JUnitRunner])
class ContentLoopSpec extends WordSpec with BeforeAndAfterEach {

  val LoopFx = new {
    val FirstContentIndex = 0
    val LastContentIndex = 9
    val VacantContentIndex = 10
    val ContentsCount = 10

    val NextContentNo = 10
  }

  def mkContentLoop(id: JLong = null, no: JInteger = DocItemFX.DefaultNo, docRef: DocRef = DocRefFX.Default, contentsCount: Int = LoopFx.ContentsCount) =
    ContentLoop.builder().id(id).no(no).docRef(docRef) |>> { builder =>
      (0 until contentsCount).foreach(builder.addContent)
    } build()


  "ContentLoop.builder().build()" should {
    "create new ContentLoop instance with default values" in {
      ContentLoop.builder().build() |> { loop =>
        assertNull(loop.getId)
        assertNull(loop.getNo)
        assertNull(loop.getDocRef)

        assertNotNull(loop.getContents)

        assertTrue(loop.getContents.isEmpty)
      }
    }
  }


  "ContentLoop.builder(Content).build()" should {
    "create new ContentLoop instance with values copied from provided content loop" in {
      ContentLoop.builder() |> {
        _.id(1L)
         .no(2)
         .docRef(DocRef.of(1001, 0))
         .addContent(0)
         .addContent(1)
         .addContent(2)
         .disableContent(0)
         .build()
      } |> { loop =>
        ContentLoop.builder(loop).build()
      } |> { loop =>
        assertEquals(1L, loop.getId)
        assertEquals(2, loop.getNo)
        assertEquals(DocRef.of(1001, 0), loop.getDocRef)

        assertEquals(3, loop.getContents.size)
        assertEquals(2, loop.getContents.asScala.count(_.isEnabled))
      }
    }
  }


  "Add first content" should {
    "return a pair of updated loop and added content with lowest index and highest no" in {
      val loop = mkContentLoop()
      val ops = new ContentLoopOps(loop)

      ops.addContentFirst() |> { loopAndContent =>
        val updatedLoop = loopAndContent.loop()
        val newContent = loopAndContent.content()
        val expectedNewContentNo = LoopFx.NextContentNo
        val expectedNewContentIndex = 0

        assertEquals(LoopFx.ContentsCount, loop.getContents.size)

        assertNotSame(loop, updatedLoop)

        assertEquals(LoopFx.ContentsCount + 1, updatedLoop.getContents.size)
        assertEquals(LoopFx.ContentsCount + 1, updatedLoop.getContents.asScala.count(_.isEnabled))

        assertEquals(expectedNewContentNo, newContent.getNo)

        updatedLoop.getContents.get(expectedNewContentIndex) |> { firstContent =>
          assertSame(newContent, firstContent)
        }
      }
    }
  }


  "Add last content" should {
    "return pair of updated loop with added content with highest index and highest no" in {
      val loop = mkContentLoop()
      val ops = new ContentLoopOps(loop)

      ops.addContentLast() |> { loopAndContent =>
        val updatedLoop = loopAndContent.loop()
        val newContent = loopAndContent.content()
        val expectedNewContentNo = LoopFx.NextContentNo
        val expectedNewContentIndex = 10

        assertEquals(LoopFx.ContentsCount, loop.getContents.size)
        assertEquals(LoopFx.ContentsCount, loop.getContents.asScala.count(_.isEnabled))

        assertNotSame(loop, updatedLoop)

        assertEquals(LoopFx.ContentsCount + 1, updatedLoop.getContents.size)
        assertEquals(LoopFx.ContentsCount + 1, updatedLoop.getContents.asScala.count(_.isEnabled))

        assertEquals(expectedNewContentNo, newContent.getNo)

        updatedLoop.getContents.get(expectedNewContentIndex) |> { lastContent =>
          assertSame(newContent, lastContent)
        }
      }
    }
  }


  "Add content before other content" which {
    "does not exist" should {
      "throw IndexOutOfBoundsException" in {
        val loop = mkContentLoop()
        val loopOps = new ContentLoopOps(loop)

        intercept[IndexOutOfBoundsException] {
          loopOps.addContentBefore(LoopFx.VacantContentIndex)
        }
      }
    }

    "exists" should {
      "return updated content and inserted content" in {
        mkContentLoop() |> { loop =>
          val ops = new ContentLoopOps(loop)

          ops.addContentBefore(LoopFx.LastContentIndex) |> { loopAndContent =>
            val contents = loopAndContent.loop().getContents
            val content = loopAndContent.content()

            assertEquals(LoopFx.ContentsCount + 1, contents.size)
            assertEquals(LoopFx.NextContentNo, content.getNo)
            assertSame(content, contents.get(LoopFx.LastContentIndex))
          }
        }
      }
    }
  }


  "Delete content" which {
    "does not exist" should {
      "throw IndexOutOfBoundsException" in {
        val loop = mkContentLoop()
        val loopOps = new ContentLoopOps(loop)

        intercept[IndexOutOfBoundsException] {
          loopOps.disableContent(LoopFx.VacantContentIndex)
        }
      }
    }

    "exists" should {
      "return updated loop" in {
        mkContentLoop() |> { loop =>
          assertTrue("content exists", new ContentLoopOps(loop).findContent(5).isPresent)

          val ops = new ContentLoopOps(loop)

          ops.deleteContent(5) |> { updatedLoop =>
            val contents = updatedLoop.getContents.asScala
            assertEquals(LoopFx.ContentsCount - 1, contents.size)
            assertFalse("content exists", updatedLoop.ops.findContent(5).isPresent)
          }
        }
      }
    }
  }


  "Add content after other content" which {
    "does not exist" should {
      "throw IndexOutOfBoundsException" in {
        val loop = mkContentLoop()
        val loopOps = new ContentLoopOps(loop)

        intercept[IndexOutOfBoundsException] {
          loopOps.addContentAfter(LoopFx.VacantContentIndex)
        }
      }
    }

    "exists" should {
      "return updated content and inserted content" in {
        mkContentLoop() |> { loop =>
          val ops = new ContentLoopOps(loop)

          ops.addContentAfter(LoopFx.FirstContentIndex) |> { loopAndContent =>
            val contents = loopAndContent.loop().getContents
            val content = loopAndContent.content()

            assertEquals(LoopFx.ContentsCount + 1, contents.size)
            assertEquals(LoopFx.NextContentNo, content.getNo)
            assertSame(content, contents.get(LoopFx.FirstContentIndex + 1))
          }
        }
      }
    }
  }


  "Disable content" which {
    "does not exist" should {
      "throw IndexOutOfBoundsException" in {
        val loop = mkContentLoop()
        val loopOps = new ContentLoopOps(loop)

        intercept[IndexOutOfBoundsException] {
          loopOps.disableContent(LoopFx.VacantContentIndex)
        }
      }
    }

    "exist" should {
      "return updated loop" in {
        mkContentLoop() |> { loop =>
          val ops = new ContentLoopOps(loop)

          ops.disableContent(5) |> { updatedLoop =>
            val contents = updatedLoop.getContents.asScala
            assertEquals(LoopFx.ContentsCount - 1, contents.count(_.isEnabled))

            contents.zipWithIndex.filter { case (content, _) => !content.isEnabled } |> { disabledContents =>
              assertEquals(1, disabledContents.size)
              assertEquals(5, disabledContents.head._2)
            }
          }
        }
      }
    }
  }


  "Enable content" which {
    "does not exist" should {
      "throw IndexOutOfBoundsException" in {
        val loop = mkContentLoop()
        val loopOps = new ContentLoopOps(loop)

        intercept[IndexOutOfBoundsException] {
          loopOps.enableContent(LoopFx.VacantContentIndex)
        }
      }
    }

    "exist" should {
      "return updated loop" in {
        ContentLoop.builder().addContent(0).addContent(1).addContent(2).disableContent(1).build() |> { loop =>
          loop.getContents |> { contents =>
            assertTrue(contents.get(0).isEnabled)
            assertTrue(contents.get(1).isDisabled)
            assertTrue(contents.get(2).isEnabled)
          }

          assertEquals(3, loop.getContents.size)
          assertEquals(2, loop.getContents.asScala.count(_.isEnabled))

          val ops = new ContentLoopOps(loop)

          ops.enableContent(1) |> { updatedLoop =>
            assertEquals(3, updatedLoop.getContents.size)
            assertEquals(3, updatedLoop.getContents.asScala.count(_.isEnabled))
          }
        }
      }
    }
  }


  "Move non existing content forward" should {
    "throw IndexOutOfBoundsException" in {
      val loop = mkContentLoop()
      val loopOps = new ContentLoopOps(loop)

      intercept[IndexOutOfBoundsException] {
        loopOps.moveContentForward(LoopFx.VacantContentIndex)
      }
    }
  }


  "Move existing content forward" should {
    "return the loop unchanged" when {
      "the content is the last" in {
        mkContentLoop() |> { loop =>
          assertEquals(loop, new ContentLoopOps(loop).moveContentForward(LoopFx.LastContentIndex))
        }
      }

      "all following contents are disabled" in {
        ContentLoop.builder(mkContentLoop()) |> { builder =>
          6 to 9 foreach builder.disableContent
          builder.build()
        } |> { loop =>
          assertEquals(loop, loop.ops.moveContentForward(5))
        }
      }
    }
  }


  "Move existing content forward" should {
    "swap the content with a next content and return updated loop" in {
      mkContentLoop() |> { loop =>
        val contentAt5 = loop.getContents.get(5)
        val contentAt6 = loop.getContents.get(6)

        val ops = new ContentLoopOps(loop)

        ops.moveContentForward(5) |> { updateLoop =>
          assertSame(contentAt5, updateLoop.getContents.get(6))
          assertSame(contentAt6, updateLoop.getContents.get(5))
        }
      }
    }

    "place the content next after a nearest enabled content with greater index and return updated loop" in {
      ContentLoop.builder(mkContentLoop()) |> { builder =>
        5 to 7 foreach builder.disableContent
        builder.build()
      } |> { loop =>
        val contentAt4 = loop.getContents.get(4)
        val contentAt8 = loop.getContents.get(8)

        val ops = new ContentLoopOps(loop)

        ops.moveContentForward(4) |> { updateLoop =>
          assertSame(contentAt4, updateLoop.getContents.get(8))
          assertSame(contentAt8, updateLoop.getContents.get(7))
        }
      }
    }
  }


  "Move non existing content backward" should {
    "throw IndexOutOfBoundsException" in {
      val loop = mkContentLoop()
      val loopOps = new ContentLoopOps(loop)

      intercept[IndexOutOfBoundsException] {
        loopOps.moveContentBackward(LoopFx.VacantContentIndex)
      }
    }
  }


  "Move existing content backward" should {
    "return the loop unchanged" when {
      "the content is the first" in {
        val loop = mkContentLoop()
        val updatedLoop = loop.ops.moveContentBackward(LoopFx.FirstContentIndex)

        assertEquals(loop, updatedLoop)
      }

      "all previous contents are disabled" in {
        ContentLoop.builder(mkContentLoop()) |> { builder =>
          0 to 5 foreach builder.disableContent
          builder.build()
        } |> { loop =>
          assertEquals(loop, loop.ops.moveContentBackward(6))
        }
      }
    }
  }


  "Move existing content backward" should {
    "swap the content with a prev content and return updated loop" in {
      mkContentLoop() |> { loop =>
        val contentAt5 = loop.getContents.get(5)
        val contentAt6 = loop.getContents.get(6)

        val ops = new ContentLoopOps(loop)

        ops.moveContentBackward(6) |> { updateLoop =>
          assertSame(contentAt5, updateLoop.getContents.get(6))
          assertSame(contentAt6, updateLoop.getContents.get(5))
        }
      }
    }

    "place the content before nearest enabled content with lower index and return updated loop" in {
      ContentLoop.builder(mkContentLoop()) |> { builder =>
        5 to 7 foreach builder.disableContent
        builder.build()
      } |> { loop =>
        val contentAt4 = loop.getContents.get(4)
        val contentAt8 = loop.getContents.get(8)

        val ops = new ContentLoopOps(loop)

        ops.moveContentBackward(8) |> { updateLoop =>
          assertSame(contentAt4, updateLoop.getContents.get(5))
          assertSame(contentAt8, updateLoop.getContents.get(4))
        }
      }
    }
  }


  "Move non existing content top" should {
    "throw IndexOfBoundsException" in {
      val loop = mkContentLoop()
      val loopOps = new ContentLoopOps(loop)

      intercept[IndexOutOfBoundsException] {
        loopOps.moveContentFirst(LoopFx.VacantContentIndex)
      }
    }
  }


  "Move existing content top" should {
    "return the loop unchanged" when {
      "the content is allready at the top" in {
        val loop = mkContentLoop()
        val updatedLoop = new ContentLoopOps(loop).moveContentFirst(0)

        assertEquals(loop, updatedLoop)
      }
    }


    "return updated loop with the content at the top" in {
      val loop = mkContentLoop()
      val updatedLoop = loop.ops.moveContentFirst(5)

      assertEquals(loop.getContents.get(5), updatedLoop.getContents.get(0))
      assertEquals(loop.getContents.get(0), updatedLoop.getContents.get(1))
    }
  }


  "Move non existing content bottom" should {
    "throw IndexOfBoundsException" in {
      val loop = mkContentLoop()
      val loopOps = new ContentLoopOps(loop)

      intercept[IndexOutOfBoundsException] {
        loopOps.moveContentLast(LoopFx.VacantContentIndex)
      }
    }
  }


  "Move existing content bottom" should {
    "return a loop unchanged" when {
      "the content is already at the bottom" in {
        val loop = mkContentLoop()
        val loopOps = new ContentLoopOps(loop)
        val updatedLoop = loopOps.moveContentLast(LoopFx.LastContentIndex)

        assertEquals(loop, updatedLoop)
      }
    }


    "return updated loop with the content at the bottom" in {
      val loop = mkContentLoop()
      val updatedLoop = loop.ops.moveContentLast(5)

      assertEquals(loop.getContents.get(5), updatedLoop.getContents.get(LoopFx.LastContentIndex))
      assertEquals(loop.getContents.get(LoopFx.LastContentIndex), updatedLoop.getContents.get(LoopFx.LastContentIndex - 1))
    }
  }
}