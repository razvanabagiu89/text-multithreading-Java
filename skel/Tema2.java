import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Tema2 {

    // nrOfThreads
    private final String nrWorkers;
    private final String inFile;
    private final String outFile;
    // ArrayList containing all input files to read from
    private ArrayList<String> files;
    private Integer nrFiles;
    private Integer fragmentDimension;

    // results stored from the Map operations
    static List<MapResult> mapResults = Collections.synchronizedList(new ArrayList<>());
    // results stored from the Reduce operations
    static ConcurrentHashMap<String, Double> reduceResults = new ConcurrentHashMap<>();

    public Tema2(String nrWorkers, String inFile, String outFile) {
        this.nrWorkers = nrWorkers;
        this.inFile = inFile;
        this.outFile = outFile;
        files = new ArrayList<>();
    }

    public ArrayList<String> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<String> files) {
        this.files = files;
    }

    // debug purposes
    @Override
    public String toString() {
        return "Tema2{" +
                "nrWorkers='" + nrWorkers + '\'' +
                ", in_file='" + inFile + '\'' +
                ", out_file='" + outFile + '\'' +
                ", files=" + files +
                ", fragmentDimension=" + fragmentDimension +
                ", nrFiles=" + nrFiles +
                '}';
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        if (args.length < 3) {
            System.err.println("Usage: Tema2 <workers> <in_file> <out_file>");
            return;
        }

        // parse phase
        String nrWorkers = args[0];
        String inFile = args[1];
        String outFile = args[2];
        Tema2 tema2 = new Tema2(nrWorkers, inFile, outFile);

        Scanner scanner = new Scanner(new File(tema2.inFile));
        tema2.fragmentDimension = scanner.nextInt();
        scanner.nextLine();
        tema2.nrFiles = scanner.nextInt();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            tema2.files.add(scanner.nextLine());
        }
        scanner.close();

        // map pool
        ExecutorService tpeMap = Executors.newFixedThreadPool(Integer.parseInt(tema2.nrWorkers));
        AtomicInteger counterMap = new AtomicInteger(0);
        Integer fileId = 0;

        // submit map tasks for each file
        for (String file : tema2.files) {
            int fileIndex = 0;
            int offset = 0;
            String text = Files.readString(Path.of(file));

            while (fileIndex < text.length()) {
                // fragment dimension for each task
                int fragmentDimensionTask = Math.min(fileIndex + tema2.fragmentDimension, text.length()) - fileIndex;

                // detect worker is in a word at beginning of file
                int newOffset = 0;
                int newFragmentDimensionTask = 0;

                if (offset - 1 > 0) {
                    // if offset - 1 is a letter
                    if ((text.charAt(offset - 1) >= 65 && text.charAt(offset - 1) <= 90) ||
                            text.charAt(offset - 1) >= 97 && text.charAt(offset - 1) <= 122) {

                        // if offset is a letter
                        if ((text.charAt(offset) >= 65 && text.charAt(offset) <= 90) ||
                                text.charAt(offset) >= 97 && text.charAt(offset) <= 122) {

                            // detected word, so we proceed to ignore it and move the offset to the right
                            int move = 1;
                            while (offset + move < text.length() && ((text.charAt(offset + move) >= 65 &&
                                    text.charAt(offset + move) <= 90) ||
                                    (text.charAt(offset + move) >= 97 && text.charAt(offset + move) <= 122))) {
                                move += 1;
                            }
                            newOffset = offset + move;
                        }
                    }
                }

                // detect worker is in a word at end of file
                // if last character is a letter
                if ((text.charAt(offset + fragmentDimensionTask - 1) >= 65 &&
                        text.charAt(offset + fragmentDimensionTask - 1) <= 90) ||
                        text.charAt(offset + fragmentDimensionTask - 1) >= 97 &&
                                text.charAt(offset + fragmentDimensionTask - 1) <= 122) {

                    // detected word, so we proceed to add it and move the fragmentDimensionTask to the right
                    int move = 1;
                    while (offset + fragmentDimensionTask - 1 + move < text.length() &&
                            ((text.charAt(offset + fragmentDimensionTask - 1 + move) >= 65 &&
                                    text.charAt(offset + fragmentDimensionTask - 1 + move) <= 90) ||
                            (text.charAt(offset + fragmentDimensionTask - 1 + move) >= 97 &&
                                    text.charAt(offset + fragmentDimensionTask - 1 + move) <= 122))) {
                        move += 1;
                    }
                    newFragmentDimensionTask = fragmentDimensionTask + move - 1;
                }

                if (newOffset != 0) {
                    if (newFragmentDimensionTask != 0) {
                        newFragmentDimensionTask -= (newOffset - offset);
                    } else {
                        newFragmentDimensionTask = fragmentDimensionTask - (newOffset - offset);
                    }
                }
                // cases for newFragmentDimensionTask or newOffset
                if (newOffset == 0 && newFragmentDimensionTask == 0) {
                    counterMap.incrementAndGet();
                    tpeMap.submit(new MapRunnable(file, fileId, offset, fragmentDimensionTask, counterMap, tpeMap));
                } else if (newOffset == 0) {
                    counterMap.incrementAndGet();
                    tpeMap.submit(new MapRunnable(file, fileId, offset, newFragmentDimensionTask, counterMap, tpeMap));
                } else if (newFragmentDimensionTask == 0) {
                    counterMap.incrementAndGet();
                    tpeMap.submit(new MapRunnable(file, fileId, newOffset, fragmentDimensionTask, counterMap, tpeMap));
                } else {
                    counterMap.incrementAndGet();
                    tpeMap.submit(new MapRunnable(file, fileId, newOffset, newFragmentDimensionTask, counterMap, tpeMap));
                }

                offset += tema2.fragmentDimension;
                fileIndex += fragmentDimensionTask;
            }
            fileId++;
        }

        boolean wait = tpeMap.awaitTermination(5, TimeUnit.SECONDS);
        if (!wait) {
            System.out.println("Error awaiting at Map");
        }

        // reduce pool
        ExecutorService tpeReduce = Executors.newFixedThreadPool(Integer.parseInt(tema2.nrWorkers));
        AtomicInteger counterReduce = new AtomicInteger(0);
        for (int i = 0; i < tema2.nrFiles; i++) {

            // process the map results further
            MapResult addMe = new MapResult(i);

            for (MapResult mapResult : mapResults) {
                // join phase
                if (i == mapResult.getId()) {
                    /*
                    1. join all dictionaries
                    2. join all maximalLists
                     */
                    for (Integer key : mapResult.getDict().keySet()) {
                        if (addMe.getDict().putIfAbsent(key, mapResult.getDict().get(key)) != null) {
                            addMe.getDict().put(key, addMe.getDict().get(key) + mapResult.getDict().get(key));
                        }
                    }
                    addMe.getMaximalList().addAll(mapResult.getMaximalList());
                }
            }
            counterReduce.incrementAndGet();
            tpeReduce.submit(new ReduceRunnable(tema2.getFiles().get(i), counterReduce, addMe, tpeReduce));
        }

        wait = tpeReduce.awaitTermination(5, TimeUnit.SECONDS);
        if (!wait) {
            System.out.println("Error awaiting at Reduce");
        }

        // sort by rank and output to file
        Map<String, Double> sortedResult = Tema2.reduceResults.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        FileWriter writer = new FileWriter(tema2.outFile);
        for (String key : sortedResult.keySet()) {
            writer.write(key.replace("tests/files/", "") + "\n");
        }
        writer.close();
    }
}
