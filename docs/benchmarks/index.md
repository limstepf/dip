Benchmarks
==========
This page contains latest benchmark results which were the basis of profound decisions that have been made. Some come even with their very own Python script (using `pandas`, `matplotlib`, `numpy`, ...) to plot some lovely graphs, so enjoy!

Benchmarks are (unless stated otherwise) produced with the help of Java Microbenchmark Harness (JMH). 

Need to (re-)run some benchmark? You'll find them in in the `DIVA DIP Application` module (or `app` directory) in the package `ch.unifr.diva.dip.benchmarks`. And as long as all ingredients are in that module (or the `DIVA DIP API`) you can just add new ones there and run them straight from your IDE. Otherwise you better create a new module/project for the benchmark.

Contents
--------
* [limstepf-alpha](limstepf-alpha/):
    A set of benchmarks that ran on a Windows 7 Professional box with an Intel(R) Core(TM) i7-4770K CPU and 16 GB DDR3. So that's 4 cores, or 8 available processors with HT.

