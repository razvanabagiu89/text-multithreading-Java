# Text Analysis using Map-Reduce Paradigm

## About

https://curs.upb.ro/2021/pluginfile.php/406992/mod_resource/content/9/Tema%202%20-%20Enunt.pdf

Analyse multiple texts by using the Map-Reduce paradigm with concepts like ExecutorService or ForkJoinPool

## How to run the tests

- run local bash script

## Structure & Flow

### For solving the tasks I used the following Java classes:

- Tema2

    The main entry point of the program starts here, by parsing the tests input and retrieving the data
regarding the number of threads to use, file to read and file to output the final result. After the parsing
is done, I proceeded to create 2 thread pools, one for the Map operation and another for the Reduce operation,
hence the name MapRunnable and ReduceRunnable. For each file to read given in the input, I adjusted the
workers offset and fragment dimension accordingly to the rules involving words at start or end of text. After this,
Map Tasks were assigned to workers, which were then collected back in the entry point, then Reduce Tasks
were assigned to workers in the second pool, which were then collected, sorted and written to file.

- MapRunnable

    After the offset and fragment dimension adjustments from above, in this class I am checking if the
offset and fragment dimension are valid, in order not to exceed the length of file. Each worker opens the
read file, reads *only* the required substring in order to provide memory efficiency and proceeds to compute
a dictionary with lengths of words and their occurrences, and a list of maximal length words.

- ReduceRunnable

    After all the Map Tasks are finalized, in the join phase, all the dictionaries from the same text are
brought together, alongside with their lists. At the process phase, the rank is computed accordingly and
the results are returned in a ConcurrentHashMap in order for the threads to return them efficiently.

- MapResult

    Class used to store the result from the Map Tasks.

- FiboCalculator

    Provides the nth number of the Fibonacci sequence.


## Project structure including tests
```bash
├── README.md
├── out
│   └── production
│       └── tema2
│           ├── Fibonacci.class
│           ├── MapResult.class
│           ├── MapRunnable.class
│           ├── ReduceRunnable.class
│           └── Tema2.class
├── skel
│   ├── FiboCalculator.java
│   ├── MapResult.java
│   ├── MapRunnable.java
│   ├── ReduceRunnable.java
│   └── Tema2.java
├── tema2.iml
├── test.sh
└── tests
    ├── files
    │   ├── alls_well_act1
    │   ├── alls_well_act2
    │   ├── alls_well_act3
    │   ├── alls_well_act4
    │   ├── alls_well_act5
    │   ├── alls_well_full
    │   ├── in1
    │   ├── in2
    │   ├── in3
    │   ├── sonnets_10
    │   ├── sonnets_20
    │   ├── sonnets_30
    │   ├── sonnets_40
    │   ├── sonnets_50
    │   └── sonnets_all
    ├── in
    │   ├── test0.txt
    │   ├── test1.txt
    │   ├── test2.txt
    │   ├── test3.txt
    │   └── test4.txt
    └── out
        ├── test0_out.txt
        ├── test1_out.txt
        ├── test2_out.txt
        ├── test3_out.txt
        └── test4_out.txt

```
