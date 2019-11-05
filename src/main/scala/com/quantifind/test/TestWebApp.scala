package com.quantifind.test

import com.quantifind.kafka.offsetapp.OffsetGetterWeb.{TimeSerializer}
import com.quantifind.sumac.{ArgMain, FieldArgs}
import kafka.utils.Logging
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle
import unfiltered.filter.Plan

import scala.concurrent.duration.FiniteDuration


trait TestWebApp[T <: MyWebApp.Arguments] extends ArgMain[T] {

  def htmlRoot: String

  def setup(args: T): unfiltered.filter.Plan

  def afterStart() {}

  def afterStop() {}

  override def main(parsed: T) {
//    val root = getClass.getResource(htmlRoot)
//    println("serving resources from: " + root)
//    unfiltered.jetty.MyHttp(parsed.port)
//      .ctxBase("km")
//      // .resources("kf", root)
//      .resources(root)
//      //.contextBase(parsed.context)
//      .filter(setup(parsed))
//      /*.context(parsed.context){
//          //_.filter(PlanFilter(new SimpleUser))
//          _.filter(setup(parsed))
//        }*/
//      .run(_ => afterStart(), _ => afterStop())
    val root = getClass.getResource(htmlRoot)
    println("serving resources from: " + root)
    unfiltered.jetty.MyJetty(parsed.port,"/kk")
      //.ctxBase("km")
      .resources(root)
      .filter(setup(parsed))
      /*.context(parsed.context){
          //_.filter(PlanFilter(new SimpleUser))
          _.filter(setup(parsed))
        }*/
      .run(_ => afterStart(), _ => afterStop())
  }

}
object TWeb extends TestWebApp[MyOWArgs] with Logging {
  def htmlRoot: String = "/offsetapp"
  override def setup(args: MyOWArgs): Plan = new Plan {
    implicit val formats = Serialization.formats(NoTypeHints) + new TimeSerializer
    var prep = args.context
    def intent: Plan.Intent = {
      case GET(Path(Seg("group" :: Nil))) => {
        JsonContent ~> ResponseString(write("rrr"))
      }
      case GET(Path(Seg(prep::"group" ::list:: Nil))) => {
        var bbb = Path
        var sss = Seg
        println(sss)
        JsonContent ~> ResponseString(write(list))
      }
    }
  }
}

trait Users {
  def auth(u: String, p: String): Boolean
}

class SimpleUser extends Users{
  def auth(u: String, p: String)= u.equals("admin") && p.equals("hello")
}

case class MyAuth(users: Users) {
  def apply[A,B](intent: Cycle.Intent[A,B]) =
    Cycle.Intent[A,B] {
      case req@BasicAuth(user, pass) if(users.auth(user, pass)) =>
        Cycle.Intent.complete(intent)(req)
      case _ =>
        Unauthorized ~> WWWAuthenticate("""Basic realm="/"""")
    }
}

case class PlanFilter(users: Users) extends
  unfiltered.filter.Plan {
  def intent = MyAuth(users) {
    case _ => ResponseString("Shhhh!")
  }
}


