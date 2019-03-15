from tkinter import *
from tkinter.filedialog import askdirectory, askopenfilename
from tkinter.filedialog import asksaveasfile
import webbrowser
import os
import numpy as np


#from Parser_2 import *
from GraphConstructor_2 import *
from ParserJavaCallgraph import write_file_path

sim_matrix_global = None
dend_global = None
dict_current_clusters = dict()

# UNDO variables
dict_previous_clusters = dict()
dict_previous_class_cluster = dict()


# Button behavior functions


#def exportdml():
#    print('dml')

def undofunc():

    #Clear List and update to previous state
    clusterlist.delete(0, END)

    global dict_current_clusters

    dict_current_clusters = dict_previous_clusters.copy()
    for key,value in dict_current_clusters.items():
        clusterlist.insert(END, 'Cluster ' + np.array2string(key))

    #Return domain to previous state
    global dict_class_cluster

    dict_class_cluster = dict_previous_class_cluster.copy()

    for key, value in dict_class_cluster.items():
        print(key, value)



def exportjson():
    json_graph = json.load(open('javascript/force.json'))

    node_info = json_graph["nodes"]

    f = asksaveasfile(mode='w', defaultextension=".json")
    if f is None:  # asksaveasfile return `None` if dialog closed with "cancel".
        return
    text2save = json.dumps(node_info, sort_keys=True, indent=4, separators=(',', ': '))
    f.write(text2save)
    f.close()


def joinfunc():
    aux_controller_list = []
    aux_domain_list = []

    global dict_current_clusters
    global dict_class_cluster

    print(dict_class_cluster)
    # Copy the current dictionary so we can undo the changes
    global dict_previous_clusters
    dict_previous_clusters = dict_current_clusters.copy()

    global dict_previous_class_cluster
    dict_previous_class_cluster = dict_class_cluster.copy()

    selected_clusters = clusterlist.curselection()

    maximum_key = max(dict_current_clusters, key=int) + 1

    # print(selected_clusters)

    for selected_index in selected_clusters:
        # Get cluster number
        real_index = int(clusterlist.get(selected_index).split(' ')[-1])
        aux_controller_list.extend(dict_current_clusters[real_index])
        aux_domain_list.extend(dict_class_cluster[real_index])

        # delete entries from controller dictionary
        del dict_current_clusters[real_index]
        # delete entries from domain dictionary
        del dict_class_cluster[real_index]

    for selected_index in selected_clusters[::-1]:
        # delete entries from listbox, starting from the last index so there are no conflicts
        clusterlist.delete(selected_index)

    # Delete duplicate controllers
    no_duplicates_cluster_list = list(set(aux_controller_list))

    # Insert new entry to list and dictionaries
    dict_current_clusters[maximum_key] = no_duplicates_cluster_list

    # Join the domain
    dict_class_cluster[maximum_key] = aux_domain_list
    clusterlist.insert(END, 'Cluster ' + np.array2string(maximum_key))

    json_data_graph = build_cluster_controller_graph_2_arguments(dict_current_clusters, dict_class_cluster)

    # Translate the graph info to a .json file
    write_json(json_data_graph, 'force')

    # Only works locally with FIREFOX
    webbrowser.get('firefox').open_new_tab('file:///home/luis_nunes/PycharmProjects/Tese/javascript/index.html')


def cutfunc():
    # CLEAR LIST
    clusterlist.delete(0, END)

    cutvalue = entrycut.get()
    # print("cut value:" + cutvalue)

    dict_cluster_controller_aux = cut_dendrogram(dend_global, linkage.get(), float(cutvalue))

    print("Dictionary from Cut:")
    print(dict_cluster_controller_aux)

    ordered_dict = collections.OrderedDict(sorted(dict_cluster_controller_aux.items()))

    global dict_current_clusters
    dict_current_clusters = ordered_dict

    #POPULATE LISTBOX
    for key, value in ordered_dict.items():
        clusterlist.insert(END, 'Cluster ' + np.array2string(key))

    ### EVALUATION ###
    dend = dend_global
    sim_matrix = sim_matrix_global

    #internal_evaluation(dend, sim_matrix, dict_cluster_controller_aux)
    #external_evaluation_ldod(dend, sim_matrix)
    external_evaluation_blended(dend, sim_matrix)

    json_data_graph = build_cluster_controller_graph(dict_cluster_controller_aux)

    # Translate the graph info to a .json file
    write_json(json_data_graph, 'force')

    #Only works locally with FIREFOX
    webbrowser.get('firefox').open_new_tab('file:///home/luis_nunes/PycharmProjects/Tese/javascript/class_controller_statistic.html')
    webbrowser.get('firefox').open_new_tab('file:///home/luis_nunes/PycharmProjects/Tese/javascript/index.html')


