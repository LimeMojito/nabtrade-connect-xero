/*
 * Copyright 2011-2025 Lime Mojito Pty Ltd
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.limemojito.xero.nabtrade;

import java.io.File;
import java.io.IOException;

/**
 * Performs a conversion of downloaded Nab Trade statements to Xero CSV import format.
 */
public class Main {

    /**
     * Supply the directory to read CSV files from.
     *
     * @param args main program arguments.
     * @throws Exception On a failure.
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new RuntimeException("Usage: inDir");
        }
        File input = new File(args[0]);

        if (!input.isDirectory()) {
            throw new IOException("Input is not a directory");
        }

        CSVConverter converter = new CSVConverter(input);
        converter.convert();
    }
}
