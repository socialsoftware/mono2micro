import json
import numpy as np
import pandas as pd
from py4j.java_gateway import JavaGateway

DISTR_SRC_FILE_PATH = '../../java/pt/ist/socialsoftware/mono2micro/utils/mojoCalculator/src/main/resources/distrSrc.rsf'
DISTR_TARGET_FILE_PATH = '../../java/pt/ist/socialsoftware/mono2micro/utils/mojoCalculator/src/main/resources' \
                         '/distrTarget.rsf'


ldod_entities_to_id = {
    "AddText": 1,
	"AltText": 2,
	"AltTextWeight": 3,
	"AnnexNote": 4,
	"Annotation": 5,
	"AppText": 6,
	"AwareAnnotation": 7,
	"Category": 8,
	"Citation": 9,
	"ClassificationGame": 10,
	"ClassificationGameParticipant": 11,
	"ClassificationGameRound": 12,
	"DelText": 13,
	"Dimensions": 14,
	"Edition": 15,
	"ExpertEdition": 16,
	"ExpertEditionInter": 17,
	"Facsimile": 18,
	"FragInter": 19,
	"Fragment": 20,
	"Frequency": 21,
	"GapText": 22,
	"GeographicLocation": 23,
	"HandNote": 24,
	"Heteronym": 25,
	"HumanAnnotation": 26,
	"InfoRange": 27,
	"LastTwitterID": 28,
	"LbText": 29,
	"LdoD": 30,
	"LdoDDate": 31,
	"LdoDUser": 32,
	"ManuscriptSource": 33,
	"MediaSource": 34,
	"Member": 35,
	"NoteText": 36,
	"NullEdition": 37,
	"NullHeteronym": 38,
	"ParagraphText": 39,
	"PbText": 40,
	"PhysNote": 41,
	"Player": 42,
	"PrintedSource": 43,
	"Range": 44,
	"RdgGrpText": 45,
	"RdgText": 46,
	"RecommendationWeights": 47,
	"RefText": 48,
	"RegistrationToken": 49,
	"Rend": 50,
	"Role": 51,
	"Section": 52,
	"SegText": 53,
	"SimpleText": 54,
	"SocialMediaCriteria": 55,
	"Source": 56,
	"SourceInter": 57,
	"SpaceText": 58,
	"SubstText": 59,
	"Surface": 60,
	"Tag": 61,
	"Taxonomy": 62,
	"TextPortion": 63,
	"TimeWindow": 64,
	"Tweet": 65,
	"TwitterCitation": 66,
	"TypeNote": 67,
	"UnclearText": 68,
	"UserConnection": 69,
	"VirtualEdition": 70,
	"VirtualEditionInter": 71
}

bw_entities_to_id = {
	"Activity": 1,
	"ActivityModel": 2,
	"ActivityView": 3,
	"ActivityWorkItem": 4,
	"AndCondition": 5,
	"AssociationGoal": 6,
	"Attribute": 7,
	"AttributeBoolCondition": 8,
	"AttributeInstance": 9,
	"AttributeValueExpression": 10,
	"BinaryExpression": 11,
	"BlendedWorkflow": 12,
	"BoolComparison": 13,
	"Cardinality": 14,
	"Comparison": 15,
	"Condition": 16,
	"ConditionModel": 17,
	"DataModel": 18,
	"DefAttributeCondition": 19,
	"DefEntityCondition": 20,
	"DefPathCondition": 21,
	"DefProductCondition": 22,
	"Dependence": 23,
	"Entity": 24,
	"EntityInstance": 25,
	"Expression": 26,
	"FalseCondition": 27,
	"Goal": 28,
	"GoalModel": 29,
	"GoalView": 30,
	"GoalWorkItem": 31,
	"MulCondition": 32,
	"NotCondition": 33,
	"NumberLiteral": 34,
	"OrCondition": 35,
	"Path": 36,
	"Position": 37,
	"PostWorkItemArgument": 38,
	"PreWorkItemArgument": 39,
	"Product": 40,
	"ProductGoal": 41,
	"ProductInstance": 42,
	"RelationBW": 43,
	"RelationInstance": 44,
	"Rule": 45,
	"Specification": 46,
	"StringLiteral": 47,
	"TrueCondition": 48,
	"View": 49,
	"WorkItem": 50,
	"WorkItemArgument": 51,
	"WorkflowInstance": 52
}

