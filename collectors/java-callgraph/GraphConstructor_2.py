import codecs
import json
import flask
import glob
import matplotlib.pyplot as plt
import matplotlib.pylab as plab
import networkx as nx
import numpy as np
from networkx.drawing.nx_pydot import write_dot
from networkx.readwrite import json_graph
import collections
import webbrowser
import pickle


from sklearn import metrics
from ParserJavaCallgraph import get_dict_controller_classes_updatedfile
from ParserJavaCallgraph import get_all_controller_classes
#from Parser_2 import retrieve_domain_classes
from ParserJavaCallgraph import retrieve_domain_classes
from ParserJavaCallgraph import get_dict_controller_classes
from ParserJavaCallgraph import get_abstract_classes
from ParserJavaCallgraph import get_list_of_controllers

from scipy.cluster import hierarchy
from scipy.spatial import distance

list_of_graphs = []
final_graph = []
list_colors = []

#Static dictionary with manually retrieven data
dict_blended_controller_class_data = dict()
dict_ldod_controller_class_data = dict()

# Dictionary that saves the cluster and containing classes after a cut in the dendrogram
dict_class_cluster = dict()

# Dictionary that saves the percentage of clusters accessed by each controller
dict_cluster_controller_access = dict()

# Dictionary that saves the percentage of classes accessed inside each cluster
dict_ctrl_class_percentage = dict()

# Dictionary for class and controller statistic
dict_class_controller_statistic = dict()


options = {
    'node_size': 300,
    'with_labels': True,
    'font_weight':'bold',
}

def get_list_of_controllers_path(path):
    return glob.glob(path + "/*_ESTest_scaffolding.java")

def get_path_callgraph_file():
    return glob.glob("/home/luis_nunes/PycharmProjects/Tese/outputFinalLdod.txt")
    #return glob.glob("/home/luis_nunes/PycharmProjects/Tese/outputFinalBlended.txt")


def get_list_of_controllers_evosuite():
    # Evosuite Files - not used
    return glob.glob("/home/luis_nunes/PycharmProjects/Tese/Blended_workflow/2/*_ESTest_scaffolding.java")
    #return glob.glob("/home/luis_nunes/PycharmProjects/Tese/2nditeration_java_files_LDOD/*_ESTest_scaffolding.java")

def numpy_array_to_csv(array):
    np.savetxt('similarity_matrix_2.csv', array, delimiter=',')


def internal_evaluation(hierarc, sim_matrix, dict_cluster_controllers):
    number_singleton = 0
    number_average = 0
    number_max = 0
    silhouette_score = 0

    number_clusters_cut = len(dict_cluster_controllers)

    for key, value in dict_class_cluster.items():
        print(key, value, len(value))
        if len(value) == 1:
            number_singleton += 1
        if len(value) > number_max:
            number_max = len(value)
        number_average += len(value)

    number_average /= len(dict_class_cluster.keys())

    print('Singletons: ' + number_singleton.__str__())
    print('Maximum: ' + number_max.__str__())
    print('Average: ' + number_average.__str__())

    nodes = hierarchy.fcluster(hierarc, number_clusters_cut, criterion="maxclust")

    print(nodes)

    print(metrics.silhouette_score(sim_matrix, nodes))

def possible_pairs_from_list(source):
    result = []
    for p1 in range(len(source)):
        for p2 in range(p1 + 1, len(source)):
            result.append([source[p1], source[p2]])
    return result


