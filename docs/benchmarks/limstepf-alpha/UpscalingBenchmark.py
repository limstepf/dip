# -*- coding: utf-8 -*-
"""
JMH benchmark analysis and plots:
Resampling (upscaling) benchmark
"""
import string
import pandas as pd
import matplotlib.pyplot as plt
from IPython.display import display

# options
#benchmark_csvfile = 'UpscalingBenchmark-20160804_121506.csv'
benchmark_csvfile = 'UpscalingBenchmark-20160825_162052.csv'
benchmark_prefix = 'ch.unifr.diva.dip.benchmarks.UpscalingBenchmark.'

plt_figsize = (9,9)

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

def filterType(i):
    if (i == 0):
        return '0:NN'
    if (i == 1):
        return '1:Bilinear'
    return '2:Bicubic'
    
df['Param: type'] = df['Param: type'].apply(lambda x: filterType(x))

# print table
display(df)

# plot avg time by image size
plt.rc('axes', color_cycle=[
    'r', 'r', 'r', 
    'g', 'g', 'g',
    'c', 'c', 'c',
    'b', 'b', 'b'
])

fig = plt.figure()
ax = pd.pivot_table(df, values='Score', columns=['Benchmark', 'Param: type'], index='Param: scale').plot(
    figsize=plt_figsize, 
    logy=True, logx=True,
    style=['x-', 'x--', 'x:', 'x-', 'x--', 'x:', 'x-', 'x--', 'x:', 'x-', 'x--', 'x:']
)

ax.set_xlabel('scale factor')
ax.set_ylabel('avg. ms/op')

ax.legend( # legend next to/ouside of plot
    loc='upper left', 
    bbox_to_anchor=(1.025, 1),
    borderaxespad=0,
    ncol=1
) 
