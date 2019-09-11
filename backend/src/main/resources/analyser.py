import requests
import time

start_time = time.time()

URL = 'http://localhost:8080/mono2micro/codebase/LdoD/analyser'

max_fscore = -1
max_params = []

interval = 10
n_clusters_range = range(3,15)

def sendRequest(a, w, r, s1, s2):
    global max_fscore, max_params, start_time
    a = a * 10
    w = w * 10
    r = r * 10
    s1 = s1 * 10
    s2 = s2 * 10
    for n_clusters in n_clusters_range:
        PARAMS = {'expertName': 'expertCut',
                    'accessMetricWeight': a,
                    'writeMetricWeight': w,
                    'readMetricWeight': r,
                    'sequenceMetric1Weight': s1,
                    'sequenceMetric2Weight': s2,
                    'numberClusters': n_clusters}
        request = requests.get(url = URL, params = PARAMS)
        data = request.json()
        if data > max_fscore:
            max_fscore = data
            max_params = [a,w,r,s1,s2,n_clusters]
        time.sleep(.1)

        if time.time() - start_time > 20:
            start_time = time.time()
            print('Current params: ' + str([a,w,r,s1,s2,n_clusters]))
            print('Max F-Score: ' + str(max_fscore))
            print('Max Params: ' + str(max_params))
            print('-----------------------------------------')


for a in range(interval, -1, -1):
    remainder = interval - a
    if remainder == 0:
        sendRequest(a, 0, 0, 0, 0)
    else:
        for w in range(remainder, -1, -1):
            remainder2 = remainder - w
            if remainder2 == 0:
                sendRequest(a, w, 0, 0, 0)
            else:
                for r in range(remainder2, -1, -1):
                    remainder3 = remainder2 - r
                    if remainder3 == 0:
                        sendRequest(a, w, r, 0, 0)
                    else:
                        for s1 in range(remainder3, -1, -1):
                            remainder4 = remainder3 - s1
                            if remainder4 == 0:
                                sendRequest(a, w, r, s1, 0)
                            else:
                                sendRequest(a, w, r, s1, remainder4)

print('Final Best F-Score:' + max_fscore)
print('Final Best Params:' + max_params)