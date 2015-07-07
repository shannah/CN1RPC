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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 *
 * @author shannah
 */
public class ExternalizableClass {
    final TypeElement classElement;
    private final String suffix = "Impl";
    private final Messager messager;
    
    public ExternalizableClass(TypeElement classElement, Messager messager) {
        this.classElement = classElement;
        this.messager = messager;
    }
    
    public TypeElement getTypeElement() {
        return classElement;
    }
    
    public String getQualifiedName() {
        if (classElement.getNestingKind().isNested()) {
            Element el = classElement.getEnclosingElement();
            while (((TypeElement)el).getNestingKind().isNested()) {
                el = ((TypeElement)el).getEnclosingElement();
            }
            return ((TypeElement)el).getQualifiedName() + "_" + classElement.getSimpleName() + suffix;
        } else {
            return classElement.getQualifiedName().toString()+suffix;
        }
    }
    
    public String getSimpleName() {
        if (classElement.getNestingKind().isNested()) {
            Element el = classElement.getEnclosingElement();
            while (((TypeElement)el).getNestingKind().isNested()) {
                el = ((TypeElement)el).getEnclosingElement();
            }
            return ((TypeElement)el).getSimpleName() + "_" + classElement.getSimpleName() + suffix;
        } else {
            return classElement.getSimpleName().toString()+suffix;
        }
    }
    
    public String getPackageName() {
        Element el = classElement;
        while (((TypeElement)el).getNestingKind().isNested()) {
            el = ((TypeElement)el).getEnclosingElement();
        }
        String fqn = ((TypeElement)el).getQualifiedName().toString();
        int lastDot = fqn.lastIndexOf(".");
        if (lastDot >= 0) {
            return fqn.substring(0, lastDot);
        } else {
            return "";
        }
    }
    
    private String getPrimitiveWriteMethod(String typeName) {
        String writeStr = null;
        if ("int".equals(typeName)) {
            writeStr = "writeInt";
        } else if ("short".equals(typeName)) {
            writeStr = "writeShort";
        } else if ("long".equals(typeName)) {
            writeStr = "writeLong";
        } else if ("boolean".equals(typeName)) {
            writeStr = "writeBoolean";
        } else if ("byte".equals(typeName)) {
            writeStr = "writeByte";
        } else if ("char".equals(typeName)) {
            writeStr = "writeChar";
        } else if ("float".equals(typeName)) {
            writeStr = "writeFloat";
        } else if ("double".equals(typeName)) {
            writeStr = "writeDouble";
        }
        return writeStr;
    }
    
    private String getPrimitiveReadMethod(String typeName) {
        String writeStr = null;
        if ("int".equals(typeName)) {
            writeStr = "readInt";
        } else if ("short".equals(typeName)) {
            writeStr = "readShort";
        } else if ("long".equals(typeName)) {
            writeStr = "readLong";
        } else if ("boolean".equals(typeName)) {
            writeStr = "readBoolean";
        } else if ("byte".equals(typeName)) {
            writeStr = "readByte";
        } else if ("char".equals(typeName)) {
            writeStr = "readChar";
        } else if ("float".equals(typeName)) {
            writeStr = "readFloat";
        } else if ("double".equals(typeName)) {
            writeStr = "readDouble";
        }
        return writeStr;
    }
    
    public void generateSource(Filer filer) throws IOException {
        messager.printMessage(Kind.NOTE, "Generating source for "+getQualifiedName());
        // Generate the externalize method
        MethodSpec.Builder externalizeBuilder = MethodSpec.methodBuilder("externalize")
                .addParameter(java.io.DataOutputStream.class, "out")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addException(IOException.class);
        MethodSpec.Builder builder = externalizeBuilder;
        ClassName util = ClassName.get("com.codename1.io", "Util");
        
        List<Element> fields = new ArrayList<Element>();
        //for (Element enclosed : classElement.getEnclosedElements()) {
        //    if (enclosed.getKind() == ElementKind.FIELD && !enclosed.getModifiers().contains(Modifier.PRIVATE)) {
        //        fields.add(enclosed);
        //    }
        //}
        
        
        TypeMirror currType = classElement.asType();
        while (currType != null && currType.getKind() == TypeKind.DECLARED) {
            DeclaredType dtype = (DeclaredType)currType;
            TypeElement typeEl = (TypeElement)dtype.asElement();
            if ("java.lang.Object".equals(typeEl.getQualifiedName().toString())) {
                break;
            }
            for (Element enclosed : typeEl.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.FIELD && !enclosed.getModifiers().contains(Modifier.PRIVATE)) {
                    fields.add(enclosed);
                }
            }
            currType = typeEl.getSuperclass();
        }
        
