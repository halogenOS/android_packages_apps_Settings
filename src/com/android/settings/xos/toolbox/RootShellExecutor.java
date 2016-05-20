package com.android.settings.xos.toolbox;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Process;
import java.lang.Runtime;

public class RootShellExecutor {
    
    private RootShellExecutor() {
        // Empty private constructor
    }
    
    /**
     * Execute a command using SuperUser permissions
     */
    public static void execSu(String cmd) 
        throws InterruptedException, IOException {
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
    
        outputStream.writeBytes(cmd + "\n");
        outputStream.flush();
    
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        su.waitFor();
    }
    
    /**
     * Execute a command using SuperUser permissions and catch exceptions.
     * Return true if success, false if not.
     */ 
    public static boolean execSuSafe(String cmd) {
        try {
            execSu(cmd);
            return true;
        } catch(Exception ex) {
            return false;
        }
    }
    
}
