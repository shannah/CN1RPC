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

import com.codename1.io.Util;
import com.codename1.ws.annotations.Externalizable;
import com.codename1.ws.annotations.WebService;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author shannah
 */
@AutoService(Processor.class)
public class WebServiceProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    
    
    private Set<ExternalizableClass> externalizables = new HashSet<ExternalizableClass>();
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv); //To change body of generated methods, choose Tools | Templates.
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        
        
        
        
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<String>();
        annotations.add(WebService.class.getCanonicalName());
        annotations.add(Externalizable.class.getCanonicalName());
        return annotations;                
    }

    //@Override
    //public SourceVersion getSupportedSourceVersion() {
    //    return SourceVersion.RELEASE_5;
    //}
    
    private void addClass(String fqn, Element... dependencies) {
        try {
            if (elementUtils.getTypeElement(fqn) == null) {   
                JavaFileObject jfo = filer.createSourceFile(fqn, dependencies);
                Writer writer = null;
                try {
                    writer = jfo.openWriter();
                    String contents = Util.readToString(WebServiceProcessor.class.getResourceAsStream("/"+fqn.replace('.', '/')+".java"));

                    writer.write(contents);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (Exception ex){}
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            messager.printMessage(Kind.ERROR, "Failed to write "+fqn);
        }
    }
    
    
    private void addProxyServerHelper(Element... dependencies) {
        addClass("com.codename1.proxy.server.ProxyServerHelper", dependencies);
    }
    

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Get all of the externalizables
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Externalizable.class)) {
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                
                externalizables.add(new ExternalizableClass((TypeElement)annotatedElement, messager));
            }
        }
        
        for (ExternalizableClass cls : externalizables) {
            try {
                cls.generateSource(filer);
            } catch (IOException ex) {
                ex.printStackTrace();
                messager.printMessage(Kind.ERROR, ex.getMessage());
            }
        }
        
        
        // Now let's create factories for each package
        
        Map<String,Set<ExternalizableClass>> extPackages = new HashMap<String, Set<ExternalizableClass>>();
        for (ExternalizableClass cls : externalizables) {
            Set<ExternalizableClass> pkg = (Set<ExternalizableClass>)extPackages.get(cls.getPackageName());
            if (pkg == null) {
                pkg = new HashSet<ExternalizableClass>();
                extPackages.put(cls.getPackageName(), pkg);
            }
            pkg.add(cls);
        }
        List<FactoryClass> factories = new ArrayList<FactoryClass>();
        
        for (String pkg : extPackages.keySet()) {
            FactoryClass fc = new FactoryClass(pkg, extPackages.get(pkg), messager);
            factories.add(fc);
            try {
                fc.generateSource(filer);
            } catch (IOException ex) {
                ex.printStackTrace();
                messager.printMessage(Kind.ERROR, ex.getMessage());
                
            }
        }
        
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(WebService.class)) {
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                try {
                    generateSource((TypeElement)annotatedElement, factories);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    messager.printMessage(Kind.ERROR, ex.getMessage());
                    //Logger.getLogger(WebServiceProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        for (ExternalizableClass cls : externalizables) {
            if (!copyJavaSourceToClient(StandardLocation.SOURCE_OUTPUT, cls.getQualifiedName())) {
                copyJavaSourceToClient(StandardLocation.SOURCE_PATH, cls.getQualifiedName());
            }
            //if (!copyJavaSourceToClient(StandardLocation.SOURCE_OUTPUT, cls.classElement.getQualifiedName().toString())) {
            //    copyJavaSourceToClient(StandardLocation.SOURCE_PATH, cls.classElement.getQualifiedName().toString());
            //}
            
            TypeMirror currClass = cls.classElement.asType();
            while (currClass != null && !"java.lang.Object".equals(((TypeElement)typeUtils.asElement(currClass)).getQualifiedName().toString())) {
                TypeElement te = (TypeElement)typeUtils.asElement(currClass);
                if (te.getNestingKind().isNested()) {
                    Element el = te.getEnclosingElement();
                    while (((TypeElement)el).getNestingKind().isNested()) {
                        el = ((TypeElement)el).getEnclosingElement();
                    }
                    String fqn = ((TypeElement)el).getQualifiedName().toString();
                    if (!copyJavaSourceToClient(StandardLocation.SOURCE_OUTPUT, fqn)) {
                        copyJavaSourceToClient(StandardLocation.SOURCE_PATH, fqn);
                    }
                    
                } else {
                    String fqn = te.getQualifiedName().toString();
                    if (!copyJavaSourceToClient(StandardLocation.SOURCE_OUTPUT, fqn)) {
                        copyJavaSourceToClient(StandardLocation.SOURCE_PATH, fqn);
                    }
                }
                currClass = te.getSuperclass();
                
            }
        }
        
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(WebService.class)) {
            WebService cn1 = annotatedElement.getAnnotation(WebService.class);
            TypeElement te = (TypeElement)annotatedElement;
            try {
                for (String projectPath : cn1.exports()) {
                    File f = new File(getSourceRoot(te), projectPath);
                    if (f.exists() && f.isDirectory()) {
                        File buildXml = new File(f, "build.xml");
                        if (!buildXml.exists()) {
                            throw new IOException("Path was incorrect.  Could not find build.xml in cn1 project dir");
                        }
                        File srcDir = new File(f, "src");
                        if (!srcDir.exists()) {
                            throw new IOException("Path was incorrect.  Could not find src directory in cn1 project dir");
                        }
                        copyGeneratedSourcesToClient(te, f);
                    } else {
                        throw new IOException("Path was incorrect.  Could not find project cn1 directory.");
                    }
                }
                
            } catch (IOException ex) {
                messager.printMessage(Kind.ERROR, "Failed to find source path for "+te.getQualifiedName(), annotatedElement);
                return false;
            }
            
        }
        
        externalizables.clear();
        return true;
    }
    
    private boolean isInPackage(String pkg, File srcRoot, File srcFile) {
        File pkgDir = new File(srcRoot, pkg.replace('.', File.separatorChar));
        File f = srcFile.getParentFile();
        while (f != null && !f.equals(pkgDir)) {
            f = f.getParentFile();
        }
        return f != null;
    }
    
    private boolean isInPackage(Collection<String> packages, File srcRoot, File srcFile) {
        for (String pkg : packages) {
            if (isInPackage(pkg, srcRoot, srcFile)) {
                return true;
            }
        }
        return false;
    }
    
    private void copyGeneratedSourcesToClient(TypeElement cn1Class, File clientProjectDir) throws IOException {
        // Check managed files
        
        File clientProjectSrcDir = new File(clientProjectDir, "src");
        
        CN1ClientFiler clientFiler = new CN1ClientFiler(filer);
        File generatedSources = clientFiler.getRootOutputLocation();
        
        ProxyClass proxyClass = new ProxyClass(cn1Class, new ArrayList<FactoryClass>(), messager, typeUtils);
        Set<String> exportedPackages = proxyClass.findExportedPackages();
        exportedPackages.add(elementUtils.getPackageOf(cn1Class).getQualifiedName().toString());
        String fqn = cn1Class.getQualifiedName().toString();
        File manifest = new File(clientProjectDir, fqn+".mf");
        if (manifest.exists()) {
            String[] lines = Util.readToString(new FileInputStream(manifest)).split("\n");
            for (String srcFilePath : lines) {
                if (srcFilePath.trim().length() == 0) {
                    continue;
                }
                File srcFile = new File(clientProjectSrcDir, srcFilePath);
                File newFile = new File(generatedSources, srcFilePath);
                boolean shouldDelete = false;
                if (!shouldDelete && !srcFile.exists()) {
                    shouldDelete = true;
                }
                if (!shouldDelete && srcFile.lastModified() < newFile.lastModified()) {
                    shouldDelete = true;
                }
                if (!shouldDelete && !isInPackage(exportedPackages, clientProjectSrcDir, srcFile)) {
                    shouldDelete = true;
                }
                if (shouldDelete) {
                    srcFile.delete();
                }
            }
        }
        manifest.delete();
        //File destSources = new File(clientProjectDir, "src");
        manifestEntries = new ArrayList<String>();
        copyDirRoot = null;
        copyDirDestRoot = null;
        copyDirPackageFilter = exportedPackages;
        copyDir(generatedSources, clientProjectSrcDir, clientProjectSrcDir, clientProjectSrcDir, clientProjectSrcDir);
        PrintWriter writer = new PrintWriter(manifest.getAbsolutePath());
        for (String entry : manifestEntries) {
            writer.append(entry);
            writer.append("\n");
        }
        writer.close();
        manifestEntries = null; 
    }
    private List<String> manifestEntries;
    private File copyDirRoot, copyDirDestRoot;
    private Collection<String> copyDirPackageFilter;
    
    private void copyDir(File dir, File classesDir, File resDir, File sourceDir, File libsDir) throws IOException {
        if (copyDirRoot==null) {
            copyDirRoot = dir;
        }
        if (copyDirDestRoot==null) {
            copyDirDestRoot = sourceDir;
        }
        for (File currentFile : dir.listFiles()) {
            String fileName = currentFile.getName();
            if (currentFile.isDirectory()) {
                File newClassesDir = new File(classesDir, fileName);
                newClassesDir.mkdirs();
                File newresDir = new File(resDir, fileName);
                newresDir.mkdirs();
                File newsourceDir = new File(sourceDir, fileName);
                newsourceDir.mkdirs();
                File newlibsDir = new File(libsDir, fileName);
                newlibsDir.mkdirs();
                copyDir(currentFile, newClassesDir, newresDir, newsourceDir, newlibsDir);
                continue;
            }
            File destFile;
            if (fileName.endsWith(".class")) {
                destFile = new File(classesDir, fileName);
            } else {
                if (fileName.endsWith(".java") || fileName.endsWith(".m") || fileName.endsWith(".h") || fileName.endsWith(".cs")) {
                    destFile = new File(sourceDir, fileName);
                    if (copyDirPackageFilter != null && !isInPackage(copyDirPackageFilter, copyDirRoot, currentFile)) {
                        // If the current file isn't in one of the specified packages
                        // we just skip it
                        continue;
                    }
                    String relPath = destFile.getAbsolutePath().substring(copyDirDestRoot.getAbsolutePath().length()+1);
                    manifestEntries.add(relPath);
                } else {
                    if (fileName.endsWith(".jar") || fileName.endsWith(".a") || fileName.endsWith(".dylib")) {
                        destFile = new File(libsDir, fileName);
                    } else {
                        destFile = new File(resDir, fileName);
                    }
                }
            }
            destFile.getParentFile().mkdirs();
            DataInputStream di = new DataInputStream(new FileInputStream(currentFile));
            byte[] data = new byte[(int) currentFile.length()];
            di.readFully(data);
            di.close();

            FileOutputStream fos = new FileOutputStream(destFile);
            fos.write(data);
            fos.close();
        }
    }
    
    private File getSourceRoot(TypeElement te) throws IOException {

        FileObject fo = filer.getResource(StandardLocation.SOURCE_PATH, "", te.getQualifiedName().toString().replace('.','/')+".java");
        String path = fo.toUri().toURL().getFile();
        path = path.substring(0, path.length() - (te.getQualifiedName().toString()+".java").length());
        return new File(path);
        
    }
    
    private boolean copyJavaSourceToClient(Location sourceLocation, String className) {
        try {
            CN1ClientFiler clientFiler = new CN1ClientFiler(filer);
            FileObject fo = null;
            String sourceFilePath = className.replace('.','/')+".java";
            try {
                fo = filer.getResource(sourceLocation, "", sourceFilePath);
            } catch (Throwable ex) {
                messager.printMessage(Kind.NOTE, "Could not copy "+className+" to client files. "+ex.getMessage());
            }

            if (fo != null) {
                InputStream input = null;
                JavaFileObject outFile = clientFiler.createSourceFile(className);
                OutputStream output = null;
                try {
                    input = fo.openInputStream();
                    output = outFile.openOutputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Util.copy(input, baos);
                    String contents = new String(baos.toByteArray());
                    contents = contents.replaceAll("@Externalizable", "");
                    contents = contents.replaceAll("\\bimport com\\.codename1\\.ws.annotations\\.Externalizable;", "");
                    contents = "//GEN-BEGIN:autogenerated\n// DO NOT MODIFY THIS FILE.  IT HAS BEEN AUTOMATICALLY GENERATED\n" + 
                                "// CHANGES MAY BE OVERWRITTEN WITHOUT NOTICE\n" + contents + "\n//GEN-END:autogenerated\n";
                    Util.copy(new ByteArrayInputStream(contents.getBytes()), output);
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Throwable t){}
                    }
                    if (output != null) {
                        try {
                            output.close();
                        } catch (Throwable t){}
                    }
                }
                return true;
            }
            return false;
        } catch (IOException ex) {
            messager.printMessage(Kind.NOTE, "Could not copy "+className+" to client files. "+ex.getMessage());
            return false;
        }
    }
    
    private void generateSource(TypeElement classElement, Collection<FactoryClass> factories) throws IOException {
        generateServlet(classElement, factories);
        generateProxy(classElement, factories);
        
               
    }
    
    private void generateServlet(TypeElement classElement, Collection<FactoryClass> factories) throws IOException {
        addProxyServerHelper();
        ServletClass cls = new ServletClass(classElement, factories, messager, typeUtils );
        cls.generateSource(filer);
    }
    
    private void generateProxy(TypeElement classElement, Collection<FactoryClass> factories) throws IOException {
        ProxyClass cls = new ProxyClass(classElement, factories, messager, typeUtils);
        
        
        cls.generateSource(filer, new CN1ClientFiler(filer));
    }
    
}
