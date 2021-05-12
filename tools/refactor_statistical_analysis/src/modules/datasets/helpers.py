from typing import List
import pandas as pd
import numpy as np


def read_dataset(path, rows, feature: str, codebase: str):
    dataset =  pd.read_csv(path, names=rows, skiprows=1)

    if feature:
        dataset = dataset.query(f'Feature == "{feature}"')

    elif codebase:
        dataset = dataset.query(f'Codebase == "{codebase}"')
    
    return dataset


def mean(values: List) -> float:
    data = np.array(values)
    return data.mean()


def stdev(values: List) -> float:
    data = np.array(values)
    return np.std(data)