def pairwise_evaluation(expert_dict, struct101):
    print('###Pairwise Evaluation###')
    common_pairs = 0
    all_pairs_clustering = 0
    all_pairs_expert = 0

    # Possible pairs from expert
    for key2, expertcluster in expert_dict.items():
        possible_pairs_expert = possible_pairs_from_list(expertcluster)
        all_pairs_expert = all_pairs_expert + len(possible_pairs_expert)

    for key1, dendcluster in dict_class_cluster.items():
        possible_pairs = possible_pairs_from_list(dendcluster)
        all_pairs_clustering = all_pairs_clustering + len(possible_pairs)
        for key2, expertcluster in expert_dict.items():
            for pair in possible_pairs:
                if set(pair).issubset(expertcluster):
                    common_pairs = common_pairs + 1

    print("TRANSACTIONAL")
    print("common pairs:")
    print(common_pairs)
    print("all pairs clustering:")
    print(all_pairs_clustering)
    print("all pairs expert:")
    print(all_pairs_expert)

    common_pairs = 0
    all_pairs_clustering = 0


    for key1, s101cluster in struct101.items():
        possible_pairs = possible_pairs_from_list(s101cluster)
        print(possible_pairs)
        all_pairs_clustering = all_pairs_clustering + len(possible_pairs)
        for key2, expertcluster in expert_dict.items():
            for pair in possible_pairs:
                if set(pair).issubset(expertcluster):
                    common_pairs = common_pairs + 1

    print("STRUCT101")
    print("common pairs:")
    print(common_pairs)
    print("all pairs clustering:")
    print(all_pairs_clustering)
    print("all pairs expert:")
    print(all_pairs_expert)


def external_evaluation_blended(hierarc, sim_matrix):
    print('###External Evaluation - BLENDED###')
    TP = 0
    expert_dict_blended = {
        0: ['BlendedWorkflow', 'Specification', 'DataModel', 'Path', 'Attribute', 'Product', 'RelationBW',
            'Dependence', 'Entity', 'Rule', 'Cardinality', 'Condition', 'AndCondition', 'OrCondition', 'NotCondition',
            'MulCondition',
            'Comparison', 'AttributeBoolCondition', 'TrueCondition', 'FalseCondition', 'BoolComparison',
            'AttributeBoolCondition', 'Expression', 'BinaryExpression', 'NumberLiteral', 'StringLiteral',
            'AttributeValueExpression'],
        1: ['BlendedWorkflow', 'ConditionModel', 'ActivityModel', 'GoalModel', 'Goal', 'Activity',
            'DefProductCondition', 'DefEntityCondition', 'DefAttributeCondition', 'DefPathCondition'],
        2: ['BlendedWorkflow', 'ProductInstance', 'EntityInstance', 'AttributeInstance', 'RelationInstance', 'WorkItem',
            'ActivityWorkItem',
            'GoalWorkItem', 'WorkItemArgument', 'PreWorkItemArgument', 'PostWorkItemArgument', 'WorkflowInstance']}
    struct101_dict_blended = {0: ['BlendedWorkflow'],
                              1: ['AttributeBoolCondition', 'AttributeValueExpression'],
                              2: ['BinaryExpression', 'AndCondition', 'AttributeInstance', 'BoolComparison',
                                  'FalseCondition',
                                  'NumberLiteral', 'EntityInstance', 'Comparison', 'NotCondition', 'OrCondition',
                                  'Expression',
                                  'ProductInstance', 'StringLiteral', 'RelationInstance', 'WorkflowInstance',
                                  'TrueCondition',
                                  'PostWorkItemArgument', 'WorkItem'],
                              3: ['ActivityWorkItem', 'GoalWorkItem', 'PreWorkItemArgument',
                                  'WorkItemArgument'],
                              4: ['Activity', 'ActivityModel', 'GoalModel', 'Goal', 'Rule', 'ConditionModel',
                                  'Condition', 'DefAttributeCondition', 'DefEntityCondition', 'MulCondition'],
                              5: ['Cardinality',
                                  'DefPathCondition', 'Dependence', 'RelationBW', 'Path', 'Specification', 'DataModel',
                                  'Entity',
                                  'Attribute', 'Product', 'DefProductCondition']}

    aux_list_indexes = []

    for key1, dendcluster in dict_class_cluster.items():
        for key2, expertcluster in expert_dict_blended.items():
            # print(dendcluster, expertcluster)
            if set(dendcluster).issubset(expertcluster):
                TP = TP + 1
                aux_list_indexes.append(key1)

    print('###########DEND TO EXPERT - BLENDED')
    print('Precision: ' + TP.__str__() + ' / ' + len(dict_class_cluster.items()).__str__())
    print('Clusters not in expert decomposition:')
    for key, value in dict_class_cluster.items():
        if key.item() in aux_list_indexes:
            continue
        else:
            print(value)

    TP = 0
    aux_list_indexes = []

    for key1, dendcluster in struct101_dict_blended.items():
        for key2, expertcluster in expert_dict_blended.items():
            # print(dendcluster, expertcluster)
            if set(dendcluster).issubset(expertcluster):
                TP = TP + 1
                aux_list_indexes.append(key1)

    print('###########STRUCT TO EXPERT - BLENDED')
    print('Precision: ' + TP.__str__() + ' / ' + len(struct101_dict_blended.items()).__str__())
    print('Clusters not in expert decomposition:')
    for key, value in struct101_dict_blended.items():
        if key in aux_list_indexes:
            continue
        else:
            print(value)

    pairwise_evaluation(expert_dict_blended, struct101_dict_blended)


