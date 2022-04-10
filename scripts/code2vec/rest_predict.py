import traceback

import json
import os

import numpy as np
from scipy.cluster.hierarchy import dendrogram, linkage
from matplotlib import pyplot as plt

from dto.AttentionPath import AttentionPath
from dto.MethodPredict import MethodPredict
from dto.NamePrediction import NamePrediction

from .common import common
from .extractor import Extractor

SHOW_TOP_CONTEXTS = 10
MAX_PATH_LENGTH = 8
MAX_PATH_WIDTH = 2
JAR_PATH = 'code2vec/JavaExtractor/JPredict/target/JavaExtractor-0.0.1-SNAPSHOT.jar'

JSON_PATH = '../repos/answerservice.json'

class InteractivePredictor:
    exit_keywords = ['exit', 'quit', 'q']

    def __init__(self, config, model):
        model.predict([])
        self.model = model
        self.config = config
        self.path_extractor = Extractor(config,
                                        jar_path=JAR_PATH,
                                        max_path_length=MAX_PATH_LENGTH,
                                        max_path_width=MAX_PATH_WIDTH)

    def predict(self, original_name, snippet):

        try:
            print("[+] Extracting paths...")
            filename = 'tmp.java'
            with open(filename, 'w') as f:
                f.write(snippet)
                f.close()
            predict_lines, hash_to_string_dict = self.path_extractor.extract_paths(filename)
            os.remove(filename)

            raw_prediction_results = self.model.predict(predict_lines)
            method_prediction_results = common.parse_prediction_results(
                raw_prediction_results, hash_to_string_dict,
                self.model.vocabs.target_vocab.special_words, topk=SHOW_TOP_CONTEXTS)

            for raw_prediction, method_prediction in zip(raw_prediction_results, method_prediction_results):
                
                # print('[+] Original name:\t' + original_name)
                namePredictions = []
                for name_prob_pair in method_prediction.predictions:
                    # print('\t(%f) predicted: %s' % (name_prob_pair['probability'], name_prob_pair['name']))
                    namePredictions.append(
                        NamePrediction(
                            probability=name_prob_pair['probability'],
                            name=name_prob_pair['name']
                        )
                    )
                
                # print('[+] Attention:')
                attentionPaths = []
                for attention_obj in method_prediction.attention_paths:
                    # print('%f\tcontext: %s,%s,%s' % (
                    # attention_obj['score'], attention_obj['token1'], attention_obj['path'], attention_obj['token2']))
                    attentionPaths.append(
                        AttentionPath(
                            score=attention_obj['score'],
                            startToken=attention_obj['token1'],
                            path=attention_obj['path'],
                            endToken=attention_obj['token2']
                        )
                    )
                if self.config.EXPORT_CODE_VECTORS:
                    # print('[+] Code vector: ')
                    # print(' '.join(map(str, raw_prediction.code_vector)))
                    return MethodPredict(
                        name=original_name,
                        namePredictions=namePredictions,
                        attentionPaths=attentionPaths,
                        code_vector=raw_prediction.code_vector.tolist()
                    )

        except ValueError as e:
            print("[ - ] predict error: " + str(e))
            return None
