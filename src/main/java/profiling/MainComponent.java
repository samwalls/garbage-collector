package profiling;

import java.util.Arrays;

public class MainComponent {

    public static void main(String[] args) {
        MainComponent mainComponent = new MainComponent();
        mainComponent.run();
    }

    public void run() {
        TimeProfiler profiler = new TimeProfiler("insertion", new TreadmillInsertionProblem(), Arrays.asList(
                // run for problem instances of these sizes
                1, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000
                // run 100 times and average
        ), 5);
        profiler.run();
        profiler = new TimeProfiler("indirect", new TreadmillIndirectProblem(), Arrays.asList(
                // run for problem instances of these sizes
                1, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000
                // run 100 times and average
        ), 5);
        profiler.run();
    }
}
