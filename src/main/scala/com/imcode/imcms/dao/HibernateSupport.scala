package com.imcode
package imcms.dao

import org.hibernate._
import scala.reflect.ClassTag

trait HibernateSupport {

  @scala.reflect.BeanProperty
  var sessionFactory: SessionFactory = _

  @deprecated
  def flush(): Unit = hibernate.flush()

  object hibernate {

    import HibernateSupport.HibernateResultTransformer

    type NamedParam = (String, Any)


    def withCurrentSession[A](fn: Session => A): A = fn(sessionFactory.getCurrentSession)


    def flush(): Unit = withCurrentSession { _.flush() }


    private def setParams[Q <: Query](ps: Any*)(query: Q): Q = query |>> { _ =>
      for ((param, position) <- ps.zipWithIndex) query.setParameter(position, param.asInstanceOf[AnyRef])
    }

    private def setNamedParams[Q <: Query](namedParam: NamedParam, namedParams: NamedParam*)(query: Q): Q = query |>> { _ =>
      for ((name, value) <- namedParam +: namedParams) query.setParameter(name, value.asInstanceOf[AnyRef])
    }


    def runSqlQuery[A](queryString: String, ps: Any*)(fn: SQLQuery => A): A = withCurrentSession {
      _.createSQLQuery(queryString) |> setParams(ps: _*) |> fn
    }

    def runQuery[A](queryString: String, ps: Any*)(fn: Query => A): A = withCurrentSession {
      _.createQuery(queryString) |> setParams(ps: _*) |> fn
    }

    def runQueryWithNamedParams[A](queryString: String, nameParam: NamedParam, namedParams: NamedParam*)(fn: Query => A): A = withCurrentSession {
      _.createQuery(queryString) |> setNamedParams(nameParam, namedParams: _*) |> fn
    }

    def runNamedQuery[A](queryName: String, ps: Any*)(fn: Query => A): A = withCurrentSession {
      _.getNamedQuery(queryName) |> setParams(ps: _*) |> fn
    }

    def runNamedQueryWithNamedParams[A](queryName: String, namedParam: NamedParam, namedParams: NamedParam*)(fn: Query => A): A = withCurrentSession {
      _.getNamedQuery(queryName) |> setNamedParams(namedParam, namedParams: _*) |> fn
    }


    def getByQuery[A](queryString: String, ps: Any*): A =
      runQuery(queryString, ps: _*)(_.uniqueResult().asInstanceOf[A])

    def getByNamedQuery[A](queryName: String, ps: Any*): A =
      runNamedQuery(queryName, ps: _*)(_.uniqueResult().asInstanceOf[A])

    def getByNamedQueryAndNamedParams[A](queryName: String, namedParam: NamedParam, namedParams: NamedParam*): A =
      runNamedQueryWithNamedParams(queryName, namedParam, namedParams: _*)(_.uniqueResult().asInstanceOf[A])


    def listAll[A <: AnyRef : ClassTag](): JList[A]  = withCurrentSession {
      _.createCriteria(scala.reflect.classTag[A].runtimeClass).list().asInstanceOf[JList[A]]
    }

    def listByQuery[A <: AnyRef](queryString: String, ps: Any*): JList[A] =
      runQuery(queryString, ps: _*)(_.list().asInstanceOf[JList[A]])

    def listByQueryAndNamedParams[A <: AnyRef](queryString: String, namedParam: NamedParam, namedParams: NamedParam*): JList[A] =
      runQueryWithNamedParams(queryString, namedParam, namedParams: _*)(_.list().asInstanceOf[JList[A]])

    def listByNamedQuery[A <: AnyRef](queryName: String, ps: Any*): JList[A] =
      runNamedQuery(queryName, ps: _*)(_.list().asInstanceOf[JList[A]])

    def listByNamedQueryAndNamedParams[A <: AnyRef](queryName: String, namedParam: NamedParam, namedParams: NamedParam*): JList[A] =
      runNamedQueryWithNamedParams(queryName, namedParam, namedParams: _*)(_.list().asInstanceOf[JList[A]])

    def listBySqlQuery[A <: AnyRef : HibernateResultTransformer](queryString: String, ps: Any*): JList[A] =
      runSqlQuery(queryString, ps: _*) { query =>
        query.setResultTransformer(implicitly[HibernateResultTransformer[A]].transformer)
        query.list().asInstanceOf[JList[A]]
      }

    def bulkUpdate(queryString: String, ps: Any*): Int =
      runQuery(queryString, ps: _*)(_.executeUpdate())

