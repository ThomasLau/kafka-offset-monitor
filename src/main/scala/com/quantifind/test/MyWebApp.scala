package com.quantifind.test

import com.quantifind.sumac.FieldArgs
import com.quantifind.sumac.validation.Required
import unfiltered.util.Port

import scala.concurrent.duration.FiniteDuration

object MyWebApp {
  trait Arguments extends FieldArgs {
    var port = Port.any
    var context = "bizkkmonitor"
  }
}

class MyOWArgs extends MyWebApp.Arguments {
  var pluginsArgs : String = _
}
