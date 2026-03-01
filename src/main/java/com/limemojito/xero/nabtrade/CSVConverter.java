/*
 * Copyright 2011-2026 Lime Mojito Pty Ltd
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

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * The CSVConverter class is responsible for converting CSV files from one format to another.
 * It reads input CSV files, processes them by converting specific columns and adding new calculated columns,
 * and then writes the output to the original file location after creating a backup of the original.
 *
 * <p>The conversion format is from NAB Trade to Xero CSV suitable for a statement import.</p>
 */
public class CSVConverter {
    private static final String[] INPUT_TITLES = ("Date,Type,Description,Debit,Credit,Balance").split(",");
    private static final String[] OUTPUT_TITLES = ("Date,Type,Description,Tx,Debit,Credit,Balance").split(",");
    private final File inputDir;

    /**
     * Create a new converter for the supplied input directory.
     *
     * @param inputDir Directory to scan for CSV files.
     */
    public CSVConverter(File inputDir) {
        this.inputDir = inputDir;
    }

    /**
     * Perform the conversion.
     *
     * @throws IOException On an IO failure
     */
    public void convert() throws IOException {
        File[] files = this.inputDir.listFiles((dir, name) -> name.endsWith("csv"));
        if (files == null) {
            throw new IOException("No CSV files found");
        }
        long processStartTime = System.currentTimeMillis();
        for (File file : files) {
            final List<String[]> inputData = readInput(file);
            final File backup = backup(file);
            writeOutput(file, inputData);
            remove(backup);
        }
        System.out.printf("Processed %d in %d ms.%n", files.length, System.currentTimeMillis() - processStartTime);
    }

    private void remove(File backup) throws IOException {
        if (!backup.delete()) {
            throw new IOException("Could not delete " + backup);
        }
    }

    private File backup(File file) throws IOException {
        final File backupFile = backupFile(file);
        if (!file.renameTo(backupFile)) {
            throw new IOException("Could not backup " + file.getAbsolutePath());
        }
        return backupFile;
    }

    private File backupFile(File file) {
        return new File(file.getParent(), file.getName() + ".bak");
    }

    private void writeOutput(File output, List<String[]> inputData) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(output))) {
            writer.writeNext(OUTPUT_TITLES);
            for (int i = 1; i < inputData.size(); i++) {
                final String[] line = inputData.get(i);
                final String[] outputLine = createOutputLine(line);
                writer.writeNext(outputLine);
            }
        }
    }

    private String[] createOutputLine(String[] inputLine) {
        final int debitIndex = 3;
        final int creditIndex = 4;
        final BigDecimal dr = readDecimal(inputLine[debitIndex]);
        final BigDecimal cr = readDecimal(inputLine[creditIndex]);
        final BigDecimal tx = cr.add(dr);
        final List<String> newLine = new LinkedList<>(asList(inputLine).subList(0, debitIndex));
        newLine.add(tx.toString());
        newLine.addAll(asList(inputLine).subList(debitIndex, INPUT_TITLES.length));
        return newLine.toArray(new String[OUTPUT_TITLES.length]);
    }

    private BigDecimal readDecimal(String val) {
        BigDecimal dr = new BigDecimal(trimToZero(val));
        dr = dr.setScale(2, HALF_UP);
        return dr;
    }

    private static String trimToZero(String val) {
        String trimmedString = trim(val);
        return isBlank(trimmedString) ? "0" : trimmedString;
    }

    private List<String[]> readInput(File file) throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
            return reader.readAll();
        }
    }
}
