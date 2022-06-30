import env
from fastapi import APIRouter
from evaluations.class_analysis_correlation import class_analysis_correlation
from evaluations.entities_analysis_correlation import entities_analysis_correlation
from evaluations.features_entities_traces_analysis_correlation import features_entities_traces_analysis_correlation
from evaluations.features_method_calls_analysis_correlation import features_method_calls_analysis_correlation
from evaluations.static_analysis_correlation import static_analysis_correlation
from evaluations.compare_evaluation_metrics import compare_evaluation_metrics

router = APIRouter()

@router.get("/correlation/{analysisType}/{strategy}")
async def correlation(analysisType, strategy):
	if (analysisType == "features") and (strategy == "entitiesTraces"):
		features_entities_traces_analysis_correlation()

	elif (analysisType == "features") and (strategy == "methodCalls"):
		features_method_calls_analysis_correlation()
		
	return {"operation": "correlation", "analysisType": analysisType, "strategy": strategy}

@router.get("/correlation/{analysisType}/")
async def correlation(analysisType):
	if (analysisType == "static"):
		static_analysis_correlation()

	elif (analysisType == "classes"):
		class_analysis_correlation()
	
	elif (analysisType == "entities"):
		entities_analysis_correlation()

	return {"operation": "correlation", "analysisType": analysisType}

@router.get("/metrics/{analysisType}/{strategy}")
async def compare_metrics(analysisType, strategy):
	if (analysisType == "features") and (strategy == "entitiesTraces"):
		compare_evaluation_metrics("FAEA")

	elif (analysisType == "features") and (strategy == "methodCalls"):
		compare_evaluation_metrics("FAMC")
		
	return {"operation": "correlation", "analysisType": analysisType, "strategy": strategy}

@router.get("/metrics/{analysisType}/")
async def compare_metrics(analysisType):
	if (analysisType == "static"):
		compare_evaluation_metrics("SA")

	elif (analysisType == "classes"):
		compare_evaluation_metrics("CA")
	
	elif (analysisType == "entities"):
		compare_evaluation_metrics("EA")

	return {"operation": "correlation", "analysisType": analysisType}