def external_evaluation_ldod(hierarc, sim_matrix):
    print('###External Evaluation -  LDOD###')
    TP = 0

    expert_dict_ldod = {0:['LdoD', 'UserConnection', 'Role', 'LdoDUser', 'RegistrationToken'], 1:['LdoD','Member', 'RecommendationWeights',
                        'VirtualEdition', 'Taxonomy', 'Category', 'Tag', 'Section', 'VirtualEditionInter', 'Annotation', 'Range'],
                        2:['LdoD','Edition', 'ExpertEdition', 'NullEdition', 'Fragment', 'FragInter', 'ExpertEditionInter',
                           'Heteronym', 'NullHeteronym', 'AnnexNote', 'SourceInter', 'LdoDDate', 'Source', 'PrintedSource',
                           'Facsimile', 'Surface', 'ManuscriptSource', 'Dimensions', 'PhysNote', 'HandNote', 'TypeNote',
                           'TextPortion', 'Rend', 'RdgText', 'RdgGrpText', 'AppText', 'AltText', 'AltTextWeight', 'PbText',
                           'LbText', 'SimpleText', 'SegText', 'SpaceText', 'ParagraphText', 'AddText', 'DelText', 'SubstText',
                           'GapText', 'UnclearText', 'NoteText', 'RefText', 'GraphElement']}
    struct101_dict_ldod = {0:['AppText', 'AnnexNote', 'Dimensions', 'ExpertEdition', 'ExpertEditionInter', 'HandNote',
                              'LbText', 'RdgText', 'RefText', 'NoteText', 'PrintedSource', 'RecommendationWeights',
                              'RdgGrpText', 'Section', 'SourceInter'], 1:['DelText'], 2:['GapText'], 3:['UnclearText'],
                           4:['SpaceText'], 5:['AddText'], 6:['AdHocCategory', 'Annotation', 'LdoD', 'Taxonomy',
                            'VirtualEdition', 'VirtualEditionInter', 'NullHeteronym'], 7:['SegText', 'AltText', 'AltTextWeight'],
                           8:['ParagraphText', 'SubstText', 'PbText', 'PhysNote', 'Rend', 'TextPortion', 'TypeNote',
                              'ManuScriptSource', 'SimpleText'], 9:['Range'], 10:['ExpertEdition', 'Fragment', 'Heteronym',
                            'FragInter', 'Category', 'LdoDDate', 'Tag', 'LdoDUser', 'Source', 'Surface', 'Edition',
                            'Facsimile', 'Member', 'RegistrationToken', 'Role', 'UserConnection']}

    for key,value in dict_class_cluster.items():
        print(key,value)

    # This list saves the indexes of the clusters that are considered True Positives
    aux_list_indexes = []

    for key1, dendcluster in dict_class_cluster.items():
        for key2, expertcluster in expert_dict_ldod.items():
            # print(dendcluster, expertcluster)
            if set(dendcluster).issubset(expertcluster):
                TP = TP + 1
                aux_list_indexes.append(key1)

    print('###########DEND TO EXPERT - LDOD')
    print('Precision: ' + TP.__str__() + ' / ' + len(dict_class_cluster.items()).__str__())
    print('Clusters not in expert decomposition:')
    for key, value in dict_class_cluster.items():
        if key.item() in aux_list_indexes:
            continue
        else:
            print(value)

    TP = 0
    aux_list_indexes = []

    for key1, dendcluster in struct101_dict_ldod.items():
        for key2, expertcluster in expert_dict_ldod.items():
            # print(dendcluster, expertcluster)
            if set(dendcluster).issubset(expertcluster):
                TP = TP + 1
                aux_list_indexes.append(key1)

    print('###########STRUCT TO EXPERT - LDOD')
    print('Precision: ' + TP.__str__() + ' / ' + len(struct101_dict_ldod.items()).__str__())
    print('Clusters not in expert decomposition:')
    for key, value in struct101_dict_ldod.items():
        if key in aux_list_indexes:
            continue
        else:
            print(value)

    pairwise_evaluation(expert_dict_ldod, struct101_dict_ldod)

