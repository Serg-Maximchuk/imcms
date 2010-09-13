package com

package object imcode {

  def let[B, T](expr: B)(block: B => T): T = block(expr)

  def forlet[T](exprs: T*)(block: T => Unit): Unit = exprs foreach block  

  def using[R <: {def close(): Unit}, T](resource: R)(block: R => T): T = try {
    block(resource)
  } finally {
    resource.close()
  }

  def bmap[T](test: => Boolean)(block: => T): List[T] = {
    import collection.mutable.ListBuffer
    
    val ret = new ListBuffer[T]
    while (test) ret += block
    ret.toList
  }

  /**
   * Converts camel-case string into underscore.
   * ex: IPAccess => ip_access, SearchTerms => search_terms, mrX => mr_x, iBot => i_bot
   */
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
}