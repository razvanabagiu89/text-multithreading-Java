import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class ReduceRunnable implements Runnable {

    private final String file;
    private final MapResult mapResult;
    private final AtomicInteger counter;
    private final ExecutorService tpe;

    public ReduceRunnable(String file, AtomicInteger counter, MapResult mapResult, ExecutorService tpe) {
        this.file = file;
        this.mapResult = mapResult;
        this.counter = counter;
        this.tpe = tpe;
    }

    @Override
    public void run() {

        // processing phase
        double sumFib = 0;
        int maxLen = -1;

        // get sum of fibonacci levels for each letter
        for (int key : mapResult.getDict().keySet()) {
            sumFib += FiboCalculator.compute(key + 1) * mapResult.getDict().get(key);
            if (maxLen < key) {
                maxLen = key;
            }
        }

        // get nr of occurrences for the longest word
        int occurrences = 0;
        for (int i = 0; i < mapResult.getMaximalList().size(); i++) {
            if (maxLen == mapResult.getMaximalList().get(i).length()) {
                occurrences++;
            }
        }

        // get nr of words from the already computed dictionary
        double nrWords = 0;
        for (double aux : mapResult.getDict().values()) {
            nrWords += aux;
        }
        double rank = sumFib / nrWords;

        // add to results
        String out = file + "," + String.format("%.2f", rank) + "," + maxLen + "," + occurrences;
        Tema2.reduceResults.put(out, Math.ceil(rank * 100) / 100);

        int left = counter.decrementAndGet();
        if (left == 0) {
            tpe.shutdown();
        }
    }
}
