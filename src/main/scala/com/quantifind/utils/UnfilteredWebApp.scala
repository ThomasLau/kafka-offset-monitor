package com.quantifind.utils

import unfiltered.util.Port

import com.quantifind.sumac.{ArgMain, FieldArgs}
import com.quantifind.utils.UnfilteredWebApp.Arguments

import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle


/**
 * build up a little web app that serves static files from the resource directory
 * and other stuff from the provided plan
 * User: pierre
 * Date: 10/3/13
 */
trait UnfilteredWebApp[T <: Arguments] extends ArgMain[T] {

  def htmlRoot: String

  def setup(args: T): unfiltered.filter.Plan

  def afterStart() {}

  def afterStop() {}

  override def main(parsed: T) {
    val root = getClass.getResource(htmlRoot)
    var baseroot = parsed.context
    if(null == baseroot){
      baseroot= "/bizkm"
    }
    if(!baseroot.startsWith("/")){
      baseroot="/"+baseroot
    }
    println("serving resources from: " + root + "-" + baseroot)
    unfiltered.jetty.MyJetty(parsed.port,baseroot)
      .resources(root) //whatever is not matched by our filter will be served from the resources folder (html, css, ...)
      //.contextBase(parsed.context)
      .filter(setup(parsed))
      /*.context(parsed.context){
          _.filter(PlanFilter(new SimpleUser))
        }*/

      .run(_ => afterStart(), _ => afterStop())
  }

}

object UnfilteredWebApp {

  trait Arguments extends FieldArgs {
    var port = Port.any
    var context = "bizkkmonitor"
  }

}

// object myUser("foo","bar")

trait Users {
  def auth(u: String, p: String): Boolean
}

class SimpleUser extends Users{
  def auth(u: String, p: String)= u.equals("admin") && p.equals("hello")
}

case class Auth(users: Users) {
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
  def intent = Auth(users) {
    case _ => ResponseString("Shhhh!")
  }
}


