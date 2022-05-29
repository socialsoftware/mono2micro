from turtle import color
import numpy as np 
from matplotlib import pyplot as plt
import json

plt.style.use('seaborn-whitegrid')

def plotStats(codebasesPath, codebaseName, analysisType, strategy):

    analyserPath = "/analyser"
    if analysisType != None and len(analysisType) != 0:
        analyserPath += "/" + analysisType

    if strategy != None and len(strategy) != 0:
        analyserPath += "/" + strategy

    with open(codebasesPath + codebaseName + analyserPath + "/analysisStats.json") as f:
        analysis_stats = json.load(f)

    cohesion_stats_data    = [np.array(analysis_stats['cohesion'][i]['data']) for i in range(10)]
    complexity_stats_data  = [np.array(analysis_stats['complexity'][i]['data']) for i in range(10)]
    coupling_stats_data    = [np.array(analysis_stats['coupling'][i]['data']) for i in range(10)]
    performance_stats_data = [np.array(analysis_stats['performance'][i]['data']) for i in range(10)]

    beige_color  = dict(color='#EDC192')
    green_color  = dict(color='#928E5E')
    blue_color   = dict(color='#80C2CE')
    red_color    = dict(color='#E55D53')
    purple_color = dict(color='#BF80FF')

    # =============================================================================================================================
    plt.clf()

    bp = plt.boxplot(complexity_stats_data, meanline=True, showmeans=True, meanprops=blue_color, boxprops=beige_color, medianprops=purple_color, whiskerprops=beige_color)
    caps = bp['caps']
    for i in range(len(caps)):
        if (i % 2) == 0: caps[i].set(color=green_color['color']) # Low cap
        else: caps[i].set(color=red_color['color']) # High cap

    plt.xlabel('Number of clusters')
    plt.ylabel('Complexity')
    plt.title('Mean Complexity per Number of clusters')
    plt.xlim(0, 11)
    plt.ylim(0)
    plt.legend([bp['medians'][0], bp['means'][0]], ['Median', 'Mean'])
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(codebasesPath + codebaseName + analyserPath + "/complexity.png", format="png", bbox_inches='tight', marker='o')

    # =============================================================================================================================
    plt.clf()

    bp = plt.boxplot(performance_stats_data, meanline=True, showmeans=True, meanprops=blue_color, boxprops=beige_color, medianprops=purple_color, whiskerprops=beige_color)
    caps = bp['caps']
    for i in range(len(caps)):
        if (i % 2) == 0: caps[i].set(color=red_color['color']) # Low cap
        else: caps[i].set(color=green_color['color']) # High cap

    plt.xlabel('Number of clusters')
    plt.ylabel('Performance')
    plt.title('Performance per Number of clusters')
    plt.xlim(0, 11)
    plt.ylim(0)
    plt.legend([bp['medians'][0], bp['means'][0]], ['Median', 'Mean'])
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(codebasesPath + codebaseName + analyserPath + "/performance.png", format="png", bbox_inches='tight', marker='o')

    # =============================================================================================================================
    plt.clf()

    bp = plt.boxplot(cohesion_stats_data, meanline=True, showmeans=True, meanprops=blue_color, boxprops=beige_color, medianprops=purple_color, whiskerprops=beige_color)
    caps = bp['caps']
    for i in range(len(caps)):
        if (i % 2) == 0: caps[i].set(color=red_color['color']) # Low cap
        else: caps[i].set(color=green_color['color']) # High cap

    plt.xlabel('Number of clusters')
    plt.ylabel('Cohesion')
    plt.title('Cohesion per Number of clusters')
    plt.xlim(0, 11)
    plt.ylim(0, 1)
    plt.legend([bp['medians'][0], bp['means'][0]], ['Median', 'Mean'])
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(codebasesPath + codebaseName + analyserPath + "/cohesion.png", format="png", bbox_inches='tight', marker='o')

    # =============================================================================================================================
    plt.clf()

    bp = plt.boxplot(coupling_stats_data, meanline=True, showmeans=True, meanprops=blue_color, boxprops=beige_color, medianprops=purple_color, whiskerprops=beige_color)
    caps = bp['caps']
    for i in range(len(caps)):
        if (i % 2) == 0: caps[i].set(color=green_color['color']) # Low cap
        else: caps[i].set(color=red_color['color']) # High cap

    plt.xlabel('Number of clusters')
    plt.ylabel('Coupling')
    plt.title('Mean Coupling per Number of clusters')
    plt.xlim(0, 11)
    plt.ylim(0, 1)
    plt.legend([bp['medians'][0], bp['means'][0]], ['Median', 'Mean'])
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(codebasesPath + codebaseName + analyserPath + "/coupling.png", format="png", bbox_inches='tight', marker='o')
    