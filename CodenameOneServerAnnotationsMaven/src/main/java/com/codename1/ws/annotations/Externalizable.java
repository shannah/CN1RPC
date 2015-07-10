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

/**
 * Causes a subclass to be generated with the name `${classname}Impl` that implements
 * `com.codename1.io.Externalizable` and is able to serialize all non-private, non-transient,
 * non-final members.  Also causes a factory class named `ExternalizableFactory` to be generated
 * in the same package which can be used to instantiate new instances of the `Externalizable`
 * subclass.
 * 
 * This annotation is helpful in conjunction with the `@WebService` annotation because parameters
 * and return values of web services must either be primitive types, strings, arrays of primitive types,
 * boxed types, arrays of strings --- or POJOs implementing the `Externalizable` interface.  Using
 * this annotation saves you from writing all of the serialization boiler-plate code that is necessary
 * to read and write the object from a stream.
 * 
 * === Simple Example
 * 
 * [source,java]
 * ----
 * @Externalizable
 * public class Person {
 *     String name;
 *     int age;
 * 
 *     // setters and getters here
 * }
 * ----
 * 
 * This class can now be instantiated by the `ExternalizableFactory` as follows:
 * 
 * [source,java]
 * ----
 * ExternalizableFactory f = new ExternalizableFactory();
 * Person p = f.create(Person.class);
 * p.setName("Steve");
 * p.setAge(11);
 * ----
 * 
 * The code above works on both the client and server side, since all `@Externalizable` classes
 * used as a parameter or return type in a web service is automatically installed on the client.
 * As is their corresponding `ExternalizableFactory` classes.  This person object can now be easily passed back
 * and forth between client and server using a web service and associated proxy.
 * 
 * === Allowed Data Types
 * 
 * Fields in an `@Externalizable` class can be:
 * 
 * 1. Primitive types (`int`, `float`, `byte`, etc...)
 * 2. Strings
 * 3. Primitive Arrays (`int[]`, etc...)
 * 4. Boxed types (`Integer`, etc...)
 * 5. `Map`s or `List`s
 * 6. `Date`s
 * 7. Other `@Externalizable` types
 * 8. Objects implementing `com.codenameone.io.Externalizable` (but make sure that they are registered
 * with `Util.register()` on both client and server before trying to use read or write them to/from streams.
 * 9. Arrays of `@Externalizable` or `Externalizable` objects.
 * 10. Other types supported by `com.codename1.io.Util.readObject()/writeObject()` that I may have missed here.
 * 
 * @author shannah
 */
public @interface Externalizable {
    
    int version() default 1;
    
}
