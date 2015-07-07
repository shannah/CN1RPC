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


import com.codename1.ws.annotations.WebService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 *
 * @author shannah
 */
public class ServletClass {
    
    private TypeElement serverClass;
    private Messager messager;
    private Map<String, FactoryClass> factories = new HashMap<String,FactoryClass>();
    private String suffix = "Servlet";
    private Types types;
    
    public ServletClass(TypeElement serverClass, Collection<FactoryClass> factories, Messager messager, Types types) {
        this.messager = messager;
        for (FactoryClass factory : factories) {
            this.factories.put(factory.getPackageName(), factory);
        }
        
        this.serverClass = serverClass;
        this.types = types;
    }
    
    
    
    public String getQualifiedName() {
        return serverClass.getQualifiedName()+suffix;
    }
    
    public String getSimpleName() {
        return serverClass.getSimpleName()+suffix;
    }
    
    public String getPackageName() {
        String fqn = serverClass.getQualifiedName().toString();
        int lastDot = fqn.lastIndexOf(".");
        if (lastDot >= 0) {
            return fqn.substring(0, lastDot);
        } else {
            return "";
        }
    }
    
    
    /**
     * Goes through all of the parameters (in and out) to find the externalizable
     * packages that need to be exported.
     * @return Set of package names
     */
    private Set<String> findExportedPackages() {
        HashSet<String> out = new HashSet<String>();
        for (Element e : serverClass.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD) {
                ExecutableType t = (ExecutableType)e.asType();
                if (e.getModifiers().contains(Modifier.PUBLIC) && e.getModifiers().contains(Modifier.STATIC)) {
                    List<TypeMirror> ptypes = new ArrayList<TypeMirror>();
                    ptypes.add(t.getReturnType());
                    for (TypeMirror ptype : t.getParameterTypes()) {
                        ptypes.add(ptype);
                    }
                    for (TypeMirror ptype : t.getParameterTypes()) {
                        ptypes.add(ptype);
                    }
                    for (TypeMirror ptype : ptypes) {
                        if (ptype.getKind().isPrimitive()) {
                            // We don't care about primitives
                            //stmt.append("(").append(ptype.toString()).append(")args[").append(i++).append("], ");
                        } else if (ptype.getKind() == TypeKind.VOID) {
                            // Do nothing here
                        } else if (ptype.getKind() == TypeKind.ARRAY) {
                            TypeMirror ctype = ((ArrayType)ptype).getComponentType();
                            if (ctype.getKind().isPrimitive()) {
                                // don't care
                            } else {
                                String pkg = getPackageName(((TypeElement)types.asElement(ctype)).getQualifiedName().toString());
                                if (pkg != null && !"".equals(pkg) && !pkg.startsWith("java")) {
                                    out.add(pkg);
                                }
                            }
                        } else {
                            TypeElement te = (TypeElement)((DeclaredType)ptype).asElement();
                            String pkg = getPackageName(te.getQualifiedName().toString());
                            if (pkg != null && !"".equals(pkg) && !pkg.startsWith("java")) {
                                out.add(pkg);
                            }
                        }
                    }
                }
            }
        }
        return out;
    }
    
    
    private List<FactoryClass> findExportedExternalizableFactories() {
        ArrayList<FactoryClass> out = new ArrayList<FactoryClass>();
        for (String pkg : findExportedPackages()) {
            if (!factories.containsKey(pkg)) {
                messager.printMessage(Kind.ERROR, "Web service "+serverClass+" uses externalizables from the "+pkg+" package but no factory was found.");
            }
            out.add(factories.get(pkg));
        }
        return out;
    }
    
    public void generateSource(Filer filer) throws IOException {
        
        MethodSpec.Builder initBuilder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addException(ClassName.get("javax.servlet", "ServletException"));
        
        for (FactoryClass factory : findExportedExternalizableFactories()) {
            initBuilder.addStatement("new $T().init()", ClassName.get(factory.getPackageName(), factory.getSimpleName()));
        }
        
        
        
        MethodSpec doGet = MethodSpec.methodBuilder("doGet")
                .addModifiers(Modifier.PROTECTED)
                .returns(void.class)
                .addParameter(ClassName.get("javax.servlet.http", "HttpServletRequest"), "request")
                .addParameter(ClassName.get("javax.servlet.http", "HttpServletResponse"), "response")
                .addException(ClassName.get("javax.servlet", "ServletException"))
                .addException(IOException.class)
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addStatement("response.setContentType(\"text/html;charset=UTF-8\")")
                .addStatement("$T out = response.getWriter()", java.io.PrintWriter.class)
                .addCode(CodeBlock.builder().beginControlFlow("try")
                        .addStatement("out.println(\"<!DOCTYPE html>\")")
                        .addStatement("out.println(\"<html>\")")
                        .addStatement("out.println(\"<head>\")")
                        .addStatement("out.println(\"<title>Error</title>\")")
                        .addStatement("out.println(\"</head>\")")
                        .addStatement("out.println(\"<body>\")")
                        .addStatement("out.println(\"<h1>Webservice access only!</h1>\")")
                        .addStatement("out.println(\"</body>\")")
                        .addStatement("out.println(\"</html>\")")
                        .endControlFlow().beginControlFlow("finally")
                        .addStatement("out.close()")
                        .endControlFlow()
                        .build()
                )
                .build();
        
        MethodSpec.Builder doPost = MethodSpec.methodBuilder("doPost")
                .addModifiers(Modifier.PROTECTED)
                .returns(void.class)
                .addParameter(ClassName.get("javax.servlet.http", "HttpServletRequest"), "request")
                .addParameter(ClassName.get("javax.servlet.http", "HttpServletResponse"), "response")
                .addException(ClassName.get("javax.servlet", "ServletException"))
                .addException(IOException.class)
                .addAnnotation(AnnotationSpec.builder(Override.class).build());
                
        MethodSpec.Builder b = doPost;
        b.addStatement("$T di = new $T(request.getInputStream())", DataInputStream.class, DataInputStream.class);
        b.addStatement("$T methodName = di.readUTF()", String.class);
        
        List<FieldSpec> fieldSpecs = new ArrayList<FieldSpec>();
        
        ClassName proxyServerHelper = ClassName.get("com.codename1.proxy.server", "ProxyServerHelper");
        for (Element e : serverClass.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD) {
                ExecutableType t = (ExecutableType)e.asType();
                if (e.getModifiers().contains(Modifier.PUBLIC) && e.getModifiers().contains(Modifier.STATIC)) {
                    // It's a public method so let's expose this
                    b.beginControlFlow("if (methodName.equals(\"$L\"))", e.getSimpleName().toString());
                    b.addStatement("Object[] args = $T.readMethodArguments(di, def_$L)", proxyServerHelper, e.getSimpleName().toString());
                    StringBuilder stmt = new StringBuilder();
                    List stmtArgs = new ArrayList();
                    TypeMirror retType = t.getReturnType();
                    
                    // TODO:  DEAL WITH VOID return types
                    if (retType.getKind() == TypeKind.VOID) {
                        stmt.append("$T.$L(");
                        stmtArgs.add(ClassName.get(getPackageName(), serverClass.getSimpleName().toString()));
                        stmtArgs.add(e.getSimpleName().toString());
                        
                        //stmt.append("$T.writeResponse(response, def_$L");
                        //stmtArgs.add(proxyServerHelper);
                        //stmtArgs.add(e.getSimpleName().toString());
                    } else {
                        stmt.append("$T.writeResponse(response, def_$L, ($L)$T.$L(");
                        stmtArgs.add(proxyServerHelper);
                        stmtArgs.add(e.getSimpleName().toString());



                        if (retType.getKind().isPrimitive()) {
                            stmtArgs.add(retType.toString());
                        } else if (retType.getKind() == TypeKind.ARRAY) {
                            ArrayType at = (ArrayType)retType;
                            stmtArgs.add(ClassName.get(at));
                            /*
                            TypeMirror ctype = at.getComponentType();
                            if (ctype.getKind().isPrimitive()) {
                                
                            }*/
                        } else {
                            TypeElement rte = (TypeElement)((DeclaredType)retType).asElement();
                            String rtfqn = rte.getQualifiedName().toString();

                            if ("java.lang.String".equals(rte.getQualifiedName().toString())) {
                                stmtArgs.add(String.class);
                            } else if (isBoxedType(rte.getQualifiedName().toString())) {
                                stmtArgs.add(ClassName.get("java.lang", getSimpleName(rtfqn)));
                            } else {
                                stmtArgs.add(ClassName.get("com.codename1.io", "Externalizable"));
                            }

                            //stmtArgs.add(ClassName.get(getPackageName(rtfqn)))
                        }

                        stmtArgs.add(ClassName.get(getPackageName(), serverClass.getSimpleName().toString()));
                        stmtArgs.add(e.getSimpleName().toString());
                    }
                    
                    
                    int i=0;
                    for (TypeMirror ptype : t.getParameterTypes()) {
                        stmt.append("($T)args[").append(i++).append("], ");
                        stmtArgs.add(ClassName.get(ptype));
                        /*
                        if (ptype.getKind().isPrimitive()) {
                            stmt.append("(").append(ptype.toString()).append(")args[").append(i++).append("], ");
                        } else if (ptype.getKind() == TypeKind.ARRAY) {
                            
                        } else {
                            stmt.append("($L)args[").append(i++).append("], ");
                            TypeElement te = (TypeElement)((DeclaredType)ptype).asElement();
                            stmtArgs.add(te.getQualifiedName().toString());
                        }
                                */
                    }
                    if (i>0) {
                        stmt.setLength(stmt.length()-2);
                    }
                    stmt.append(")");
                    if (retType.getKind() != TypeKind.VOID) {
                        stmt.append(")");
                    }
                    b.addStatement(stmt.toString(), stmtArgs.toArray());
                    
                    if (retType.getKind() == TypeKind.VOID) {
                        b.addStatement("$T.writeResponse(response, def_$L)", proxyServerHelper, e.getSimpleName().toString());
                    }
                    
                    b.addStatement("return");
                    b.endControlFlow();
                    
                    
                    FieldSpec.Builder fb = FieldSpec.builder(ClassName.get("com.codename1.proxy.server", "ProxyServerHelper.WSDefinition"), "def_"+e.getSimpleName(), Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC);
                    StringBuilder initBldr = new StringBuilder();
                    initBldr.append("$T.createServiceDefinition(\"$L\", ");
                    List initArgs = new ArrayList();
                    initArgs.add(proxyServerHelper);
                    initArgs.add(e.getSimpleName());
                    
                    List<TypeMirror> ptypes = new ArrayList<TypeMirror>();
                    ptypes.add(t.getReturnType());
                    for (TypeMirror ptype : t.getParameterTypes()) {
                        ptypes.add(ptype);
                    }
                    for (TypeMirror ptype : ptypes) {
                        initBldr.append("$T.$L, ");
                        initArgs.add(proxyServerHelper);
                        if (ptype.getKind() == TypeKind.VOID) {
                            initArgs.add("TYPE_VOID");
                        } else if (ptype.getKind().isPrimitive()) {
                            initArgs.add("TYPE_"+ptype.toString().toUpperCase());
                        } else if (ptype.getKind() == TypeKind.ARRAY) {
                            //String elType = getArrayElementType(te.getQualifiedName().toString());
                            ArrayType at = (ArrayType)ptype;
                            TypeMirror ctype = at.getComponentType();
                            if (ctype.getKind().isPrimitive()) {
                                initArgs.add("TYPE_"+ctype.toString().toUpperCase()+"_ARRAY");
                            } else if ("java.lang.String".equals(((TypeElement)types.asElement(ctype)).getQualifiedName().toString())) {
                                initArgs.add("TYPE_STRING_ARRAY");
                            } else {
                                initArgs.add("TYPE_EXTERNALIABLE");
                            }
                        } else {
                            TypeElement te = (TypeElement)((DeclaredType)ptype).asElement();
                            if ("java.lang.String".equals(te.getQualifiedName().toString())) {
                                initArgs.add("TYPE_STRING");
                            } else if (isBoxedType(te.getQualifiedName().toString())) {
                                initArgs.add("TYPE_"+te.getSimpleName().toString().toUpperCase()+"_OBJECT");
                            } else {
                                initArgs.add("TYPE_EXTERNALIABLE");
                            }
                        
                        }
                    }
                    initBldr.setLength(initBldr.length()-2);
                    initBldr.append(")");
                    
                    fb.initializer(initBldr.toString(), initArgs.toArray());
                    
                    fieldSpecs.add(fb.build());
                    
                }
            }
        }
        WebService wsAnno = serverClass.getAnnotation(WebService.class);
        
        String servletName = wsAnno.name();
        if (servletName == null || "".equals(servletName)) {
            servletName = getSimpleName();
        }
        String urlPattern = wsAnno.urlPattern();
        if (urlPattern == null || "".equals(urlPattern)) {
            urlPattern = "/"+getSimpleName();
        }
        
        
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(getSimpleName())
                .superclass(ClassName.get("javax.servlet.http", "HttpServlet"))
                .addAnnotation(
                        AnnotationSpec.builder(ClassName.get("javax.servlet.annotation", "WebServlet"))
                                .addMember("name","\"$L\"", servletName)
                                .addMember("urlPatterns", "{\"$L\"}", urlPattern)
                        .build())
                
                .addModifiers(Modifier.PUBLIC)
                .addMethod(initBuilder.build())
                .addMethod(doPost.build())
                .addMethod(doGet);
        for (FieldSpec fb : fieldSpecs) {
            typeSpec.addField(fb);
        }
        
                
        
        
        //System.out.println(typeSpec.build().toString());
        JavaFile javaFile = JavaFile.builder(getPackageName(), typeSpec.build()).build();
        
        
        
        JavaFileObject jfo = filer.createSourceFile(getQualifiedName(), null);
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
        
        
        
        
    }

    private static String[] boxedTypes = new String[]{
        "java.lang.Boolean",
        "java.lang.Integer",
        "java.lang.Double",
        "java.lang.Long",
        "java.lang.Short",
        "java.lang.Byte",
        "java.lang.Character",
        "java.lang.Float"
    };
    private boolean isBoxedType(String fqn) {
        return Arrays.asList(boxedTypes).contains(fqn);
    }
    
    private static String[] primitives = new String[] {
        "int", "short", "boolean", "byte", "char", "long", "float", "double"
    };
    
    private boolean isPrimitive(String fqn) {
        return Arrays.asList(primitives).contains(fqn);
    }
    
    private boolean isArrayType(String fqn) {
        return fqn.endsWith("[]");
    }
    
    private String getArrayElementType(String fqn) {
        return fqn.substring(0, fqn.indexOf("["));
    }
    
    private String getSimpleName(String fqn) {
        if (fqn.indexOf(".") >= 0) {
            return fqn.substring(fqn.indexOf(".")+1);
        } else {
            return fqn;
        }
    }
    
    private String getPackageName(String fqn) {
        if (fqn.indexOf(".") >= 0) {
            String pkg = fqn.substring(0, fqn.lastIndexOf("."));
            char firstChar = getSimpleName(pkg).charAt(0);
            if (firstChar >= 'A' && firstChar <= 'Z') {
                // This is a class name
                return getPackageName(pkg);
            } else {
                return pkg;
            }
        } else {
            return "";
        }
    }
    
}
