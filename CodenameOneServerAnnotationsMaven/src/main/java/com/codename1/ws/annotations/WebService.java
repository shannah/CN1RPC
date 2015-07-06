/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.ws.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 * @author shannah
 */
@Target(ElementType.TYPE)
public @interface WebService {
    public String name() default "";
    public String urlPattern() default "";
}
