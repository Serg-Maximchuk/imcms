package com.imcode.util.event

trait Publisher[T] {

  var listeners = Seq.empty[T => Unit]

  def listen(listener: T => Unit) {
    listeners :+= listener
  }

  def notifyListeners(ev: T): Unit = for (l <- listeners) l(ev)

  def notifyListeners(): Unit = ???
}