/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.testws;

import com.codename1.ws.annotations.Externalizable;
/**
 *
 * @author shannah
 */
public class InternalExternalizables {
    
    @Externalizable
    public static class Nested1 {
        String stringVal;
    }
    
    @Externalizable
    public static class Nested2 {
        String stringVal2;
    }
}