def calculateMoJoBetweenBestOfStaticAndBestOfDynamic():
    print("MoJo")

    dynamicEntities = []
    staticEntities = []
    firstFileDecompositions = bestDecompositionsOfFile[0]
    bestDecompositionForN = firstFileDecompositions[0]

    for clusterKey in bestDecompositionForN.keys():
        for entity in bestDecompositionForN[clusterKey]:
            dynamicEntities.append(entity)

    secondFileDecompositions = bestDecompositionsOfFile[1]
    bestDecompositionForN = secondFileDecompositions[0]

    for clusterKey in bestDecompositionForN.keys():
        for entity in bestDecompositionForN[clusterKey]:
            staticEntities.append(entity)

    for n in range(0, len(bestDecompositionsOfFile[0])):
        distrSrc = ""
        firstFileDecompositions = bestDecompositionsOfFile[0]
        bestDecompositionForN = firstFileDecompositions[n]

        for clusterKey in bestDecompositionForN.keys():
            for entity in bestDecompositionForN[clusterKey]:
                distrSrc += "contain " + clusterKey + " " + str(entity) + "\n"

        text_file = open(DISTR_SRC_FILE_PATH, "w+")
        text_file.write(distrSrc)
        text_file.close()

        distrTarget = ""  # static
        secondFileDecompositions = bestDecompositionsOfFile[1]
        bestDecompositionForN = secondFileDecompositions[n]

        for clusterKey in bestDecompositionForN.keys():
            for entity in bestDecompositionForN[clusterKey]:
                distrTarget += "contain " + clusterKey + " " + str(bw_entities_to_id[entity]) + "\n"

        text_file = open(DISTR_TARGET_FILE_PATH, "w+")
        text_file.write(distrTarget)
        text_file.close()

        # run Java to calculate MoJoFM
        try:
            gateway = JavaGateway()
            result = gateway.entry_point.runMoJo()
        except Exception:
            print("Warning: Entry point for the MoJoFM calculator not running")
            raise SystemExit

        # print("N = " + str(n + 3))
        print(str(result))


def getClusters(complexityWeights):
    cutFileName = ",".join(
        [
            str(int(complexityWeights[0])),
            str(int(complexityWeights[1])),
            str(int(complexityWeights[2])),
            str(int(complexityWeights[3])),
            str(int(n))
        ]
    ) + ".json"

    with open('../codebases/' + parsedFileName + '/analyser/cuts/' + cutFileName) as f:
        dataFile = json.load(f)
        return dataFile['clusters']


# the two csv files two compare
# if comparing Dynamic to Static dynamic has to be on index 0
files = [
    '45_83104.77_bw-simulation.csv',
    '45_61179.04_bwRebaixadoADynamicExpert.csv'
]
bestDecompositionsOfFile = []

for file in files:
    print(file)

    data = pd.read_csv("./data/" + file)

    minComplexityClusters = []

    for n in range(3, 11):
        minWeights = []
        minComplexity = float("inf")
        minComplexityWeights = []  # a, w, r, s
        for entry in data.values:
            if entry[0] != n:
                continue

            if entry[8] <= minComplexity:
                minComplexity = entry[8]
                minComplexityWeights = [entry[1], entry[2], entry[3], entry[4]]
                minWeights.append([entry[1], entry[2], entry[3], entry[4]])

        if minComplexity == float("inf"):  # no entries for this N
            continue

        parsedFileName = "_".join(file.split("_")[2:])
        parsedFileName = parsedFileName[0:len(parsedFileName) - 4]

        accessesWeights = []
        writeWeights = []
        readWeights = []
        sequenceWeights = []
        for sub in minWeights:
            accessesWeights.append(sub[0])
            writeWeights.append(sub[1])
            readWeights.append(sub[2])
            sequenceWeights.append(sub[3])

        avgText = ("AVG: [" + str(round(np.mean(accessesWeights), 2)) +
              ", " + str(round(np.mean(writeWeights), 2)) +
              ", " + str(round(np.mean(readWeights), 2)) +
              ", " + str(round(np.mean(sequenceWeights), 2)) +
              "]")

#         print('N = ' + str(n) + '\tMIN: ' + str(minComplexityWeights) + '\t' + avgText)
        minComplexityClusters.append(getClusters(minComplexityWeights))

    bestDecompositionsOfFile.append(minComplexityClusters)
#     print()

calculateMoJoBetweenBestOfStaticAndBestOfDynamic()
