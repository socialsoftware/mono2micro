import pandas as pd
import numpy as np
from datetime import datetime

from matplotlib import pyplot as plt

plt.style.use('ggplot')


def plot_complexity_reduction_per_merge_percentage(data):
    fig, ax = plt.subplots(1, 1, figsize=(4, 4))

    initial_sac_frc_sac = np.add(data.initial_frc_complexities, data.initial_sac_complexities)
    final_sac_frc_sac = np.add(data.final_frc_complexities, data.final_sac_complexities)

    sac_frc_reductions = []
    for index, fac in enumerate(initial_sac_frc_sac):
        reduction_percentage = (
                (initial_sac_frc_sac[index]-final_sac_frc_sac[index]) * 100
            )/initial_sac_frc_sac[index]
        sac_frc_reductions.append(reduction_percentage)

    sac_frc_reductions = np.array(sac_frc_reductions)

    best_x = np.array(data.merge_percentages)
    best_y = np.array(sac_frc_reductions)
    best_m, best_b = np.polyfit(best_x, best_y, 1)
    ax.plot(best_x, best_m*best_x + best_b, '--', color="red")

    ax.scatter(data.merge_percentages, sac_frc_reductions, s=6, color="cornflowerblue")

    ax.set_xlabel("Invocations merged %", fontsize=10)
    ax.set_ylabel("FRC+SAC % reduction", fontsize=10)

    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)

    ax.legend()
    ax.set_axisbelow(True)
    ax.grid(True)

    fig.tight_layout()

    plt.show()