def calculate_cluster_controller_access(linkage_type):
    f = open('statistics_2_' + linkage_type + '.txt', 'a')
    print("---Cluster Percentage-------------", file=f)
    dict_ctrl_class = get_dict_controller_classes_updatedfile()

    for ctrl, _classes in dict_ctrl_class.items():
        set_classes = list(set(_classes))
        for cluster, _classes2 in dict_class_cluster.items():
            for _class in set_classes:
                if _class in _classes2:
                    dict_cluster_controller_access.setdefault(cluster, []).append(ctrl)
                    break

    ordered_dict_cluster_controller_access = collections.OrderedDict(sorted(dict_cluster_controller_access.items()))

    for cluster, controllers in ordered_dict_cluster_controller_access.items():
        print("Cluster ", end=" ", file=f)
        print(cluster, end=" ", file=f)
        print("-- Controllers accessed: " + sorted(controllers).__str__(), end=" ", file=f)
        # print("-- " + len(controllers).__str__() + " in " + len(dict_class_cluster.values()).__str__() + " -- " +
        #      ((float(len(controllers)) / float(len(dict_class_cluster.values))) * 100).__str__() + "%", file=f)
        print(file=f)
        print("---------------------------------", file=f)
    f.close()


def calculate_controller_percentage_classes(linkage_type):
    f = open('statistics_2_' + linkage_type + '.txt', 'a')
    print("-----Classes Percentage-----", file=f)
    dict_ctrl_class = get_dict_controller_classes_updatedfile()
    ordered_dict_ctrl_class = collections.OrderedDict(sorted(dict_ctrl_class.items()))
    class_counter = 0

    for ctrl, _classes in ordered_dict_ctrl_class.items():
        # To remove duplicates
        set_classes = list(set(_classes))
        print(ctrl + ":", file=f)
        for cluster, _classes2 in dict_class_cluster.items():
            for _class in set_classes:
                if _class in _classes2:
                    class_counter += 1
            print("Cluster: " + cluster.item(0).__str__() + " --", end=" ",file=f)
            print(float(class_counter).__str__() + " of " + float(len(_classes2)).__str__() + " -- " +
                  ((float(class_counter)/float(len(_classes2)))*100).__str__() + "%", file=f)
            class_counter = 0
    f.close()


def get_number_controllers_two_classes(class_1, class_2):
    # Compares the adjacency list of both classes to the number of controllers in common between them
    number_common_controllers = 0
    controllers_class1 = final_graph[len(final_graph)-1][class_1]
    controllers_class2 = final_graph[len(final_graph)-1][class_2]
    #print(controllers_class1)
    #print(controllers_class2)
    for key1 in controllers_class1.keys():
        for key2 in controllers_class2.keys():
            if(key1 == key2):
                number_common_controllers += 1
    return number_common_controllers


def get_number_controllers_one_class(class_1):
    # returns the adjancency list of the nodes, being that the length of that list corresponds
    # to the number of controllers associated with it

    return len(final_graph[len(final_graph)-1][class_1])


