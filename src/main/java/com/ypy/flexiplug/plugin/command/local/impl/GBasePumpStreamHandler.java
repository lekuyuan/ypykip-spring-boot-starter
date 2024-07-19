package com.ypy.flexiplug.plugin.command.local.impl;

import org.apache.commons.exec.PumpStreamHandler;

import java.io.InputStream;
import java.io.OutputStream;

public class GBasePumpStreamHandler extends PumpStreamHandler {

    public GBasePumpStreamHandler() {
        super();
    }

    public GBasePumpStreamHandler(OutputStream outAndErr) {
        super(outAndErr, outAndErr);
    }

    public GBasePumpStreamHandler(OutputStream out, OutputStream err) {
        super(out, err, (InputStream)null);
    }

    public GBasePumpStreamHandler(OutputStream out, OutputStream err, InputStream input) {
        super(out, err, input);
    }

    OutputStream out(){
        return super.getOut();
    }

    OutputStream err(){
        return super.getErr();
    }
}
