# -*- coding: utf-8 -*-
"""
JMH benchmark analysis and plots:
ConcurrentOp speedup/threads
"""
import string
import pandas as pd
import matplotlib.pyplot as plt
from IPython.display import display

# options
benchmark_csvfile = 'ConcurrentOpThreadsRgbBenchmark-20160707_033227.csv'
benchmark_prefix = 'ch.unifr.diva.dip.benchmarks.ConcurrentOpThreadsRgbBenchmark.'
#benchmark_csvfile = 'ConcurrentOpThreadsGrayBenchmark-20160707_032712.csv'
#benchmark_prefix = 'ch.unifr.diva.dip.benchmarks.ConcurrentOpThreadsGrayBenchmark.'

plt_figsize = (6,6)

pd.set_option('display.max_columns', None)
pd.set_option('display.max_rows', None)

# parse file
df = pd.read_csv(
    benchmark_csvfile,
    delimiter=',',
    header=0
);


#remove namespace from benchmark names
df['Benchmark'] = df['Benchmark'].apply(lambda x: string.replace(x, benchmark_prefix, ''))

# find single thread score for each benchmark
max_threads = df['Param: numThreads'].max()
score_singleThread = {}
df_singleThread = df.loc[df['Param: numThreads'] == 1]

for index, row in df_singleThread.iterrows():
    score_singleThread[row['Benchmark']] = row['Score']

# calculate speedup
df['Speedup'] = df.apply(lambda row: score_singleThread[row['Benchmark']]  / row['Score'], axis=1 )


# print table
display(df)

# plot speedup by num threads for all benchmarks
fig = plt.figure()
ax = pd.pivot_table(df, values='Speedup', columns='Benchmark', index='Param: numThreads').plot(
    figsize=plt_figsize, 
    style='x-'
)

ax.add_line( # draw perfect/linear speedup
    plt.Line2D(
        (1, max_threads), 
        (1, max_threads), 
        linewidth=.5,
        linestyle='--',
        color='k'
    )
)

ax.set_xlabel('threads')
ax.set_ylabel('speedup')

ax.legend( # legend next to/ouside of plot
    loc='upper left', 
    bbox_to_anchor=(1.025, 1),
    borderaxespad=0,
    ncol=1
) 

plt.xlim(1, max_threads) # equal aspect
plt.ylim(1, max_threads)