def create_similarity_matrix():
    print('Creating Similarity Matrix...')

    np.set_printoptions(threshold=np.inf, linewidth=1000, precision=3)
    list_unique_classes = sorted(list(get_all_controller_classes()))

    print(list_unique_classes)

    similarity_matrix = np.zeros((len(list_unique_classes), len(list_unique_classes)))
    for index, value in np.ndenumerate(similarity_matrix):
        # Initialize diagonal with ones
        if len(set(index)) == 1:
            similarity_matrix[index] = 1
        else:
        # index[0] represents class C1 and index[1] represents class C2
            #print(list_unique_classes[index[0]], list_unique_classes[index[1]])

            number_controllers_c1c2 = get_number_controllers_two_classes(list_unique_classes[index[0]], list_unique_classes[index[1]])
            number_controllers_c1 = get_number_controllers_one_class(list_unique_classes[index[0]])

            #print(number_controllers_c1c2)
            #print(number_controllers_c1)

            similarity_measure = number_controllers_c1c2/number_controllers_c1
            #print(similarity_measure)
            similarity_matrix[index] = similarity_measure

    # UNCOMMENT TO PRINT SIMILARITY MATRIX
    # print(similarity_matrix)
    print('Number of unique classes=' + len(list_unique_classes).__str__())
    #numpy_array_to_csv(similarity_matrix)

    return similarity_matrix


def join_graphs():
    G = nx.compose_all(list_of_graphs)
    final_graph.append(G)


def draw_graph_dot():
    #nx.draw(final_graph[len(final_graph)-1], **options)
    #plt.show()
    pos = nx.nx_agraph.graphviz_layout(final_graph[len(final_graph)-1])
    nx.draw(final_graph[len(final_graph)-1], pos=pos)
    write_dot(final_graph[len(final_graph)-1], 'pygraph.dot')


def draw_graph_matplot():

    # Draw with labels using defined options
    #nx.draw(final_graph[len(final_graph) - 1], **options)

    pos = nx.nx_agraph.graphviz_layout(final_graph[len(final_graph) - 1])
    G = final_graph[len(final_graph)-1]
    for (node, data) in G.nodes(data=True):
        if data['is_ctrl'] == 'yes':
            list_colors.append('blue')
        else:
            list_colors.append('red')
    nx.draw(G, pos=pos, node_color=list_colors)
    plt.show()


def graph_construction(controller, classes):
    # Adds controller node and subsequent edges to nodes of corresponding classes

    G = nx.Graph()
    G.add_node(controller, is_ctrl='yes')
    for domain_class in classes:
        G.add_node(domain_class, is_ctrl='no')
        G.add_edge(controller, domain_class)

    list_of_graphs.append(G)

    #plt.savefig("/home/luis_nunes/PycharmProjects/test.png")


def hierarchical_clustering_average(similarity_matrix, linkage_type):
    """
    Hierarchical Clustering with custom distance
    :return dendrogram from the hierarchical clustering
    :param similarity_matrix - matrix with weight function between classes
    :param linkage_type - string representing the type of linkage to be applied
    """
    if linkage_type == 'average':
        hierarc = hierarchy.average(similarity_matrix)
    elif linkage_type == 'single':
        hierarc = hierarchy.single(similarity_matrix)
    elif linkage_type == 'complete':
        hierarc = hierarchy.complete(similarity_matrix)



    hierarchy.dendrogram(hierarc, labels=sorted(list(get_all_controller_classes())),
                         distance_sort='descending')

    # Uncomment to see image instead of saving
    #plt.show()

    ###################################################
    # Uncomment to save the .png file of the dendrogram

    plab.savefig("dendrogram_" + linkage_type + "_2.png", format="png", bbox_inches='tight')

    # Closes the open pyplot windows so the dendrograms can be redrawn
    plt.close('all')

    return hierarc, linkage_type

def cut_dendrogram(hierarc, linkage_type, height):
    # EMPTY DICTIONARIES FOR REPEATED CUTS
    dict_class_cluster.clear()
    dict_cluster_controller_access.clear()
    dict_ctrl_class_percentage.clear()

    cut = hierarchy.cut_tree(hierarc, height=height)

    list_unique_classes = sorted(list(get_all_controller_classes()))

    for i in range(0, len(list_unique_classes)):
        dict_class_cluster.setdefault(cut[i][0], []).append(list_unique_classes[i])

    ###################################################
    # Statistics generation
    f = open('statistics_2_' + linkage_type + '.txt', 'w')
    print("-----Clusters from Dendrogram-----", file=f)

    for key, value in dict_class_cluster.items():
        print(key, value, file=f)
    f.close()
    # Calculate percentages
    calculate_cluster_controller_access(linkage_type)
    calculate_controller_percentage_classes(linkage_type)

    return dict_cluster_controller_access

