/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.testws;

import com.codename1.ws.annotations.CodenameOne;
import com.codename1.ws.annotations.WebService;

/**
 *
 * @author shannah
 */
@WebService(name="CN1Servier", urlPattern="/cn1rpc")
@CodenameOne(projectPath="../../../TestClientApp")
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
}
