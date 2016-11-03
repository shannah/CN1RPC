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
package com.codename1.testws;

import com.codename1.ws.WebServiceContext;
import com.codename1.ws.annotations.WebService;

/**
 *
 * @author shannah
 */
@WebService(
        name="CN1Servier", 
        urlPattern="/cn1rpc", 
        exports={"../../../TestClientApp"}
)
public class TestWebServer {
    public static int add(int a, int b) {
        return a+b*2;
    }
    
    public static TestExternalizable getExternalizable() {
        return null;
    }
    
    public static void doSomethingWithNoOutput() {
        
    }
    
    public static void noOutputWithExternalizableInput(TestExternalizable t) {
        
    }
    
    public static int intOutputWithExternalizableInput(TestExternalizable t) {
        return 1;
    }
    
    public static int aVersionedMethod(TestExternalizable t, WebServiceContext context) {
        return 1;
    }
    
    public static int addArray(int[] args) {
        int counter = 0;
        for (int i=0; i<args.length; i++) {
            counter += args[i];
        }
        return counter;
    }
    
    public static int countChars(String[] strs) {
        return strs.length;
    }
    
    public static int[] getIntArray() {
        return new int[]{1,3,5};
    }
    
    public static String saySomething(String message) {
        return "Hello "+message;
    }
}
