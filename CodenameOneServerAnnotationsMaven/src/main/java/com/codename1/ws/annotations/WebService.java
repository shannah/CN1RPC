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
 * === Example using POJO Parameters
 * 
 * If you want to pass POJOs in and out of the web service, then the POJO classes
 * need to have the `@Externalizable` annotation.
 * 
 * **AdderWithHistory.java**:
 * [source,java]
 * ----
package com.codename1.demos.simpleadder;

import com.codename1.ws.annotations.WebService;

@WebService
public class AdderWithHistory {
    public static AdderContext addInts(AdderContext context) {
        int result = context.getA() + context.getB();
        
        context.setResult(result);
        context.log(request.getA()+" + "+request.getB()+" = "+result);
        return context;
    }
}
 * ----
 * 
 * **AdderContext.java**
 * 
 * [source,java]
 * ----
 package com.codename1.demos.simpleadder;
 import com.codename1.ws.annotations.Externalizable;
 import java.util.List;
 import java.util.ArrayList;
 
 @Externalizable
 public class AdderContext {
     int a, b, result;
     List<String> log = new ArrayList<String>();
    
     public void log(String str) {
         log.add(str);
     }
     
     // setters and getters...
 }
 * ----
 * 
 * **Client Code:**
 * 
 * [source,java]
 * ----
 * AdderWithHistoryProxy proxy = new AdderWithHistory("http://localhost:8080/adder-client");
 * AdderContext context = proxy.create(AdderContext.class);
 * context.setA(1);
 * context.setB(2);
 * context = proxy.addInts(context);
 * context.getResult(); // 3
 * context.getLog().get(0); // 1 + 2 = 3
 * 
 * context.setA(4);
 * context = proxy.addInts(context);
 * context.getResult(); // 6
 * context.getLog().get(0); // 1 + 2 = 3
 * context.getLog().get(1); // 4 + 2 = 6
 * 
 * ----
 * 
 * 
 * === Creating Instances of `@Externalizable` types
 * 
 * It is important to note that `@Externalizable` types cannot be simply instantiated and passed
 * to the web service since the class itself doesn't implement serialization.  The annotation results
 * in a subclass being generated that *does* implement serialization, and it is instances of this
 * subclass that are supported as input and output in the WebService.
 * 
 * You could instantiate those subclasses directly, as their naming conventions are pretty basic (just
 * add "Impl" as a suffix to the class name, but the preferred way is to use the `ExternalizableFactory`
 * class that is generated inside each package that contains `@Externalizable` classes.  E.g.
 * 
 * [source,java]
 * ----
 * ExternalizableFactory f = new ExternalizableFactory();
 * MyClass o = f.create(MyClass.class);
 * // Where MyClass has the @Externalizable annotation
 * ----
 * 
 * On the client side, this is simplified further by the `Proxy` class including a wrapper `create()` method
 * that delegates to the appropriate `ExternalizableFactory`.  However since the ExternalizableFactory is 
 * installed on both the client and server, you could use the ExternalizableFactory in both places.
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
     * 
     * **Example**
     * Suppose you have the following directory structure:
     * 
     * ----
     * /path/to/myapp/
     *     server/
     *         src/
     *             java/
     *                 com/
     *                     example/
     *                         MyService.java
     *     client/
     *         src/
     *             com/
     *                 example/
     *                     MyClient.java
     * ----
     * 
     * And the `MyService` class includes the following annotation:
     * 
     * ----
     * @WebService(exports="/path/to/myapp/client")
     * ----
     * 
     * This would result in client files being copied into the client project.  Specifically (for example)
     * the `MyServiceProxy.java` file would be installed at `/path/to/myapp/client/src/com/example/MyServiceProxy.java`.
     * 
     * The same can be achieved through a relative path.  It is important to note that relative paths
     * are always relative to the *source root* directory that contains the class marked with the `@WebService`
     * annotation.  E.g.
     * 
     * ----
     * @WebService(exports="../../../client")
     * ----
     * 
     * Would yield the same result as the absolute path above, because the client project
     * is located 3 levels up from the source root directory of the server project (which is `/path/to/myapp/server/src/java`).
     * Notice that the source root is in `src/java` and not just `src` in this case.  The source root is considered
     * the directory that contains the default package in the class path.
     */
    public String[] exports() default {};
}
