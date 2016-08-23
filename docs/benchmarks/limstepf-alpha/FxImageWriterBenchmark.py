# -*- coding: utf-8 -*-
"""
JMH benchmark analysis and plots:
benchmark
"""
import string
import pandas as pd
import matplotlib.pyplot as plt
from IPython.display import display

# options
benchmark_csvfile = 'FxImageWriterBenchmark-20160810_220306.csv'
benchmark_prefix = 'ch.unifr.diva.dip.benchmarks.FxImageWriterBenchmark.'

plt_figsize = (12,12)

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


# print table
display(df)

# plot avg time by image size
plt.rc('axes', color_cycle=[
    'r', 'r', 'r', '#FF6600', 'm',
    'g', 'b', 'b', 'y', 'c', 'c'
])

fig = plt.figure()
ax = pd.pivot_table(df, values='Score', columns='Benchmark', index='Param: size').plot(
    figsize=plt_figsize, 
    logy=True, logx=True,
    style='x-'
)

ax.set_xlabel('scale factor')
ax.set_ylabel('avg. ms/op')

ax.legend( # legend next to/ouside of plot
    loc='upper left', 
    bbox_to_anchor=(1.025, 1),
    borderaxespad=0,
    ncol=1
) 
