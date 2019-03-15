import re
import glob
import json

set_of_classes = set()
dict_controller_classes = dict()
abstract_classes = dict()
FILE_PATH = str()

def get_dict_controller_classes_updatedfile():
    '''Due to manual correction of the files, the updated class/controller is taken from a file'''
    with open(FILE_PATH, 'r') as file:
        dict = json.load(file)
        return dict

def write_file_path(string_path):
    global FILE_PATH
    FILE_PATH = string_path

def get_dict_controller_classes():
    '''returns the dictionary containing the controller and its respective classes'''
    return dict_controller_classes

def get_list_of_controllers():
    '''Returns the sorted name of the controllers'''
    # CHANGE PATH TO CONTROLLERS OF THE APPLICATION

    #TODO - add config file with path
    #Blended Workflow
    return sorted(glob.glob("/home/luis_nunes/PycharmProjects/Tese/Application_controllers/controllers_BW/*.java"))

    #LdoD
    #return sorted(glob.glob("/home/luis_nunes/PycharmProjects/Tese/Application_controllers/controllers_LDOD/*.java"))

def get_all_controller_classes():
    '''Returns the unique classes of the application'''

    #TODO -  add config file with path
    # CHANGE PATH TO DOMAIN OF APPLICATION
    for classes in set(glob.glob("/home/luis_nunes/PycharmProjects/Tese/Application_controllers/domain_BW/*.java")):

    #for classes in set(glob.glob("/home/luis_nunes/PycharmProjects/Tese/Application_controllers/domain_LDOD/*.java")):
        set_of_classes.add(classes.split('/')[-1].split('.')[0])
    print(len(set_of_classes))
    return sorted(set_of_classes)

def remove_duplicates_list(l):
    '''Creates Set from List to remove duplicates'''
    return sorted(list(set(l)))

def get_abstract_classes(file):
    with open(file, 'r+') as file_content:
        for line in file_content:
            if 'S:' in line:
                linesplit = line.split(' ')
                if '_Base' in linesplit[0]:
                    if 'domain' in linesplit[1]:
                        if '_Base' in linesplit[1]:
                            pass
                        else:
                            # print(line)
                            abstract_classes.setdefault(linesplit[0].split('.')[-1].split('_')[0], []).append(
                                linesplit[1].split('.')[-1].replace('\n', ''))
        file_content.close()



def retrieve_domain_classes(controller, file):
    '''Receives the path to the .txt that has the callgraph representation and parses for the domain classes of the controller'''
    with open(file, 'r+') as file_content:
        #print('ABS:' +  abstract_classes.__str__())
        domain_classes = []

        file_string = file_content.read();

        controller = controller.split('/')[-1][0:-5]
        # print('Controller:' + controller)

        #Find all the methods called first by the controller
        first_order_method_calls = re.findall(controller + ':' + '(.*)' + '()', file_string)

        method_calls = []

        for method in first_order_method_calls:
            method_calls.append(method[0])

        for method in method_calls:

            function_arguments = method.split('(')[-1][0:-1]

            # Retrieve domain from arguments passed on function
            if 'domain' in function_arguments:
                if 'Base' in method:
                    pass
                elif '$' in method:
                    pass
                elif 'java' in method:
                    pass
                else:
                    domain_class = function_arguments.split('.')[-1]
                    domain_classes.append(domain_class)
                    if controller not in dict_controller_classes.keys():
                        dict_controller_classes.setdefault(controller, []).append(domain_class)
                    elif domain_class not in dict_controller_classes[controller]:
                        dict_controller_classes.setdefault(controller, []).append(domain_class)

            notsplitmethod = method
            method = method.split(' ')[-1][3:].split('(')[0]
            print('On the method:' + method)


            # Also search in its abstract classes
            #classname = method.split('.')[-1].split(':')[0]
            #
            #if classname in abstract_classes.keys():
            #    absclass = abstract_classes[classname]
            #    absmethodcall = notsplitmethod.replace(classname, absclass[0], 1)

                #print(classname)
                #print(absmethodcall)

            #    if absmethodcall in method_calls:
            #        pass
            #    else:
            #        method_calls.append(absmethodcall)

            #Parse only for the method called by the controller so we can search it again
            # If has get on method we need to search the _base class
            if 'get' in method:
                if 'Base' in method:
                    # replace $ with \$ so it can be caught in the regex
                    search_calls = re.findall("M:" + method.replace('$', '\$') + "\((.*)" + "()", file_string)

                    for call in search_calls:
                        # Remove cyclic dependencies
                        if call[0] in method_calls:
                            pass
                        else:
                            print('methods insideBASE:' + call[0].split(' ')[-1][3:].split('(')[0])
                            method_calls.append(call[0])

                else:
                    split_method = method.split(':')

                    method_with_base = split_method[0] + '_Base:' + split_method[1]
                    search_calls = re.findall('M:' + method_with_base + '\((.*)' + '()', file_string)

                    for call in search_calls:
                        # Remove cyclic dependencies
                        if call[0] in method_calls:
                            pass
                        else:
                            print('methods inside1:' + call[0].split(' ')[-1][3:].split('(')[0])
                            method_calls.append(call[0])
            # See if the name of the method contains domain on the package
            if 'domain' in method:
                if 'Base' in method:
                    pass
                elif '$' in method:
                    pass
                else:
                    clazz = method.split(':')[0].split('.')[-1]
                    domain_classes.append(clazz)
                    if controller not in dict_controller_classes.keys():
                        dict_controller_classes.setdefault(controller, []).append(clazz)
                    elif clazz not in dict_controller_classes[controller]:
                        dict_controller_classes.setdefault(controller, []).append(clazz)

            search_calls = re.findall('M:' + method + '\((.*)' + '()', file_string)

            for call in search_calls:
                # Remove cyclic dependencies
                if call[0] in method_calls:
                    pass
                else:
                    print('methods inside2:' + call[0].split(' ')[-1][3:].split('(')[0])
                    method_calls.append(call[0])

        return remove_duplicates_list(domain_classes)