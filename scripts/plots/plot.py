from turtle import color
from matplotlib import pyplot as plt
import json

plt.style.use('seaborn-whitegrid')

def plotStats(codebasesPath, codebaseName, analysisType, strategy):

    analyserPath = "/analyser/" + analysisType
    if strategy != None and len(strategy) != 0:
        analyserPath += "/" + strategy

    with open(codebasesPath + codebaseName + analyserPath + "/analisysStats.json") as f:
        analisys_stats = json.load(f)

    cohesion_stats = analisys_stats['cohesion']
    complexity_stats = analisys_stats['complexity']
    coupling_stats = analisys_stats['coupling']
    performance_stats = analisys_stats['performance']

    nbr_clusters = [i for i in range(1, 11)]
    mean_complexity = [complexity['mean'] for complexity in complexity_stats]
    min_complexity  = [complexity['min']  for complexity in complexity_stats]
    max_complexity  = [complexity['max']  for complexity in complexity_stats]

    plt.plot(nbr_clusters, mean_complexity, color='#EDC192', label='Mean')
    plt.plot(nbr_clusters, min_complexity, color='#928E5E', label='Min')
    plt.plot(nbr_clusters, max_complexity, color='#80C2CE', label='Max')

    plt.xlabel('Number of clusters')
    plt.ylabel('Complexity')
    plt.title('Mean Complexity per Number of clusters')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(codebasesPath + codebaseName + analyserPath + "/complexity.png", format="png", bbox_inches='tight')

    plt.clf()

    nbr_clusters = [i for i in range(1, 11)]
    mean_performance = [performance['mean'] for performance in performance_stats]
    min_performance  = [performance['min']  for performance in performance_stats]
    max_performance  = [performance['max']  for performance in performance_stats]

    plt.plot(nbr_clusters, mean_performance, color='#EDC192', label='Mean')
    plt.plot(nbr_clusters, min_performance, color='#928E5E', label='Min')
    plt.plot(nbr_clusters, max_performance, color='#80C2CE', label='Max')

    plt.xlabel('Number of clusters')
    plt.ylabel('Performance')
    plt.title('Performance per Number of clusters')
    plt.legend()
    # plt.grid(True)
    plt.tight_layout()
    plt.savefig(codebasesPath + codebaseName + analyserPath + "/performance.png", format="png", bbox_inches='tight')

    plt.clf()

    nbr_clusters = [i for i in range(1, 11)]
    mean_cohesion = [cohesion['mean'] for cohesion in cohesion_stats]
    min_cohesion  = [cohesion['min']  for cohesion in cohesion_stats]
    max_cohesion  = [cohesion['max']  for cohesion in cohesion_stats]

    plt.plot(nbr_clusters, mean_cohesion, color='#EDC192', label='Mean')
    plt.plot(nbr_clusters, min_cohesion, color='#928E5E', label='Min')
    plt.plot(nbr_clusters, max_cohesion, color='#80C2CE', label='Max')

    plt.xlabel('Number of clusters')
    plt.ylabel('Cohesion')
    plt.title('Cohesion per Number of clusters')
    plt.legend()
    plt.grid(True)
    # plt.tight_layout()
    plt.savefig(codebasesPath + codebaseName + analyserPath + "/cohesion.png", format="png", bbox_inches='tight')

    plt.clf()

    nbr_clusters = [i for i in range(1, 11)]
    mean_coupling = [coupling['mean'] for coupling in coupling_stats]
    min_coupling  = [coupling['min']  for coupling in coupling_stats]
    max_coupling  = [coupling['max']  for coupling in coupling_stats]

    plt.plot(nbr_clusters, mean_coupling, color='#EDC192', label='Mean')
    plt.plot(nbr_clusters, min_coupling, color='#928E5E', label='Min')
    plt.plot(nbr_clusters, max_coupling, color='#80C2CE', label='Max')

    plt.xlabel('Number of clusters')
    plt.ylabel('Coupling')
    plt.title('Mean Coupling per Number of clusters')
    # plt.legend()
    # plt.grid(True)
    # plt.tight_layout()
    plt.savefig(codebasesPath + codebaseName + analyserPath + "/coupling.png", format="png", bbox_inches='tight')
    