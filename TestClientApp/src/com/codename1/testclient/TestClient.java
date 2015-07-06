package com.codename1.testclient;


import com.codename1.testws.TestWebServerProxy;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;


public class TestClient {

    private Form current;
    
    public static TestWebServerProxy proxy;
    public void init(Object context) {
        try {
            Resources theme = Resources.openLayered("/theme");
            UIManager.getInstance().setThemeProps(theme.getTheme(theme.getThemeResourceNames()[0]));
        } catch(IOException e){
            e.printStackTrace();
        }
        
        proxy = new TestWebServerProxy("http://localhost:8080/TestWebApp");
        // Pro users - uncomment this code to get crash reports sent to you automatically
        /*Display.getInstance().addEdtErrorHandler(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                evt.consume();
                Log.p("Exception in AppName version " + Display.getInstance().getProperty("AppVersion", "Unknown"));
                Log.p("OS " + Display.getInstance().getPlatformName());
                Log.p("Error " + evt.getSource());
                Log.p("Current Form " + Display.getInstance().getCurrent().getName());
                Log.e((Throwable)evt.getSource());
                Log.sendLog();
            }
        });*/
    }
    
    public void start() {
        if(current != null){
            current.show();
            return;
        }
        Form hi = new Form("Hi World");
        hi.addComponent(new Label("Hi World"));
        hi.show();
        
        try {
            int c = proxy.add(1, 2);
            System.out.println("Answer is "+c);
            
            int c2 = proxy.addArray(new int[]{1,2,3,4});
            System.out.println("Answer is "+c2);
        } catch (IOException ex) {
            //Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
    }
    
    public void destroy() {
    }

}
