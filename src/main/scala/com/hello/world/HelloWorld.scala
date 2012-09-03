package com.hello.world

import javax.servlet.http._

class HelloWorld extends HttpServlet {
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) =
    resp.getWriter().print("Hello, world. I am going to look for this file: dfdfd.js")
}