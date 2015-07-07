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
