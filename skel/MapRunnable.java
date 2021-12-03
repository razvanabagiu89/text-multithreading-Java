import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class MapRunnable implements Runnable {

    private final String file;
    private final Integer offset;
    private final Integer fragmentDimension;
    private final MapResult mapResult;
    private final AtomicInteger counter;
    private final ExecutorService tpe;

    public MapRunnable(String file, Integer fileId, Integer offset, Integer fragmentDimension,
                       AtomicInteger counter, ExecutorService tpe) {
        this.file = file;
        this.offset = offset;
        this.fragmentDimension = fragmentDimension;
        this.mapResult = new MapResult(fileId);
        this.counter = counter;
        this.tpe = tpe;
    }

    @Override
    public void run() {
        /*
        1. check if tasks are valid
        2. compute dictionaries + maximalList
         */

        // get length of file to process the invalid tasks
        long length = new File(file).length();
        if(offset > length) {
            counter.decrementAndGet();
            Tema2.mapResults.add(mapResult);
        } else if (offset + fragmentDimension > length) {
            counter.decrementAndGet();
            Tema2.mapResults.add(mapResult);
        } else {
            String substring = null;
            try {
                // read only the substring required from file, so the worker won't load the whole file, being memory ineffective
                substring = Files.readString(Path.of(file)).substring(offset, offset + fragmentDimension);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] words = Objects.requireNonNull(substring).split("[^A-Za-z0-9]");

            // complete dictionary + max list
            int maxLen = -1;
            for (String s : words) {
                if (s.length() == 0) {
                    continue;
                }
                if (mapResult.getDict().putIfAbsent(s.length(), 1) != null) {
                    mapResult.getDict().put(s.length(), mapResult.getDict().get(s.length()) + 1);
                }
                if (maxLen <= s.length()) {
                    maxLen = s.length();
                }
            }
            for (String word : words) {
                if (word.length() == maxLen) {
                    mapResult.getMaximalList().add(word);
                }
            }
            // add the result
            Tema2.mapResults.add(mapResult);

            int left = counter.decrementAndGet();
            if (left == 0) {
                tpe.shutdown();
            }
        }
    }
}
