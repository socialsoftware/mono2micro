import requests
import time

start_time = time.time()

URL = 'http://localhost:8080/mono2micro/codebase/LdoD/analyser'

max_fscore = -1
max_params = []

a_range = range(0,101)
w_range = range(0,101)
r_range = range(0,101)
s1_range = range(0,101)
s2_range = range(0,101)
s3_range = range(0,101)
n_range = range(1,26)

def analyser():
    global max_fscore, max_params, start_time
    for a in a_range:
        for w in w_range:
            for r in r_range:
                for s1 in s1_range:
                    for s2 in s2_range:
                        for s3 in s3_range:
                            for n in n_range:
                                if a+w+r+s1+s2+s3 == 100:
                                    PARAMS = {'expertName': 'expertCut',
                                                'accessMetricWeight': a,
                                                'writeMetricWeight': w,
                                                'readMetricWeight': r,
                                                'sequenceMetric1Weight': s1,
                                                'sequenceMetric2Weight': s2,
                                                'sequenceMetric3Weight': s3,
                                                'numberClusters': n}
                                    request = requests.get(url = URL, params = PARAMS)
                                    data = request.json()
                                    if data > max_fscore:
                                        max_fscore = data
                                        max_params = [a,w,r,s1,s2,s3,n]
                                    time.sleep(.1)

                                    if time.time() - start_time > 20:
                                        start_time = time.time()
                                        print('Current params: ' + str([a,w,r,s1,s2,s3,n]))
                                        print('Max F-Score: ' + str(max_fscore))
                                        print('Max Params: ' + str(max_params))
                                        print('-----------------------------------------')
                                    
                                    

analyser()

print(max_fscore)
print(max_params)