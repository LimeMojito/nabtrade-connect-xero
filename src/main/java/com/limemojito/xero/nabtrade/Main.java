package com.limemojito.xero.nabtrade;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new RuntimeException("Usage: inDir");
        }
        File input = new File(args[0]);

        if (!input.isDirectory()) {
            throw new IOException("Input is not a directory");
        }

        CSVConverter converter = new CSVConverter();
        converter.setInputDir(input);
        converter.convert();
    }
}