def build_cluster_controller_graph(dict_clusters):
    '''
    Constructs a graph where the nodes represents the clusters of the cut.
    The attribute of each node is the controllers that access these clusters.
    The attribute of the edges between nodes represent the controllers that access both clusters.
    '''

    graph_clusters = nx.Graph()
    #Add attributes to the nodes of the graph
    # ctrls: attribute that contains the number of controllers
    # namectrls: attribute that contains the name of the controllers
    # domain: attribute that contains the domain classes of the controllers
    for cluster, controllers in dict_clusters.items():
        graph_clusters.add_node(cluster, ctrls='Cluster' + cluster.__str__() + ':' + len(controllers).__str__(),
                                domain=dict_class_cluster.get(cluster), namectrls=dict_clusters.get(cluster))

    for cluster_1, controllers_1 in dict_clusters.items():
        for cluster_2, controllers_2 in dict_clusters.items():
            # Does not add edge if it already exists
            if(graph_clusters.has_edge(cluster_1, cluster_2)):
                break
            else:
                # Does not compare the cluster to itself
                if (int(cluster_1.__str__()) == int(cluster_2.__str__())):
                    break
                #Get the common controllers between clusters
                common_controllers = set(controllers_1) & set(controllers_2)

                #Get the difference of controllers between clusters
                diff_controllers = set(controllers_1).symmetric_difference(set(controllers_2));

                graph_clusters.add_edge(cluster_1, cluster_2, ctrls=len(common_controllers),
                                        commonctrls=common_controllers, diffctrls=diff_controllers)

    labels_nodes = nx.get_node_attributes(graph_clusters, 'ctrls')
    labels_edges = nx.get_edge_attributes(graph_clusters, 'ctrls')

    ########################################
    # DEFAULT MATPLOTLIB
    pos = nx.spring_layout(graph_clusters)
    nx.draw(graph_clusters, pos)
    nx.draw_networkx_labels(graph_clusters, pos=pos, labels=labels_nodes)
    nx.draw_networkx_edge_labels(graph_clusters, pos=pos, labels=labels_edges)


    # Export data to .JSON
    json_data = json_graph.node_link_data(graph_clusters)

    plab.savefig("graph.png", format="png", bbox_inches='tight')

    plt.close('all')

    return json_data

    #plt.show()

def build_cluster_controller_graph_2_arguments(dict_clusters, class_clusters):
    '''
    Constructs a graph where the nodes represents the clusters of the cut.
    The attributes of each node are the controllers that access these clusters and their domain classes.
    The attribute of the edges between nodes represent the controllers that access both clusters.
    '''
    graph_clusters = nx.Graph()
    #Add attributes to the nodes of the graph
    # ctrls: attribute that contains the number of controllers
    # namectrls: attribute that contains the name of the controllers
    # domain: attribute that contains the domain classes of the controllers
    for cluster, controllers in dict_clusters.items():
        graph_clusters.add_node(cluster, ctrls='Cluster' + cluster.__str__() + ':' + len(controllers).__str__(),
                                domain=class_clusters.get(cluster), namectrls=dict_clusters.get(cluster))
    for cluster_1, controllers_1 in dict_clusters.items():
        for cluster_2, controllers_2 in dict_clusters.items():
            # Does not add edge if it already exists
            if(graph_clusters.has_edge(cluster_1, cluster_2)):
                break
            else:
                # Does not compare the cluster to itself
                if (int(cluster_1.__str__()) == int(cluster_2.__str__())):
                    break
                common_controllers = set(controllers_1) & set(controllers_2)
                graph_clusters.add_edge(cluster_1, cluster_2, ctrls=len(common_controllers),
                                        commonctrls=common_controllers)
    labels_nodes = nx.get_node_attributes(graph_clusters, 'ctrls')
    labels_edges = nx.get_edge_attributes(graph_clusters, 'ctrls')

    # DEFAULT MATPLOTLIB
    pos = nx.spring_layout(graph_clusters)
    nx.draw(graph_clusters, pos)
    nx.draw_networkx_labels(graph_clusters, pos=pos, labels=labels_nodes)
    nx.draw_networkx_edge_labels(graph_clusters, pos=pos, labels=labels_edges)
    # Export data to .JSON
    json_data = json_graph.node_link_data(graph_clusters)
    plab.savefig("graph.png", format="png", bbox_inches='tight')
    plt.close('all')
    return json_data


