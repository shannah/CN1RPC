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
@Externalizable
public class ParentExternalizable {
    int someParentInt;
    String someParentString;
    TestExternalizable someParentExternalizable;
    int[] someParentIntArray;
            
}
