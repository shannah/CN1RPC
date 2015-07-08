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

package com.codename1.ws.annotations.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 *
 * @author shannah
 */
public class FactoryClass {
    private String packageName;
    private String className = "ExternalizableFactory";
    private Messager messager;
    private Set<ExternalizableClass> externalizables = new HashSet<ExternalizableClass>();
    
    public FactoryClass(String packageName, Collection<ExternalizableClass> externalizables, Messager messager) {
        this.messager = messager;
        this.externalizables.addAll(externalizables);
        this.packageName = packageName;
    }
    
    
    
    public String getQualifiedName() {
        return (packageName != null && !"".equals(packageName)) ? packageName + "." + className : className;
    }
    
    public String getSimpleName() {
        return className;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void generateSource(Filer filer) throws IOException {
        
        MethodSpec.Builder initBuilder = MethodSpec.methodBuilder("init");
        MethodSpec.Builder b = initBuilder;
        b.returns(void.class)
                .addModifiers(Modifier.PUBLIC);
        for (ExternalizableClass cls : externalizables) {
            b.addStatement("$T.register(\"$L\", $T.class)", ClassName.get("com.codename1.io", "Util"), cls.getTypeElement().getQualifiedName(), ClassName.get(cls.getPackageName(), cls.getSimpleName()));
        }
        
        MethodSpec.Builder createBuilder = MethodSpec.methodBuilder("create");
        b = createBuilder;
        
        TypeVariableName type = TypeVariableName.get("T");
        b.addModifiers(Modifier.PUBLIC)
                .addTypeVariable(type)
                .returns(type)
                .addParameter(ParameterizedTypeName.get(ClassName.get("java.lang", "Class"), type), "cls")
                ;
        for (ExternalizableClass cls : externalizables) {
            b.beginControlFlow("if ($T.class.equals(cls))", ClassName.get(cls.getTypeElement()));
            b.addStatement("return (T)new $T()", ClassName.get(cls.getPackageName(), cls.getSimpleName()));
            b.endControlFlow();
            
        }
        b.addStatement("throw new RuntimeException(\"$L\")", "No matching implementation found for class.");
        TypeSpec typeSpec = TypeSpec.classBuilder(getSimpleName())
                .addModifiers(Modifier.PUBLIC)
                .addMethod(initBuilder.build())
                .addMethod(createBuilder.build())
                .build();
          
        
        JavaFile javaFile = JavaFile.builder(getPackageName(), typeSpec).build();
        String classSource = javaFile.toString();
        
        TypeElement[] deps = new TypeElement[externalizables.size()];
        int i=0;
        for (ExternalizableClass cls : externalizables) {
            deps[i++] = cls.getTypeElement();
        }
        
        JavaFileObject jfo = filer.createSourceFile(getQualifiedName(), deps);
        //messager.printMessage(Kind.NOTE, "Writing Java source file "+jfo);
        
        Writer writer = null;
        try {
            writer = jfo.openWriter();
            writer.append("// DO NOT MODIFY THIS FILE.  IT HAS BEEN AUTOMATICALLY GENERATED\n");
            writer.append("// CHANGES MAY BE OVERWRITTEN WITHOUT NOTICE\n");
            javaFile.writeTo(writer);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex){}
            }
        }
        
        CN1ClientFiler clientFiler = new CN1ClientFiler(filer);
        JavaFileObject clientJfo = clientFiler.createSourceFile(getQualifiedName(), deps);
        Writer clientWriter = null;
        try {
            clientWriter = clientJfo.openWriter();
            clientWriter.append("// DO NOT MODIFY THIS FILE.  IT HAS BEEN AUTOMATICALLY GENERATED\n");
            clientWriter.append("// CHANGES MAY BE OVERWRITTEN WITHOUT NOTICE\n");
            clientWriter.append(classSource);
        } finally {
            if (clientWriter != null) {
                try {
                    clientWriter.close();
                } catch (Exception ex){}
            }
        }
        
    }
}
