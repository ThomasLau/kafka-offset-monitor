package unfiltered.jetty

import unfiltered.util.{HttpPortBindingShim, PlanServer, Port, RunnableServer}
import org.eclipse.jetty.server.{Connector, Handler, Server => JettyServer}
import org.eclipse.jetty.server.handler.{ContextHandlerCollection, ResourceHandler}
import org.eclipse.jetty.servlet.{FilterHolder, FilterMapping, ServletContextHandler, ServletHolder}
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.util.resource.Resource
import java.util.EnumSet
import java.util.concurrent.atomic.AtomicInteger

import javax.servlet.{DispatcherType, Filter}
import unfiltered.jettty.Utils

object MyJetty {
  def apply(port: Int): MyJetty = MyJetty(port, "0.0.0.0", "/")
  def apply(port: Int, ctxBase:String): MyJetty = MyJetty(port, "0.0.0.0", ctxBase)
  def local(port: Int) = MyJetty(port, "127.0.0.1", "/")
  def anylocal = local(Port.any)
}

case class MyJetty(port: Int, host: String, ctxbase:String) extends NewJettyBase {
  type ServerBuilder = MyJetty
  val url = "http://%s:%d/" format (host, port)
  val conn = new SocketConnector()
  conn.setPort(port)
  conn.setHost(host)
  underlying.addConnector(conn)
  def portBindings = HttpPortBindingShim(host, port) :: Nil

  lazy val ctxHandler:ServletContextHandler = context2(ctxbase)

  //def context2(path: String):this.type = {
  def context2(path: String):ServletContextHandler = {
    val ctx = new ServletContextHandler(handlers, path, false, false)

    ctx.setSecurityHandler(Utils.newBasicAuth("realm.properties", "/"))


    val holder = new ServletHolder(classOf[org.eclipse.jetty.servlet.DefaultServlet])
    holder.setName("Servlet %s" format counter.incrementAndGet)
    ctx.addServlet(holder, "/")
    handlers.addHandler(ctx)
    ctx
    // ctxHandler=ctx
    // this
  }
  override  def getCtxHandler():ServletContextHandler={
    ctxHandler
  }
}

trait NewJettyBase
    extends PlanServer[Filter]
    with RunnableServer { self =>
  type ServerBuilder >: self.type <: NewJettyBase

  val underlying = new JettyServer()
  val handlers = new ContextHandlerCollection
  val counter = new AtomicInteger
  val url: String

  // lazy val ctxHandler:ServletContextHandler=context2("/km")
  // var ctxHandler:ServletContextHandler

  def makePlan(plan: => Filter) = filter(plan)

  underlying.setHandler(handlers)

  def  contextHandler(path: String) = {
    val ctx = new ServletContextHandler(handlers, path, false, false)
    val holder = new ServletHolder(classOf[org.eclipse.jetty.servlet.DefaultServlet])
    holder.setName("Servlet %s" format counter.incrementAndGet)
    ctx.addServlet(holder, "/")
    handlers.addHandler(ctx)
    ctx
  }
//  def context2(path: String) = {
//        val ctx = new ServletContextHandler(handlers, path, false, false)
//        val holder = new ServletHolder(classOf[org.eclipse.jetty.servlet.DefaultServlet])
//        holder.setName("Servlet %s" format counter.incrementAndGet)
//        ctx.addServlet(holder, "/")
//        handlers.addHandler(ctx)
//        ctx
//  }

//  def context2(path: String) = {
//    val ctx = new ServletContextHandler(handlers, path, false, false)
//    val holder = new ServletHolder(classOf[org.eclipse.jetty.servlet.DefaultServlet])
//    holder.setName("Servlet %s" format counter.incrementAndGet)
//    ctx.addServlet(holder, "/")
//    handlers.addHandler(ctx)
//    ctxHandler=ctx
//    NewJettyBase.this
//  }

  def filter(filt: Filter): this.type = {
    val holder = new FilterHolder(filt)
    holder.setName("Filter %s" format counter.incrementAndGet)
    getCtxHandler().addFilter(holder, "/*", EnumSet.of(DispatcherType.REQUEST))
    this
  }

  def resources(path: java.net.URL): this.type = {
    getCtxHandler().setBaseResource(Resource.newResource(path))
    this
  }
  def getCtxHandler():ServletContextHandler

//  def context(path: String)(block: ContextBuilder => Unit) = {
//    block(new ContextBuilder {
//      val current = contextHandler(path)
//      val counter = NewJettyBase.this.counter
//    })
//    NewJettyBase.this
//  }

  // def getCtx():String
  // lazy val current  = contextHandler(getCtx())

  def start() = {
    underlying.setStopAtShutdown(true)
    underlying.start()
    NewJettyBase.this
  }
  def stop() = {
    underlying.stop()
    NewJettyBase.this
  }
  def destroy() = {
    underlying.destroy()
    this
  }
  def join() = {
    underlying.join()
    this
  }
}
