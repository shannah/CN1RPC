// DO NOT MODIFY THIS FILE.  IT HAS BEEN AUTOMATICALLY GENERATED
// CHANGES MAY BE OVERWRITTEN WITHOUT NOTICE
package com.codename1.testws;

import com.codename1.io.WebServiceProxyCall;
import com.codename1.io.WebServiceProxyCall.WSDefinition;
import java.io.IOException;
import java.lang.Class;
import java.lang.Integer;
import java.lang.String;

public class TestWebServerProxy {
  private static boolean initialized;

  private final String url;

  private final WebServiceProxyCall.WSDefinition def_add;

  private final WebServiceProxyCall.WSDefinition def_getExternalizable;

  private final WebServiceProxyCall.WSDefinition def_doSomethingWithNoOutput;

  private final WebServiceProxyCall.WSDefinition def_noOutputWithExternalizableInput;

  private final WebServiceProxyCall.WSDefinition def_intOutputWithExternalizableInput;

  private final WebServiceProxyCall.WSDefinition def_aVersionedMethod;

  private final WebServiceProxyCall.WSDefinition def_addArray;

  private final WebServiceProxyCall.WSDefinition def_countChars;

  private final WebServiceProxyCall.WSDefinition def_getIntArray;

  private final WebServiceProxyCall.WSDefinition def_saySomething;

  public TestWebServerProxy(String url) {
    init();
    if (url.charAt(url.length()-1) != '/') {
      url = url + "/";
    }
    String urlPattern = "/cn1rpc";
    if (urlPattern.charAt(0) == '/') {
      urlPattern = urlPattern.substring(1);
    }
    this.url=url+urlPattern;
    def_add = WebServiceProxyCall.defineWebService(this.url, "add", WebServiceProxyCall.TYPE_INT, WebServiceProxyCall.TYPE_INT, WebServiceProxyCall.TYPE_INT);
    def_getExternalizable = WebServiceProxyCall.defineWebService(this.url, "getExternalizable", WebServiceProxyCall.TYPE_EXTERNALIABLE);
    def_doSomethingWithNoOutput = WebServiceProxyCall.defineWebService(this.url, "doSomethingWithNoOutput", WebServiceProxyCall.TYPE_VOID);
    def_noOutputWithExternalizableInput = WebServiceProxyCall.defineWebService(this.url, "noOutputWithExternalizableInput", WebServiceProxyCall.TYPE_VOID, WebServiceProxyCall.TYPE_EXTERNALIABLE);
    def_intOutputWithExternalizableInput = WebServiceProxyCall.defineWebService(this.url, "intOutputWithExternalizableInput", WebServiceProxyCall.TYPE_INT, WebServiceProxyCall.TYPE_EXTERNALIABLE);
    def_aVersionedMethod = WebServiceProxyCall.defineWebService(this.url, "aVersionedMethod", WebServiceProxyCall.TYPE_INT, WebServiceProxyCall.TYPE_EXTERNALIABLE, WebServiceProxyCall.TYPE_INT);
    def_addArray = WebServiceProxyCall.defineWebService(this.url, "addArray", WebServiceProxyCall.TYPE_INT, WebServiceProxyCall.TYPE_INT_ARRAY);
    def_countChars = WebServiceProxyCall.defineWebService(this.url, "countChars", WebServiceProxyCall.TYPE_INT, WebServiceProxyCall.TYPE_STRING_ARRAY);
    def_getIntArray = WebServiceProxyCall.defineWebService(this.url, "getIntArray", WebServiceProxyCall.TYPE_INT_ARRAY);
    def_saySomething = WebServiceProxyCall.defineWebService(this.url, "saySomething", WebServiceProxyCall.TYPE_STRING, WebServiceProxyCall.TYPE_STRING);
  }

  private static void init() {
    if (!initialized) {
      initialized = true;
      new ExternalizableFactory().init();
    }
  }

  public <T> T create(Class<T> cls) {
    try {
      return new ExternalizableFactory().create(cls);
    }
    catch (Throwable t) {
    }
    throw new RuntimeException("No matching implementation found for class.");
  }

  public int add(int a, int b) throws IOException {
    return (Integer)WebServiceProxyCall.invokeWebserviceSync(def_add, a, b);
  }

  public TestExternalizable getExternalizable() throws IOException {
    return (TestExternalizable)WebServiceProxyCall.invokeWebserviceSync(def_getExternalizable);
  }

  public void doSomethingWithNoOutput() throws IOException {
    WebServiceProxyCall.invokeWebserviceSync(def_doSomethingWithNoOutput);
  }

  public void noOutputWithExternalizableInput(TestExternalizable t) throws IOException {
    WebServiceProxyCall.invokeWebserviceSync(def_noOutputWithExternalizableInput, t);
  }

  public int intOutputWithExternalizableInput(TestExternalizable t) throws IOException {
    return (Integer)WebServiceProxyCall.invokeWebserviceSync(def_intOutputWithExternalizableInput, t);
  }

  public int aVersionedMethod(TestExternalizable t) throws IOException {
    return (Integer)WebServiceProxyCall.invokeWebserviceSync(def_aVersionedMethod, t, 1);
  }

  public int addArray(int[] args) throws IOException {
    return (Integer)WebServiceProxyCall.invokeWebserviceSync(def_addArray, args);
  }

  public int countChars(String[] strs) throws IOException {
    return (Integer)WebServiceProxyCall.invokeWebserviceSync(def_countChars, strs);
  }

  public int[] getIntArray() throws IOException {
    return (int[])WebServiceProxyCall.invokeWebserviceSync(def_getIntArray);
  }

  public String saySomething(String message) throws IOException {
    return (String)WebServiceProxyCall.invokeWebserviceSync(def_saySomething, message);
  }
}
