/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.ws.annotations.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author shannah
 */
public class CN1ClientFiler implements Filer {
    private Filer filer;
    private File rootOutputLocation;
    
    public CN1ClientFiler(Filer filer) {
        this.filer = filer;
    }
    

    @Override
    public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException {
        getRootOutputLocation().mkdirs();
        File file = new File(getRootOutputLocation(), name.toString().replace('.','/') + ".java");
        file.getParentFile().mkdirs();
        return new JFileObject(file.toURI(), JavaFileObject.Kind.SOURCE);
    }

    @Override
    public JavaFileObject createClassFile(CharSequence name, Element... originatingElements) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileObject createResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName, Element... originatingElements) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileObject getResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public File getRootOutputLocation() throws IOException {
        if (rootOutputLocation == null) {
            FileObject dummy = null;
            try {
                dummy = filer.getResource(StandardLocation.SOURCE_OUTPUT, "", "dummy.txt");
            } catch (IOException ex) {
                
            }
            if (dummy == null) {
                try {
                    dummy = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "dummy.txt");
                } catch (IOException ex) {
                    
                }
            }
            if (dummy == null) {
                throw new IOException("Failed to get root output location for Codename One client sources");
            }
            
            rootOutputLocation = new File(new File(dummy.toUri().toURL().getFile()).getParentFile().getParentFile().getParentFile(), "cn1-client-generated-sources");
        }
        return rootOutputLocation;
    }
    
    private class JFileObject extends SimpleJavaFileObject {
        JFileObject(URI uri, JavaFileObject.Kind kind) {
            super(uri, kind);
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            new File(uri.toURL().getFile()).getParentFile().mkdirs();
            return new FileOutputStream(new File(uri.toURL().getFile()));
        }
        
        
    }
}