    def bulkUpdateByNamedParams(queryString: String, namedParam: NamedParam, namedParams: NamedParam*): Int =
      runQueryWithNamedParams(queryString, namedParam, namedParams: _*)(_.executeUpdate())

    def bulkUpdateByNamedQuery(queryString: String, ps: Any*): Int =
      runNamedQuery(queryString, ps: _*)(_.executeUpdate())

    def bulkUpdateByNamedQueryAndNamedParams(queryString: String, namedParam: NamedParam, namedParams: NamedParam*): Int =
      runNamedQueryWithNamedParams(queryString, namedParam, namedParams: _*)(_.executeUpdate())

    def bulkUpdateBySqlQuery(queryString: String, ps: Any*): Int =
      runSqlQuery(queryString, ps: _*)(_.executeUpdate())

    def get[A: ClassTag](id: java.io.Serializable): A = withCurrentSession {
      _.get(scala.reflect.classTag[A].runtimeClass, id).asInstanceOf[A]
    }

    def save[A <: AnyRef](obj: A): A = withCurrentSession { session =>
      session.save(obj)
      obj
    }

    def saveOrUpdate[A <: AnyRef](obj: A): A = withCurrentSession { session =>
      session.saveOrUpdate(obj)
      obj
    }

    def delete[A <: AnyRef](obj: A): Unit = withCurrentSession { _.delete(obj) }

    def merge[A <: AnyRef](obj: A): A = withCurrentSession { _.merge(obj).asInstanceOf[A] }
  }
}


object HibernateSupport {

  import org.hibernate.transform.ResultTransformer

  abstract class HibernateResultTransformer[+A <: AnyRef : ClassTag] {
    def transformer: ResultTransformer

    override def toString = "HibernateResultTransformer[%s]".format(scala.reflect.classTag[A].runtimeClass)
  }


  trait IdentityResultTransformer { this: ResultTransformer =>
    def transformList(collection: JList[_]): JList[_] = collection
  }


  class HibernateArrayResultTransformer[E <: AnyRef : ClassTag] extends HibernateResultTransformer[Array[E]] {
    def transformer = new ResultTransformer with IdentityResultTransformer {
      def transformTuple(tuple: Array[AnyRef], aliases: Array[String]): Array[E] = Array.ofDim[E](tuple.size) |>> { arr =>
        for ((element, i) <- tuple.zipWithIndex) arr(i) = element.asInstanceOf[E]
      }
    }
  }


  class HibernateSingleColumnTransformer[A <: AnyRef : ClassTag] extends HibernateResultTransformer[A] {
    def transformer = new ResultTransformer with IdentityResultTransformer {
      def transformTuple(tuple: Array[AnyRef], aliases: Array[String]): A = tuple(0).asInstanceOf[A]
    }
  }


  object HibernateResultTransformer extends LowLevelHibernateResultTransformerImplicits {
    implicit object defaultResultTransformerFactory extends HibernateResultTransformer[Array[AnyRef]] {
      def transformer = null;
    }
  }


  class LowLevelHibernateResultTransformerImplicits {
    implicit object anyRefSingleColumnTransformer extends HibernateSingleColumnTransformer[AnyRef]
    implicit object stringSingleColumnTransformer extends HibernateSingleColumnTransformer[String]
    implicit object jIntegerSingleColumnTransformer extends HibernateSingleColumnTransformer[JInteger]
    implicit object jDoubleSingleColumnTransformer extends HibernateSingleColumnTransformer[JDouble]
    implicit object jFloatSingleColumnTransformer extends HibernateSingleColumnTransformer[JFloat]
    implicit object jBooleanSingleColumnTransformer extends HibernateSingleColumnTransformer[JBoolean]
    implicit object jCharacterSingleColumnTransformer extends HibernateSingleColumnTransformer[JCharacter]
    implicit object jByteSingleColumnTransformer extends HibernateSingleColumnTransformer[JByte]

    implicit object stringArrayResultTransformer extends HibernateArrayResultTransformer[String]
    implicit object jIntegerArrayResultTransformer extends HibernateArrayResultTransformer[JInteger]
    implicit object jDoubleArrayResultTransformer extends HibernateArrayResultTransformer[JDouble]
    implicit object jFloatArrayResultTransformer extends HibernateArrayResultTransformer[JFloat]
    implicit object jBooleanArrayResultTransformer extends HibernateArrayResultTransformer[JBoolean]
    implicit object jCharacterArrayResultTransformer extends HibernateArrayResultTransformer[JCharacter]
    implicit object jByteArrayResultTransformer extends HibernateArrayResultTransformer[JByte]
  }
}