        for (Element enclosed : fields) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement)enclosed;
                TypeMirror type = field.asType();
                String typeName = type.toString();
                String writeStr = getPrimitiveWriteMethod(typeName);
                
                if (writeStr != null) {
                    // It's a primitive type
                    builder.addStatement("out."+writeStr+"(this."+field.toString()+")");
                } else if (typeName.endsWith("[]")) {
                    // It's an array
                    builder.addStatement("out.writeInt(this.$N==null?0:this.$N.length)", field.toString(), field.toString());
                    String arrayElementTypeName = typeName.substring(0, typeName.lastIndexOf("["));
                    if (arrayElementTypeName.endsWith("]")) {
                        messager.printMessage(Kind.ERROR, "@Externalizable doesn't support 2D arrays.  2D array found for field "+field+" of type "+classElement);
                        return;
                    }
                    writeStr = getPrimitiveWriteMethod(arrayElementTypeName);
                    
                    CodeBlock.Builder loop = CodeBlock.builder();
                    loop.beginControlFlow("if (this.$N != null)", field.toString());
                    loop.beginControlFlow("for (int i=0; i<this.$N.length; i++)", field.toString());
                    if (writeStr != null) {
                        loop.addStatement("out."+writeStr+"(this.$N[i])", field.toString());
                    } else if ("java.lang.String".equals(arrayElementTypeName)) {
                        loop.addStatement("$T.writeUTF(this.$N[i], out)", util, field.toString());
                    } else {
                        loop.addStatement("$T.writeObject(this.$N[i], out)", util, field.toString());
                    }
                    loop.endControlFlow();
                    loop.endControlFlow();
                    
                    builder.addCode(loop.build());
                    
                } else if ("java.lang.String".equals(typeName)) {
                    builder.addStatement("$T.writeUTF(this.$N, out)", util, field.toString() );
                } else {
                    builder.addStatement("$T.writeObject(this.$N, out)", util, field.toString());
                }
                
            }
        }
        
        
        // Now generate the internalize method
        MethodSpec.Builder internalizeBuilder = MethodSpec.methodBuilder("internalize")
                .addParameter(int.class, "version")
                .addParameter(java.io.DataInputStream.class, "in")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addException(IOException.class);
        builder = internalizeBuilder;
        
        
        
        for (Element enclosed : fields) {
            if (enclosed.getKind() == ElementKind.FIELD && !enclosed.getModifiers().contains(Modifier.PRIVATE)) {
                VariableElement field = (VariableElement)enclosed;
                TypeMirror type = field.asType();
                String typeName = type.toString();
                System.out.println("Field "+field);
                String readStr = getPrimitiveReadMethod(typeName);
                
                if (readStr != null) {
                    // It's a primitive type
                    builder.addStatement("this.$N = in."+readStr+"()", field.toString());
                } else if (typeName.endsWith("[]")) {
                    String arrayElementTypeName = typeName.substring(0, typeName.lastIndexOf("["));
                    if (arrayElementTypeName.endsWith("]")) {
                        messager.printMessage(Kind.ERROR, "@Externalizable doesn't support 2D arrays.  2D array found for field "+field+" of type "+classElement);
                        return;
                    }
                    
                    int lastDot = arrayElementTypeName.lastIndexOf(".");
                    String arrayElementTypePkg = "";
                    String arrayElementSimpleName = arrayElementTypeName;
                    if (lastDot >= 0) {
                        arrayElementTypePkg = arrayElementTypeName.substring(0, lastDot);
                        arrayElementSimpleName = arrayElementTypeName.substring(lastDot+1);
                    }
                    // It's an array
                    builder.addStatement("int len = in.readInt()");
                   
                    
                    readStr = getPrimitiveReadMethod(arrayElementTypeName);
                    if (readStr == null) {
                        builder.addStatement("this.$N = new $T[len]", field.toString(), ClassName.get(arrayElementTypePkg, arrayElementSimpleName));
                    } else {
                        builder.addStatement("this.$N = new "+arrayElementTypeName+"[len]", field.toString());
                    }
                    
                    CodeBlock.Builder loop = CodeBlock.builder();
                    loop.beginControlFlow("if (len>0)");
                    loop.beginControlFlow("for (int i=0; i<len; i++)");
                    if (readStr != null) {
                        loop.addStatement("this.$N[i] = in."+readStr+"()", field.toString());
                    } else if ("java.lang.String".equals(arrayElementTypeName)) {
                        loop.addStatement("this.$N[i] = $T.readUTF(in)", field.toString(), util);
                    } else {
                        loop.addStatement("this.$N[i] = ("+arrayElementTypeName+")$T.readObject(in)", field.toString(), util);
                    }
                    loop.endControlFlow();
                    loop.endControlFlow();
                    
                    builder.addCode(loop.build());
                    
                } else if ("java.lang.String".equals(typeName)) {
                    builder.addStatement("this.$N = $T.readUTF(in)",field.toString(), util );
                } else {
                    builder.addStatement("this.$N = ("+typeName+")$T.readObject(in)", field.toString(), util);
                }
                
            }
        }
        
        MethodSpec.Builder getVersion = MethodSpec.methodBuilder("getVersion")
                .returns(int.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addStatement("return 1");
        
        MethodSpec.Builder getObjectId = MethodSpec.methodBuilder("getObjectId")
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addStatement("return \"$L\"", classElement.getQualifiedName().toString());
                
        
        TypeSpec type = TypeSpec.classBuilder(getSimpleName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get("com.codename1.io", "Externalizable"))
                .superclass(ClassName.get(classElement))
                .addMethod(getObjectId.build())
                .addMethod(getVersion.build())
                .addMethod(externalizeBuilder.build())
                .addMethod(internalizeBuilder.build())
                .build();
          
        
        JavaFile javaFile = JavaFile.builder(getPackageName(), type).build();
        String classSource = javaFile.toString();
        //JavaFileObject jfo = filer.createSourceFile(getQualifiedName(), classElement);
        // Seems to still be hitting this netbeans bug if I include originiting
        JavaFileObject jfo = filer.createSourceFile(getQualifiedName(), null);
        
        CN1ClientFiler clientFiler = new CN1ClientFiler(filer);
        
        JavaFileObject clientJfo = clientFiler.createSourceFile(getQualifiedName(), new Element[]{classElement.getEnclosingElement()});
        //messager.printMessage(Kind.NOTE, "Writing Java source file "+jfo);
        
        Writer writer = null;
        Writer clientWriter = null;
        try {
            writer = jfo.openWriter();
            javaFile.writeTo(writer);
            
            clientWriter = clientJfo.openWriter();
            clientWriter.append("// DO NOT MODIFY THIS FILE.  IT HAS BEEN AUTOMATICALLY GENERATED\n");
            clientWriter.append("// CHANGES MAY BE OVERWRITTEN WITHOUT NOTICE\n");
            clientWriter.append(classSource);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex){}
            }
            if (clientWriter != null) {
                try {
                    clientWriter.close();
                } catch (Exception ex){}
            }
        }
        
        
                
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExternalizableClass) {
            ExternalizableClass e = (ExternalizableClass)obj;
            return e.classElement.getQualifiedName().equals(classElement.getQualifiedName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return classElement.getQualifiedName().hashCode();
    }
    
    
    
    
    
}
