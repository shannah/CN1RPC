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
@Externalizable(version=3)
public class TestVersionedClass {
    int a;
    int b;
    String name;
    
    class Version1 {
        int a;
        int b;
        
    }
    
    class Version2 {
        int a;
        int b;
        String name;
        float aMistake;
    }
}
