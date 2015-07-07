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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
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
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 *
 * @author shannah
 */
public class ProxyClass {
    private TypeElement serverClass;
    private Messager messager;
    private Map<String, FactoryClass> factories = new HashMap<String,FactoryClass>();
    private String suffix = "Proxy";
    private Types typeUtils;
    
    public ProxyClass(TypeElement serverClass, Collection<FactoryClass> factories, Messager messager, Types types) {
        this.messager = messager;
        for (FactoryClass factory : factories) {
            this.factories.put(factory.getPackageName(), factory);
        }
        this.serverClass = serverClass;
        typeUtils = types;
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
    Set<String> findExportedPackages() {
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
                            ArrayType at = (ArrayType)ptype;
                            TypeMirror ctype = at.getComponentType();
                            if (ctype instanceof DeclaredType) {
                                TypeElement te = (TypeElement)((DeclaredType)ctype).asElement();
                                String pkg = getPackageName(te.getQualifiedName().toString());
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
                messager.printMessage(Diagnostic.Kind.ERROR, "Web service "+serverClass+" uses externalizables from the "+pkg+" package but no factory was found.");
            }
            out.add(factories.get(pkg));
        }
        return out;
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
    
    public void generateSource(Filer filer, Filer cn1SourceFiler) throws IOException {
        
        WebService anno = serverClass.getAnnotation(WebService.class);
        String urlPattern = anno.urlPattern();
        if (urlPattern == null || "".equals(urlPattern)) {
            urlPattern = "/"+serverClass.getSimpleName()+"Servlet";
        }
        
        MethodSpec.Builder initBuilder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(void.class);
        
        MethodSpec.Builder createBuilder = MethodSpec.methodBuilder("create");
        TypeVariableName type = TypeVariableName.get("T");
        createBuilder.addModifiers(Modifier.PUBLIC)
                .addTypeVariable(type)
                .returns(type)
                .addParameter(ParameterizedTypeName.get(ClassName.get("java.lang", "Class"), type), "cls")
                ;
        
        initBuilder.beginControlFlow("if (!initialized)");
        initBuilder.addStatement("initialized = true");
        for (FactoryClass factory : findExportedExternalizableFactories()) {
            initBuilder.addStatement("new $T().init()", ClassName.get(factory.getPackageName(), factory.getSimpleName()));
            createBuilder.beginControlFlow("try");
            createBuilder.addStatement("return new $T().create(cls)", ClassName.get(factory.getPackageName(), factory.getSimpleName()));
            createBuilder.endControlFlow();
            createBuilder.beginControlFlow("catch (Throwable t)");
            createBuilder.endControlFlow();
            createBuilder.addStatement("throw new RuntimeException(\"No matching implementation found for class.\")");
        }
        initBuilder.endControlFlow();
        
        FieldSpec url = FieldSpec.builder(String.class, "url", Modifier.PRIVATE, Modifier.FINAL).build();
        
        MethodSpec.Builder constr = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "url")
                .addStatement("init()")
                .beginControlFlow("if (url.charAt(url.length()-1) != '/')")
                .addStatement("url = url + \"/\"")
                .endControlFlow()
                .addStatement("String urlPattern = \"$L\"", urlPattern)
                .beginControlFlow("if (urlPattern.charAt(0) == '/')")
                .addStatement("urlPattern = urlPattern.substring(1)")
                .endControlFlow()
                .addStatement("this.url=url+urlPattern");
        
        FieldSpec initialized = FieldSpec.builder(boolean.class, "initialized", Modifier.PRIVATE, Modifier.STATIC).build();
        List<FieldSpec> fieldSpecs = new ArrayList<FieldSpec>();
        List<MethodSpec> methodSpecs = new ArrayList<MethodSpec>();
        
        ClassName proxyCall = ClassName.get("com.codename1.io", "WebServiceProxyCall");
        ClassName wsDef = ClassName.get("com.codename1.io", "WebServiceProxyCall.WSDefinition");
        for (Element e : serverClass.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD) {
                String methodName = e.getSimpleName().toString();
                ExecutableType t = (ExecutableType)e.asType();
                if (e.getModifiers().contains(Modifier.PUBLIC) && e.getModifiers().contains(Modifier.STATIC)) {
                    // It's a public method so let's expose this
                    
                    FieldSpec fp = FieldSpec.builder(wsDef, "def_"+methodName, Modifier.PRIVATE, Modifier.FINAL).build();
                    fieldSpecs.add(fp);
                    
                    MethodSpec.Builder methodSync = MethodSpec.methodBuilder(methodName)
                            .addModifiers(Modifier.PUBLIC)
                            .addException(IOException.class)
                            .returns(ClassName.get(t.getReturnType()));
                    
                    
                    StringBuilder stmt = new StringBuilder();
                    ArrayList stmtArgs = new ArrayList();
                    if (t.getReturnType().getKind() == TypeKind.VOID) {
                        stmt.append("$T.invokeWebserviceSync(def_$L, ");
                        stmtArgs.add(proxyCall);
                        stmtArgs.add(methodName);
                    } else {
                        stmt.append("return ($T)$T.invokeWebserviceSync(def_$L, ");

                        if (t.getReturnType().getKind().isPrimitive()) {
                            stmtArgs.add(ClassName.get(typeUtils.boxedClass((PrimitiveType)t.getReturnType())));
                        } else {
                            stmtArgs.add(ClassName.get(t.getReturnType()));
                        }
                        stmtArgs.add(proxyCall);
                        stmtArgs.add(methodName);
                    }
                    int i = 0;
                    for (TypeMirror ptype : t.getParameterTypes()) {
                        
                        methodSync.addParameter(ClassName.get(ptype), "arg"+i);
                        stmt.append("arg").append(i).append(", ");
                        i++;
                        /*
                        if (ptype.getKind().isPrimitive()) {
                            
                        } else {
                            stmt.append("($L)args[").append(i++).append("], ");
                            TypeElement te = (TypeElement)((DeclaredType)ptype).asElement();
                            stmtArgs.add(te.getQualifiedName().toString());
                        }
                        */        
                        
                        //methodSync.addParameter(ClassName.get(ptype.asElement().asType()), ptype.asElement().getSimpleName().toString());
                        
                        
                    }   
                    
                    stmt.setLength(stmt.length()-2);
                    
                    stmt.append(")");
                    methodSync.addStatement(stmt.toString(), stmtArgs.toArray());
                    
                    stmt = new StringBuilder();
                    stmtArgs = new ArrayList();
                    stmt.append("def_$L = $T.defineWebService(this.url, \"$L\", ");
                    stmtArgs.add(methodName);
                    stmtArgs.add(proxyCall);
                    stmtArgs.add(methodName);
                    
                    ArrayList<TypeMirror> ptypes = new ArrayList<TypeMirror>();
                    ptypes.add(t.getReturnType());
                    for (TypeMirror ptype : t.getParameterTypes()) {
                        ptypes.add(ptype);
                    }
                    i = 0;
                    for (TypeMirror ptype : ptypes) {
                        
                        //methodSync.addParameter(ClassName.get(ptype), "arg")
                        stmt.append("$T.TYPE_$L, ");
                        stmtArgs.add(proxyCall);
                        if (ptype.getKind() == TypeKind.VOID || ptype.getKind().isPrimitive()) {
                            stmtArgs.add(ptype.getKind().name().toUpperCase());
                        } else if (ptype.getKind() == TypeKind.ARRAY) {
                            String elType = getArrayElementType(ptype.toString());
                            if (isPrimitive(elType)) {
                                stmtArgs.add(elType.toUpperCase() + "_ARRAY");
                            } else if ("java.lang.String".equals(elType)) {
                                stmtArgs.add("STRING_ARRAY");
                            } else if (isBoxedType(elType)) {
                                int dotPos = elType.lastIndexOf(".");
                                String primitiveType = elType;
                                if (dotPos > 0) {
                                    primitiveType = primitiveType.substring(dotPos+1);
                                }
                                stmtArgs.add(primitiveType.toUpperCase() + "_ARRAY");
                            } else {
                                messager.printMessage(Diagnostic.Kind.ERROR, "Parameter "+ptype+" is not valid for web service.", e);
                            }
                        } else if (isBoxedType(ptype.toString())) {
                            int dotPos = ptype.toString().lastIndexOf(".");
                            String primitiveType = ptype.toString();
                            if (dotPos > 0) {
                                primitiveType = primitiveType.substring(dotPos+1);
                            }
                            stmtArgs.add(primitiveType.toUpperCase() + "_OBJECT");
                            
                        } else {
                            stmtArgs.add("EXTERNALIABLE");
                        }
                        i++;
                        
                    }
                    
                    stmt.setLength(stmt.length()-2);
                    stmt.append(")");
                    
                    constr.addStatement(stmt.toString(), stmtArgs.toArray());
                    
                    methodSpecs.add(methodSync.build());
                    
                    
                }
            }
        }
        
        
        
        
        
        
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(getSimpleName())
                .addModifiers(Modifier.PUBLIC)
                .addMethod(initBuilder.build())
                .addMethod(constr.build())
                .addMethod(createBuilder.build())
                .addField(url)
                .addField(initialized);
        
                
        for (FieldSpec fb : fieldSpecs) {
            typeSpec.addField(fb);
        }
        
        for (MethodSpec mb : methodSpecs) {
            typeSpec.addMethod(mb);
        }
        
                
        
        
        //System.out.println(typeSpec.build().toString());
        JavaFile javaFile = JavaFile.builder(getPackageName(), typeSpec.build()).build();
        
        //filer.createResource(StandardLocation., suffix, suffix, originatingElements)
        
        JavaFileObject jfo = cn1SourceFiler.createSourceFile(getQualifiedName(), null);
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
    
    private ClassName className(String fqn) {
        return ClassName.get(getPackageName(fqn), getSimpleName(fqn));
    }
}
