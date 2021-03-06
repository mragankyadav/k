// Copyright (c) 2014-2016 K Team. All Rights Reserved.
package org.kframework.main;

import org.kframework.utils.StringUtil;
import org.kframework.utils.errorsystem.KEMException;
import org.kframework.utils.errorsystem.KException;
import org.kframework.utils.errorsystem.KExceptionManager;
import org.kframework.utils.file.FileUtil;
import org.kframework.utils.file.JarInfo;

import com.beust.jcommander.ParameterException;

public abstract class FrontEnd {

    protected abstract int run();

    private final KExceptionManager kem;
    private final GlobalOptions globalOptions;
    private final FileUtil files;

    public FrontEnd(
            KExceptionManager kem,
            GlobalOptions globalOptions,
            FileUtil files) {
        this.kem = kem;
        this.globalOptions = globalOptions;
        this.files = files;
    }

    public int main() {
        int retval;
        try {
            try {
                retval = run();
            } catch (ParameterException e) {
                throw KEMException.criticalError(e.getMessage(), e);
            } finally {
                files.deleteTempDir(kem);
            }
            kem.print();
        } catch (KEMException e) {
            // terminated with errors, so we need to return nonzero error code.
            retval = 113;
            if (globalOptions.debug) {
                e.printStackTrace();
            }
            kem.registerThrown(e);
            kem.print();
        }
        return retval;
    }

    public static void printBootError(String message) {
        System.err.println(StringUtil.splitLines(KException.criticalError(message).toString()));
    }
}
