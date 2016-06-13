/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package binp.nbi.tango.adc;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sanin
 */
public class Lock {

    private static final Logger logger = Logger.getLogger(Lock.class.getName());
    private File lockFile;

    Lock(String folder) {
        try {
            lockFile = new File(folder, "lock.lock");
            lockFile.createNewFile();
            logger.info("Lock file created.");
        } catch (IOException ex) {
            logger.log(Level.WARNING, null, ex);
        }
    }

    Lock() {
        this(null);
    }

    public boolean check() {
        return lockFile.exists();
    }

    public void unlock() {
        lockFile.delete();
        logger.info("Lock file deleted.");
    }
}
