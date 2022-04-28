from turtle import color
from matplotlib import pyplot as plt
import json

plt.style.use('fivethirtyeight')

def plotStats(codebasesPath, codebaseName):

    with open(codebasesPath + codebaseName + "/analyser/features/methodCalls/analisysStats.json") as f:
        analisys_stats = json.load(f)

    cohesion_stats = analisys_stats['cohesion']
    complexity_stats = analisys_stats['complexity']
    coupling_stats = analisys_stats['coupling']
    performance_stats = analisys_stats['performance']

    nbr_clusters = [i for i in range(1, 11)]
    mean_fmca_complexity = [complexity['mean'] for complexity in complexity_stats]
    min_fmca_complexity  = [complexity['min']  for complexity in complexity_stats]
    max_fmca_complexity  = [complexity['max']  for complexity in complexity_stats]

    plt.plot(nbr_clusters, mean_fmca_complexity, color='k', linestyle='-', marker='o', label='Mean')
    plt.plot(nbr_clusters, min_fmca_complexity, color='r', linestyle='--', marker='v', label='Min')
    plt.plot(nbr_clusters, max_fmca_complexity, color='b', linestyle='-', marker='^', label='Max')

    plt.xlabel('Number of clusters')
    plt.ylabel('Complexity')
    plt.title('Mean Complexity per Number of clusters')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(codebasesPath + codebaseName + "/" + dendrogramName + "/analyser/features/methodCalls/complexity.png", format="png", bbox_inches='tight')
    