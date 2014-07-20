package com.freedomoss.ccompare;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class CsvMatcher {

    private static final Main main = new Main();


    public static void main(String[] args) throws IOException {
        InputStream is = CsvMatcher.class.getClassLoader().getResourceAsStream("companies-dataset-1.csv");
        CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(is)), ',');
        List<String[]> lines = csvReader.readAll();

        int total = 0, failed = 0;

        for (String[] csv : lines) {
            Preconditions.checkState(csv.length == 3, "Failed record: " + csv[0]);
            int confidence = main.compare(csv[0], csv[1]);
            boolean actual = confidence > 80;
            boolean expected = Boolean.parseBoolean(csv[2]);

            total++;
            if (expected != actual) {
                System.out.println(actual + "\t\t" + Joiner.on(",").join(csv));
                failed++;
            }
        }

        System.out.println();
        System.out.println();
        System.out.println("total: " + total + ", failed: " + failed + ", recognition: " + ((1 - (double) failed / total) * 100) + "%");
    }
}
