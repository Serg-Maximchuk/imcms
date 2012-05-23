package com

import scala.util.control.{Exception => Ex}

package object imcode {

  type JBoolean = java.lang.Boolean
  type JByte = java.lang.Byte
  type JCharacter = java.lang.Character
  type JInteger = java.lang.Integer
  type JLong = java.lang.Long
  type JFloat = java.lang.Float
  type JDouble = java.lang.Double
  type JClass[A >: Null] = java.lang.Class[A]
  type JCollection[A <: AnyRef] = java.util.Collection[A]
  type JList[A <: AnyRef] = java.util.List[A]
  type JSet[A <: AnyRef] = java.util.Set[A]
  type JMap[A <: AnyRef, B <: AnyRef] = java.util.Map[A, B]

  class Piper[A](a: A) {
    def |>[B](f: A => B): B = f(a)

    def |>>(f: A => Any): A = { f(a); a }
  }

  implicit def any2Piper[A](a: A) = new Piper(a)

  def ??? = new Exception().getStackTrace()(1) |> { se =>
    sys.error("Not implemented: %s.%s".format(se.getClassName, se.getMethodName))
  }


  // scala bug: 'import Option.{apply => opt}' - 'opt' can not be used as a function
  // scala> import Option.apply
  // import Option.apply
  //
  // scala> import Option.{apply => opt}
  // import Option.{apply=>opt}
  //
  // scala> "foo" |> Option.apply
  // res0: Option[java.lang.String] = Some(foo)
  //
  // scala> "foo" |> apply
  // res1: Option[java.lang.String] = Some(foo)
  //
  // scala> opt("foo")
  // res2: Option[java.lang.String] = Some(foo)
  //
  // scala> "foo" |> opt
  // <console>:12: error: value opt is not a member of object Option
  //               "foo" |> opt
  def opt[A](value: A) = Option(value)

  def when[A](exp: Boolean)(byName: => A): Option[A] = PartialFunction.condOpt(exp) { case true => byName }

  def doto[A](exp: A, exps: A*)(f: A => Any) {
    exp +: exps foreach f
  }


  // move to collections
  def unfold[A, B](init: A)(f: A => Option[(B, A)]): List[B] = f(init) match {
    case None => Nil
    case Some((r, next)) => r :: unfold(next)(f)
  }


  // scala bug: package methods overloading does not work
  object Atoms {
    import java.util.concurrent.atomic.AtomicReference

    def OptRef[A] = new AtomicReference(Option.empty[A])
    def OptRef[A](value: A) = new AtomicReference(Option(value))
    def Ref[A <: AnyRef] = new AtomicReference[A]
    def Ref[A <: AnyRef](value: A) = new AtomicReference(value)

    def swap[A](ref: AtomicReference[A])(f: A => A): A = ref.get |> f |>> ref.set
    def swap[A](f: A => A)(ref: AtomicReference[A]): A = swap(ref)(f)
  }


  /** extractor */
  object IntNum {
    def unapply(s: String): Option[Int] = Ex.catching(classOf[NumberFormatException]).opt(s.toInt)
  }


  /** extractor */
  object PosInt {
    def unapply(s: String): Option[Int] = IntNum.unapply(s).filter(_ >= 0)
  }


  /** extractor */
  object NegInt {
    def unapply(s: String): Option[Int] = IntNum.unapply(s).filter(_ < 0)
  }


  //?? delete ??

  //implicit val orderingJInteger = new Ordering[JInteger] { def compare(i1: JInteger, i2: JInteger) = i1 compareTo i2 }

  //def flip[A1, A2, B](f: A1 => A2 => B): A2 => A1 => B = x1 => x2 => f(x2)(x1)






  trait ManagedResource[R] {
    def close(resource: R)
  }

  object ManagedResource {
    implicit def stManagedResource[R <: { def close() }](r: R) = new ManagedResource[R] {
      def close(resource: R) { resource.close() }
      override def toString = "ManagedResource[_ <: def close()]"
    }

    implicit def ioManagedResource[R <: java.io.Closeable] = new ManagedResource[R] {
      def close(resource: R) { resource.close() }
      override def toString = "ManagedResource[_ <: java.io.Closeable]"
    }
  }


  def using[R: ManagedResource, A](resource: R)(f: R => A): A =
    try {
      f(resource)
    } finally {
      if (resource != null) {
        Ex.allCatch(implicitly[ManagedResource[R]].close(resource))
      }
    }

  /** Creates zero arity fn from by-name parameter. */
  //def toF[A](byName: => A): () => A = byName _

//  def bmap[A](test: => Boolean)(byName: => A): List[A] = {
//    import collection.mutable.ListBuffer
//
//    val ret = new ListBuffer[A]
//    while (test) ret += byName
//    ret.toList
//  }

  /**
   * Converts camel-case string into underscore.
   * ex: IPAccess => ip_access, SearchTerms => search_terms, mrX => mr_x, iBot => i_bot
   */
  @deprecated("prototype")
  def camelCaseToUnderscore(s: String): String = {
    def camelCaseToUnderscore(chars: List[Char]): List[Char] =
      chars span (c => c.isLower || !c.isLetter) match {
        case (lowers, Nil) => lowers
        case (Nil, rest) => (rest span (_.isUpper) : @unchecked) match {
          case (u :: Nil, rest) => camelCaseToUnderscore(u.toLower :: rest)
          case (uppers @ (u1 :: u2 :: _), rest) =>
            if (rest.isEmpty) uppers map (_.toLower)
            else uppers.init.map(_.toLower) ++ ('_' :: camelCaseToUnderscore(uppers.last.toLower :: rest))
        }
        case (lowers, rest) => lowers ++ ('_' :: camelCaseToUnderscore(rest))
      }

    camelCaseToUnderscore(s.toList) mkString
  }

//class Default[T](init: => T) { def value = init }
//
//object Default {
//  final class Ops[A >: Null](value: A)(implicit default: Default[A]) {
//    def orDefault = if (value != null) value else default.value
//  }
//
//  implicit def mkOps[A >: Null : Default](value: A) = new Ops(value)
//
//  implicit object defaultString extends Default("")
//  implicit object defaultJInteger extends Default[java.lang.Integer](0)
//
//  implicit def defaultList[A] = new Default[List[A]](List.empty)
//  implicit def defaultMap[A, B] = new Default[Map[A, B]](Map.empty)
//  implicit def defaultArray[A : ClassManifest] = new Default[Array[A]](Array())
//}

}