from matplotlib import pyplot as plt
import seaborn as sns

from ...configuration.extraction import Extraction



def plot_metric_correlation(config: Extraction):
    plt.style.use('ggplot')

    corr = config.complexities_dataset.corr()

    sns.heatmap(corr, 
                xticklabels=corr.columns.values,
                yticklabels=corr.columns.values)

    plt.show()
