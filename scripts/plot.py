from matplotlib import pyplot as plt

nbr_clusters = [1, 2, 3, 4, 5]

mean_sa_complexity = []
mean_ea_complexity = []
mean_ca_complexity = []
mean_feta_complexity = []
mean_fmca_complexity = []

min_sa_complexity = []
min_ea_complexity = []
min_ca_complexity = []
min_feta_complexity = []
min_fmca_complexity = []

max_sa_complexity = []
max_ea_complexity = []
max_ca_complexity = []
max_feta_complexity = []
max_fmca_complexity = []

plt.plot(nbr_clusters, mean_sa_complexity)
plt.plot(nbr_clusters, mean_ea_complexity)
plt.plot(nbr_clusters, mean_ca_complexity)
plt.plot(nbr_clusters, mean_feta_complexity)
plt.plot(nbr_clusters, mean_fmca_complexity)

plt.xlabel('Number of clusters')
plt.ylabel('Complexity')
plt.title('Mean Complexity per Number of clusters')

