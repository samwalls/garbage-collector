package profiling;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimeProfiler implements Runnable {

    private Problem p;

    private List<Integer> instanceSizes;

    private Map<Integer, List<Long>> recordedTimes;

    private int nRuns;

    private String name;

    public TimeProfiler(String name, Problem p, List<Integer> instanceSizes, int nRuns) {
        this.name = name;
        this.p = p;
        this.instanceSizes = instanceSizes;
        this.nRuns = nRuns;
        recordedTimes = new HashMap<>();
        for (int i : instanceSizes)
            recordedTimes.put(i, new ArrayList<>());
    }

    @Override
    public void run() {
        String fileName = "profile_" + name + "_" + new SimpleDateFormat("ss_mm_HH_dd_MM_yyyy").format(new Date()) + ".csv";
        System.out.println("STARTING PROFILING FOR: " + fileName);
        for (int run = 0; run < nRuns; run++) {
            System.out.println("STARTING RUN " + (run + 1));
            for (int size : instanceSizes) {
                try {
                    p.init(size);
                    long timeNow = System.nanoTime();
                    p.run();
                    long timeAfter = System.nanoTime();
                    recordedTimes.get(size).add(timeAfter - timeNow);
                } catch (Exception e) {
                    System.err.println("profiling failed on run " + run);
                    e.printStackTrace();
                    return;
                }
            }
        }
        writeOut(fileName, averageTimes());
    }

    private Map<Integer, Long> averageTimes() {
        HashMap<Integer, Long> averages = new HashMap<>();
        for (Map.Entry<Integer, List<Long>> times : recordedTimes.entrySet()) {
            long average = 0;
            for (long time : times.getValue())
                average += time;
            average /= times.getValue().size();
            averages.put(times.getKey(), average);
        }
        return averages;
    }

    private void writeOut(String fileName, Map<Integer, Long> averageTimes) {
        try {
            FileWriter out = new FileWriter(fileName);
            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader("size", "time"))) {
                for (Map.Entry<Integer, Long> entry : averageTimes.entrySet())
                    printer.printRecord(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            System.err.println("failed to write profiling data");
            e.printStackTrace();
        }
    }
}