def browsefunc():
    filename = askopenfilename(filetypes=[("Text files", "*.txt")])
    filepath.config(text=filename)

def dendrograms():
    write_file_path(filepath.cget("text").split('/')[-1])

    with open(filepath.cget("text").split('/')[-1], 'r') as file:
        dict_controller_classes = json.load(file)

        for controller, classes in dict_controller_classes.items():
            graph_construction(controller, classes)

    join_graphs()
    get_class_controllers()
    sim_matrix = create_similarity_matrix()

    #draw_graph_matplot()

    # Get clustering linkage type
    linkage_type = linkage.get()

    # Creates the dendrogram and outputs it as a png file
    dend, linkage_type = hierarchical_clustering_average(sim_matrix, linkage_type)

    global dend_global
    dend_global = dend
    global sim_matrix_global
    sim_matrix_global = sim_matrix

    # Converts to the format necessary for showing in tkinter
    dendrogram_png = PhotoImage(file="dendrogram_" + linkage_type +"_2.png")
    dendimage.configure(image=dendrogram_png)
    dendimage.image = dendrogram_png

    cutframe.pack(padx=(0, 10))


if __name__ == '__main__':
    window = Tk()
    window.title("Monolithic Cluster Discovery Tool")
    window.geometry('900x768')

    textIntro = Label(window, text="This tool uses Java-Callgraph Static Analysis files to rediscover the modularity of an application", font=("Arial Bold", 13), bg="blue", fg="white")
    textIntro.pack(fill=X)

    text1 = Label(window, text="File containing a Python dictionary of Controller-Classes:")
    text1.pack(pady=10)

    browsebutton = Button(window, text="Browse", command=browsefunc)
    browsebutton.pack()

    filepath = Label(window, bg="white")
    filepath.pack(pady=5)

    buttonframe = Frame(window)
    buttonframe.pack(side=TOP, fill=BOTH)

    dendbutton = Button(buttonframe, text="Generate Dendrogram", command=dendrograms)
    dendbutton.pack(side=LEFT, pady=20, padx=(250,10))

    linkage = StringVar()

    av = Radiobutton(buttonframe, text="Average", variable=linkage, value="average")
    av.pack(side=LEFT)
    si = Radiobutton(buttonframe, text="Single", variable=linkage, value="single")
    si.pack(side=LEFT)
    co = Radiobutton(buttonframe, text="Complete", variable=linkage, value="complete")
    co.pack(side=LEFT)

    imageframe = Frame(window)
    imageframe.pack(side=TOP, fill=BOTH)

    dendimage = Label(imageframe)
    dendimage.pack(side=LEFT, padx=(0, 10))

    cutframe = Frame(imageframe)
    cutframe.pack(side=LEFT, fill=Y, padx=(1500,0))

    entrycut = Entry(cutframe, width=4, justify=CENTER)
    entrycut.pack(side=TOP, anchor="w")

    cutbutton = Button(cutframe, text="Cut", command=cutfunc, width=1)
    cutbutton.pack(side=TOP, anchor="w")

    # Manually input image
    #graph_png = PhotoImage(file="graph.png")

    scrollbar = Scrollbar(imageframe)
    scrollbar.pack(side=RIGHT, fill=Y, anchor="w")

    clusterlist = Listbox(imageframe, selectmode=EXTENDED)
    clusterlist.pack(side=LEFT, fill=Y, padx=(0, 10))

    joinbutton = Button(imageframe, text="Join Clusters", command=joinfunc, width=10)
    joinbutton.pack(side=TOP, anchor="w")

    joinbutton = Button(imageframe, text="Undo", command=undofunc, width=10)
    joinbutton.pack(side=TOP, anchor="w")

    #exportdmlbutton = Button(imageframe, text="Export as DML", command=exportdml, width=10)
    #exportdmlbutton.pack(side=BOTTOM, anchor="w")

    exportjsonbutton = Button(imageframe, text="Export as JSON", command=exportjson, width=10)
    exportjsonbutton.pack(side=BOTTOM, anchor="w")

    clusterlist.config(yscrollcommand=scrollbar.set)
    scrollbar.config(command=clusterlist.yview)

    window.mainloop()