def jsonify(data):
    json_data = dict()
    for key, value in data.items():
        if isinstance(value, list): # for lists
            value = [jsonify(item) if isinstance(item, dict) else item for item in value]
        if isinstance(value, set): # for lists
            value = [jsonify(item) if isinstance(item, dict) else item for item in value]
        if isinstance(value, dict): # for nested lists
            value = jsonify(value)
        if isinstance(key, int): # if key is integer: > to string
            key = str(key)
        if type(value).__module__=='numpy': # if value is numpy.*: > to python list
            value = value.tolist()
        json_data[key] = value
    return json_data

def write_json(json_data, name):
    new_json_data = jsonify(json_data)

    file_path = 'javascript/' + name + '.json'

    json.dump(new_json_data, codecs.open(file_path, 'w', encoding='utf-8'), separators=(',', ':'), indent=4)

def get_class_controllers():
    G = final_graph[len(final_graph) - 1]
    for (node, data) in G.nodes(data=True):
        # if it is not a controller the node is a domain class
        if data['is_ctrl'] == 'no':
            for adj in G.neighbors(node):
                dict_class_controller_statistic.setdefault(node, []).append(adj)

    ordered_dict_class_controller_statistic = collections.OrderedDict(sorted(dict_class_controller_statistic.items()))

    aux_ordered_dict = dict()

    for key,value in ordered_dict_class_controller_statistic.items():
        aux_ordered_dict[key]=sorted(value)

    write_json(aux_ordered_dict, 'class_controller_statistic')


if __name__ == '__main__':

    get_abstract_classes(get_path_callgraph_file()[0])


    ###JAVA CALLGRAPH INFORMATION RETRIEVAL
    for controller in get_list_of_controllers():
        domain_classes = retrieve_domain_classes(controller, get_path_callgraph_file()[0])
        print('Controller:' + controller.split('/')[-1][0:-5])
        print('Domain_classes:' + domain_classes.__str__())
        print(len(domain_classes))
        #print('\n')
        #for classes in domain_classes:
            #print(classes)
        ##if len(domain_classes) > 0:
            ##graph_construction(controller.split('/')[-1][0:-24], domain_classes)

    export_dict = get_dict_controller_classes()

    #Serialize dictionary into file
    with open('controller_classes_dict.txt', 'w') as file:
        file.write(json.dumps(export_dict, indent = 4))
        file.close()

    #Deserialize file into dictionary
    #with open('controller_classes_dict.txt', 'r') as file:
    #    dict = json.load(file)
    #
    #   for key,value in dict.items():
    #        print(key)
    ##join_graphs()
    ##get_class_controllers()
    ##sim_matrix = create_similarity_matrix()

    # Uncomment to draw the graphs
    #draw_graph_matplot()

    # Hierarchical clustering with 3 types of linkage execution
    ##dend, linkage_type = hierarchical_clustering_average(sim_matrix, "average")
    #hierarchical_clustering_average(sim_matrix, "single")
    #hierarchical_clustering_average(sim_matrix, "complete")

    # Cut dendrogram to retrieve clusters and relevant statistics
    ##dict_cluster_controller_aux = cut_dendrogram(dend, linkage_type, 0.8)

    # Uncomment to perform internal evaluation
    #internal_evaluation(dend, sim_matrix)
    #external_evaluation(dend, sim_matrix)

    ##json_data_graph = build_cluster_controller_graph(dict_cluster_controller_aux)


    # TIRAR DE COMMENT
    #Translate the graph info to a .json file
    #write_json(json_data_graph, 'force')

    # Run locally with firefox
    #webbrowser.get('firefox').open_new_tab('file:///home/luis_nunes/PycharmProjects/Tese/javascript/index.html')

    #webbrowser.get('firefox').open_new_tab('file:///home/luis_nunes/PycharmProjects/Tese/javascript/class_controller_statistic.html')



