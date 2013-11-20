package com.imcode
package imcms.admin.chat

import scala.collection.JavaConverters._

import com.vaadin.ui._
import com.imcode.imcms.vaadin.component._
import java.util.Date

class MessageView(sender: String, message: String) extends VerticalLayout {
  val lytHeader = new HorizontalLayout {
    val lblSender = new Label(sender) {setWidth("100%")}
    val lblDetails = new Label("Sent: " + (new Date).toString)

    this.addComponents(lblSender, lblDetails)
    //setExpandRatio(lblSender, 1.0f)

    setWidth("100%")
    setHeight(null)
    setSpacing(true)
  }

  val lblText = new Label(message) {setWidth("100%")}

  this.addComponents(lytHeader, lblText)
  setWidth("100%")
  setHeight(null)
}

class MessagesPanel extends Panel(new VerticalLayout{setSpacing(true)}) with FullSize {
  //setStyle(Panel.STYLE_LIGHT)

  def addMessage(msg: MessageView) = synchronized {
//    getComponentIterator.toList |> { components =>
//      if (components.length > 3) removeComponent(components.head)
//    }
//
//    addComponent(msg)
  }
  
}

class Chat extends VerticalLayout {
  val pnlMessages = new MessagesPanel
  val txaText = new TextArea() |>> { t => t.setRows(3); t.setSizeFull }
  val btnSend = new Button("Send") { setHeight("100%") }

  val lytMessage = new HorizontalLayout {
    this.addComponents(txaText, btnSend)
    setExpandRatio(txaText, 1.0f)
    setWidth("100%")
    setHeight("50px")
  }

  setSpacing(true)
  this.addComponents(pnlMessages, lytMessage)
  setExpandRatio(pnlMessages, 1.0f)
  setSizeFull
}

//    new Chat {
//    setCaption("Chat messages")
//      setMargin(true)
//      val subscriber = actor {
//        loop {
//          react {
//            case ChatTopic.Message(text) =>
//              pnlMessages addMessage new MessageView("#user#", text)
//              pnlMessages.requestRepaint
//            case _ =>
//          }
//        }
//      }
//
//      btnSend addListener block {
//        ChatTopic ! ChatTopic.Message(txtText.getValue.asInstanceOf[String])
//        txtText setValue ""
//      }
//      ChatTopic ! ChatTopic.Subscribe(subscriber)
//    } //chat


//object ChatTopic extends Actor {
//
//  case class Subscribe(subscriber: Actor)
//  case class Message(text: String)
//
//  var subscribers: Set[Actor] = Set.empty
//
//  def act {
//    loop {
//      react {
//        case Subscribe(subscriber) =>
//          subscribers += subscriber // send 10 last messages?
//        case msg : Message => subscribers foreach (_ ! msg)
//        case other => println("Unknown message: " + other)
//      }
//    }
//  }
//
//  start()
//}