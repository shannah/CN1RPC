/*
Copyright (c) 2015 Steve Hannah

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.codename1.ws.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * The `@WebService` annotation marks a class as a Web Service.  In practical terms,
 * marking a class with the `@WebService` annotation will result in the following
 * things happening when it is compiled:
 * 
 * 1. A servlet will be generated that serves all `public static` methods over HTTP.
 * 2. A client proxy class will be generated that provides a Java API to iteract with
 * the servlet - and effectively call `public static` methods of this class via 
 * RPC (remote procedure call).
 * 
 * 
 * == Simple Example
 * 
 * Consider the following web service that adds two `int`s and returns their sum as
 * an `int`.
 * 
 * [source,java]
 * ----
package com.codename1.demos.simpleadder;

import com.codename1.ws.annotations.WebService;

@WebService
public class SimpleAdder {
    public static int addInts(int a, int b) {
        return a+b;
    }
}

 * ----
 * 
 * By simply adding the `@WebService` annotation, we will be able to access the `addInts` method via RPC (remote procedure call)
 * on a Codename One client as follows:
 * 
 * [source,java]
 * ----
 * SimpleAdderProxy adder = new SimpleAdderProxy("http://localhost:8080/myapp");
 * int result = adder.addInts(1, 3); // should return 4
 * ----
 * 
 * You are probably wondering where the `SimpleAdderProxy` class came from.  This class and
 * a corresponding servlet are generated at compile time by the `@WebService` annotation processor.  
 * The optional `exports` attribute can result in the proxy class and necessary dependencies to be
 * automatically added to the client project whenever it is changed.
 * 
 === The Generated Code
 * 
 * So what do these generated proxy and servlet classes look like?
 * 
 * **The Servlet Class**:
 * 
 * [source,java]
 * ----
 * // DO NOT MODIFY THIS FILE.  IT HAS BEEN AUTOMATICALLY GENERATED
// CHANGES MAY BE OVERWRITTEN WITHOUT NOTICE
package com.codename1.demos.simpleadder;

import com.codename1.proxy.server.ProxyServerHelper;
import com.codename1.proxy.server.ProxyServerHelper.WSDefinition;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Override;
import java.lang.String;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
    name = "SimpleAdderServlet",
    urlPatterns = {"/SimpleAdderServlet"}
)
public class SimpleAdderServlet extends HttpServlet {
  private static final ProxyServerHelper.WSDefinition def_addInts = ProxyServerHelper.createServiceDefinition("addInts", ProxyServerHelper.TYPE_INT, ProxyServerHelper.TYPE_INT, ProxyServerHelper.TYPE_INT);

  public void init() throws ServletException {
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    DataInputStream di = new DataInputStream(request.getInputStream());
    String methodName = di.readUTF();
    if (methodName.equals("addInts")) {
      Object[] args = ProxyServerHelper.readMethodArguments(di, def_addInts);
      ProxyServerHelper.writeResponse(response, def_addInts, (int)SimpleAdder.addInts((int)args[0], (int)args[1]));
      return;
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    try {
      out.println("<!DOCTYPE html>");
      out.println("<html>");
      out.println("<head>");
      out.println("<title>Error</title>");
      out.println("</head>");
      out.println("<body>");
      out.println("<h1>Webservice access only!</h1>");
      out.println("</body>");
      out.println("</html>");
    }
    finally {
      out.close();
    }
  }
}

----
 
* **The Proxy**:

[source,java]
----
// DO NOT MODIFY THIS FILE.  IT HAS BEEN AUTOMATICALLY GENERATED
// CHANGES MAY BE OVERWRITTEN WITHOUT NOTICE
package com.codename1.demos.simpleadder;

import com.codename1.io.WebServiceProxyCall;
import com.codename1.io.WebServiceProxyCall.WSDefinition;
import java.io.IOException;
import java.lang.Class;
import java.lang.Integer;
import java.lang.String;

public class SimpleAdderProxy {
  private static boolean initialized;

  private final String url;

  private final WebServiceProxyCall.WSDefinition def_addInts;

  public SimpleAdderProxy(String url) {
    init();
    if (url.charAt(url.length()-1) != '/') {
      url = url + "/";
    }
    String urlPattern = "/SimpleAdderServlet";
    if (urlPattern.charAt(0) == '/') {
      urlPattern = urlPattern.substring(1);
    }
    this.url=url+urlPattern;
    def_addInts = WebServiceProxyCall.defineWebService(this.url, "addInts", WebServiceProxyCall.TYPE_INT, WebServiceProxyCall.TYPE_INT, WebServiceProxyCall.TYPE_INT);
  }

  private static void init() {
    if (!initialized) {
      initialized = true;
    }
  }

  public <T> T create(Class<T> cls) {
    throw new RuntimeException("No matching implementation found for class.");
  }

  public int addInts(int arg0, int arg1) throws IOException {
    return (Integer)WebServiceProxyCall.invokeWebserviceSync(def_addInts, arg0, arg1);
  }
}
----

 === Servlet Class Settings
 * 
 * The `SimpleAdder` service shown above demostrates a number of characteristics of the 
 * generated servlet class.
 * 
 * 1. The class name is always the name of the *service* class with "Servlet" appended
 * to it.  E.g. `SimpleAdder` becomes `SimpleAdderServlet`.
 * 2. The `urlPattern` for the servlet is just the simple name of the servlet class by default.  
 * For example, the `SimpleAdderServlet` generated in our example would be available at
 * http://hostname:port/mywebapp/SimpleAdderServlet,  This can be overridden by supplying
 * the `urlPattern` attribute.
 * 3. The servlet implements both GET and POST, but the GET handler is just a dummy placeholder that
 * tells the client that this servlet is only for webservices.
 * 
 * This servlet is made specifically to be "talked" to by the proxy class that is generated.  It
 * uses Codename One "Exernalization" to read and write the input and output.
 * 
 * 
 * === Proxy Class Settings
 * 
 * The generated proxy class includes a corresponding method for each public static
 * method in the originating service class.  You'll notice a couple of differences
 * in the API between the proxy and the actual service:
 * 
 * 1. The proxy class implements methods as member methods rather than static.  This allows
 * the client app to potentially create multiple proxies in the same app and point them to
 * different servers.
 * 2. The methods in the proxy class throw `IOException`.  This is in case there is a communication
 * error during the remote procedure call.
 * 
 * The "proxy" class is output to a directory named "cn-client-generated-sources" in your build
 * directory (a sibling of the "generated-sources" directory.  You can manually copy this directory into
 * your client app, but a better way would be to either use the `exports` attribute of the `@WebService`
 * annotation, or use your build tool to automatically copy the sources during build.
 * 
 * 
 * === Allowed Parameters and Return Types
 * 
 * Since all inputs and outputs for a web service need to be serialized to send over the network,
 * you can't just put *any* object type as input or output.  Supported input and return types 
 * include:
 * 
 * 1. Primitive types. E.g. `boolean`, `byte`, `int`, `float`, etc...
 * 2. Boxed primitive types.  E.g. `Boolean`, `Byte`, `Integer`, etc...
 * 3. Strings
 * 4. Arrays of primitive types.
 * 5. Objects implementing the `com.codename1.io.Externalizable` interface.
 * 6. Objects of classes that include the `@Externalizable` annotation (This is because that annotation
 * automatically results in a subclass that implements `Externalizable`.
 * 
 * === Creating Instances of `@Externalizable` types
 * 
 * The generated proxy includes a factory method called `create()` that allows you
 * to create new instances of any of the `@Externalizable` classes that are used
 * as either an input or output of the web service.  This is helpful since you can't just
 * call `new SomeClass()`.
 * 
 * @author shannah
 */
@Target(ElementType.TYPE)
public @interface WebService {
    /**
     * The name of the web service and resulting servlet.  Defaults
     * to the simple name of the @WebService class.
     * @return 
     */
    public String name() default "";
    
    /**
     * The urlPattern of the resulting servlet.  Defaults to the 
     * simple name of the servlet class.
     * 
     * **Example**:
     * 
     * [source,java]
     * ----
     * @WebService(urlPattern="/myservice")
     * public class SimpleAdder {
     *     ...
     * }
     * ----
     * 
     * This example would result in a servlet that responds to "/path/to/app/myservice"
     * 
     * @return 
     */
    public String urlPattern() default "";
    
    /**
     * Paths to the client projects that need to consume this web service.
     * This will cause the client files to be automatically copied into
     * the "src" directory of the client project on build.
     * 
     * Paths can be absolute or relative (from the "SRC_ROOT" directory).
     * @return 
     */
    public String[] exports() default {};
}
