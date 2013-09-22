package com.imcode.imcms.dao.hibernate

import org.hibernate._
import scala.reflect.ClassTag

import com.imcode._

trait HibernateSupport {

  @scala.reflect.BeanProperty
  var sessionFactory: SessionFactory = _

  /* exposes method for java API */
  def flush(): Unit = hibernate.flush()

  object hibernate {
    type NamedParam = (String, Any)
    type PositionalParam = (Int, Any)

    def withCurrentSession[A](fn: Session => A): A = fn(sessionFactory.getCurrentSession)

    def flush(): Unit = withCurrentSession { _.flush() }

    private def setParams[Q <: Query](ps: Any*)(query: Q): Q = query |>> { _ =>
      for ((param, position) <- ps.zipWithIndex) query.setParameter(position, param.asInstanceOf[AnyRef])
    }

    private def setNamedParams[Q <: Query](namedParam: NamedParam, namedParams: NamedParam*)(query: Q): Q = query |>> { _ =>
      for ((name, value) <- namedParam +: namedParams) query.setParameter(name, value.asInstanceOf[AnyRef])
    }

    // See positional params difference in JPQL and HQL
    // http://docs.jboss.org/hibernate/orm/4.2/devguide/en-US/html/ch11.html#d5e2915
    private def setPositionalParams[Q <: Query](params: PositionalParam*)(query: Q): Q = query |>> { _ =>
      for ((position, value) <- params) query.setParameter(position.toString, value.asInstanceOf[AnyRef])
    }

    def runSqlQuery[A](queryString: String, ps: Any*)(fn: SQLQuery => A): A = withCurrentSession {
      _.createSQLQuery(queryString) |> setParams(ps: _*) |> fn
    }

//    def runQuery[A](queryString: String, ps: Any*)(fn: Query => A): A = withCurrentSession {
//      _.createQuery(queryString) |> setParams(ps: _*) |> fn
//    }

    def runQuery[A](queryString: String, ps: PositionalParam*)(fn: Query => A): A = withCurrentSession {
      _.createQuery(queryString) |> setPositionalParams(ps: _*) |> fn
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


    def getByQuery[A](queryString: String, params: PositionalParam*): A =
      runQuery(queryString, params: _*)(_.uniqueResult().asInstanceOf[A])

    def getByNamedQuery[A](queryName: String, ps: Any*): A =
      runNamedQuery(queryName, ps: _*)(_.uniqueResult().asInstanceOf[A])

    def getByNamedQueryAndNamedParams[A](queryName: String, namedParam: NamedParam, namedParams: NamedParam*): A =
      runNamedQueryWithNamedParams(queryName, namedParam, namedParams: _*)(_.uniqueResult().asInstanceOf[A])


    def listAll[A <: AnyRef : ClassTag](): JList[A]  = withCurrentSession {
      _.createCriteria(scala.reflect.classTag[A].runtimeClass).list().asInstanceOf[JList[A]]
    }

    def listByQuery[A <: AnyRef](queryString: String, ps: PositionalParam*): JList[A] =
      runQuery(queryString, ps: _*)(_.list().asInstanceOf[JList[A]])

    def listByQueryAndNamedParams[A <: AnyRef](queryString: String, namedParam: NamedParam, namedParams: NamedParam*): JList[A] =
      runQueryWithNamedParams(queryString, namedParam, namedParams: _*)(_.list().asInstanceOf[JList[A]])

    def listByNamedQuery[A <: AnyRef](queryName: String, ps: Any*): JList[A] =
      runNamedQuery(queryName, ps: _*)(_.list().asInstanceOf[JList[A]])

    def listByNamedQueryAndNamedParams[A <: AnyRef](queryName: String, namedParam: NamedParam, namedParams: NamedParam*): JList[A] =
      runNamedQueryWithNamedParams(queryName, namedParam, namedParams: _*)(_.list().asInstanceOf[JList[A]])

    def listBySqlQuery[A <: AnyRef : ResultTransformerProvider](queryString: String, ps: Any*): JList[A] =
      runSqlQuery(queryString, ps: _*) { query =>
        query.setResultTransformer(implicitly[ResultTransformerProvider[A]].resultTransformer)
        query.list().asInstanceOf[JList[A]]
      }

    def bulkUpdate(queryString: String, ps: PositionalParam*): Int =
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

    def mergeAndSaveOrUpdate[A <: AnyRef](obj: A): A = withCurrentSession { session =>
      session.merge(obj).asInstanceOf[A] |>> session.saveOrUpdate
    }

    def delete[A <: AnyRef](obj: A): Unit = withCurrentSession { _.delete(obj) }

    def merge[A <: AnyRef](obj: A): A = withCurrentSession { _.merge(obj).asInstanceOf[A] }
  }
}