package pt.ist.socialsoftware.mono2micro.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.dto.AnalyserDto;
import pt.ist.socialsoftware.mono2micro.dto.AnalysisDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

@RestController
@RequestMapping(value = "/mono2micro")
public class AnalysisController {

    private static Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    private CodebaseManager codebaseManager = CodebaseManager.getInstance();



	@RequestMapping(value = "/analyser", method = RequestMethod.POST)
	public ResponseEntity<AnalyserDto> analyser(@RequestBody AnalyserDto analyser) {
		logger.debug("analyser");
		
		try {
			String inputParameters = String.format("%.0f", analyser.getAccessWeight()) + "," +
									String.format("%.0f", analyser.getWriteWeight()) + "," +
									String.format("%.0f", analyser.getReadWeight()) + "," +
									String.format("%.0f", analyser.getSequence1Weight()) + "," +
									String.format("%.0f", analyser.getSequence2Weight()) + "," +
									String.format("%.0f", analyser.getNumberClusters());
			
			JSONObject analyserJSON = CodebaseManager.getInstance().getAnalyserResults(analyser.getCodebaseName());
			if (analyserJSON.has(inputParameters)) {
				JSONObject analyserResult = analyserJSON.getJSONObject(inputParameters);
				analyser.setAccuracy(BigDecimal.valueOf(analyserResult.getDouble("accuracy")).floatValue());
				analyser.setPrecision(BigDecimal.valueOf(analyserResult.getDouble("precision")).floatValue());
				analyser.setRecall(BigDecimal.valueOf(analyserResult.getDouble("recall")).floatValue());
				analyser.setSpecificity(BigDecimal.valueOf(analyserResult.getDouble("specificity")).floatValue());
				analyser.setFmeasure(BigDecimal.valueOf(analyserResult.getDouble("fmeasure")).floatValue());
				analyser.setComplexity(BigDecimal.valueOf(analyserResult.getDouble("complexity")).floatValue());
			
				return new ResponseEntity<>(analyser, HttpStatus.OK);
			}

			Codebase codebase = codebaseManager.getCodebase(analyser.getCodebaseName());

			Dendrogram dendrogram = new Dendrogram();
			dendrogram.setCodebaseName(analyser.getCodebaseName());
			dendrogram.setName(inputParameters);
			dendrogram.setLinkageType("average");
			dendrogram.setAccessMetricWeight(analyser.getAccessWeight());
			dendrogram.setWriteMetricWeight(analyser.getWriteWeight());
			dendrogram.setReadMetricWeight(analyser.getReadWeight());
			dendrogram.setSequenceMetric1Weight(analyser.getSequence1Weight());
			dendrogram.setSequenceMetric2Weight(analyser.getSequence2Weight());
			dendrogram.setProfiles(new ArrayList<String>(codebase.getProfiles().keySet()));

			codebase.createDendrogram(dendrogram);

			Graph graph = new Graph();
			graph.setCodebaseName(analyser.getCodebaseName());
			graph.setDendrogramName(inputParameters);
			graph.setExpert(false);
			graph.setCutType("N");
			graph.setCutValue(analyser.getNumberClusters());

			dendrogram.cut(graph);

			AnalysisDto analysisDto = new AnalysisDto();
			analysisDto.setGraph1(analyser.getExpert());
			analysisDto.setGraph2(graph);
			
			analysisDto = getAnalysis(analysisDto).getBody();

			analyser.setAccuracy(analysisDto.getAccuracy());
			analyser.setPrecision(analysisDto.getPrecision());
			analyser.setRecall(analysisDto.getRecall());
			analyser.setSpecificity(analysisDto.getSpecificity());
			analyser.setFmeasure(analysisDto.getFmeasure());
			analyser.setComplexity(graph.getComplexity());
			analyser.setExpert(null);

			JSONObject analyserDtoJson = new JSONObject(analyser);

			synchronized(this) {
				analyserJSON = CodebaseManager.getInstance().getAnalyserResults(analyser.getCodebaseName());
				analyserJSON.put(inputParameters, analyserDtoJson);
				CodebaseManager.getInstance().writeAnalyserResults(analyser.getCodebaseName(), analyserJSON);
			}

			codebase.deleteDendrogram(inputParameters);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(analyser, HttpStatus.OK);
	}


	@RequestMapping(value = "/analysis", method = RequestMethod.POST)
	public ResponseEntity<AnalysisDto> getAnalysis(@RequestBody AnalysisDto analysis) {
		logger.debug("getAnalysis");
		
		Map<String,List<String>> graph1 = new HashMap<>();
		for (Cluster c : analysis.getGraph1().getClusters()) {
			graph1.put(c.getName(), c.getEntityNames());
		}

		Map<String,List<String>> graph2 = new HashMap<>();
		for (Cluster c : analysis.getGraph2().getClusters()) {
			graph2.put(c.getName(), c.getEntityNames());
		}

		List<String> entities = new ArrayList<>();
		for (List<String> l1 : graph1.values()) {
			for (String e1 : l1) {
				boolean inBoth = false;
				for (List<String> l2 : graph2.values()) {
					if (l2.contains(e1)) {
						inBoth = true;
						break;
					}
				}
				if (inBoth)
					entities.add(e1);
			}				
		}

		int truePositive = 0;
		int falsePositive = 0;
		int trueNegative = 0;
		int falseNegative = 0;

		for (int i = 0; i < entities.size(); i++) {
			for (int j = i+1; j < entities.size(); j++) {
				String e1 = entities.get(i);
				String e2 = entities.get(j);

				String e1ClusterG1 = "";
				String e2ClusterG1 = "";
				String e1ClusterG2 = "";
				String e2ClusterG2 = "";

				for (String cluster : graph1.keySet()) {
					if (graph1.get(cluster).contains(e1)) {
						e1ClusterG1 = cluster;
					}
					if (graph1.get(cluster).contains(e2)) {
						e2ClusterG1 = cluster;
					}
				}

				for (String cluster : graph2.keySet()) {
					if (graph2.get(cluster).contains(e1)) {
						e1ClusterG2 = cluster;
					}
					if (graph2.get(cluster).contains(e2)) {
						e2ClusterG2 = cluster;
					}
				}

				boolean sameClusterInGraph1 = false;
				if (e1ClusterG1.equals(e2ClusterG1))
					sameClusterInGraph1 = true;
				
				boolean sameClusterInGraph2 = false;
				if (e1ClusterG2.equals(e2ClusterG2))
					sameClusterInGraph2 = true;

				if (sameClusterInGraph1 && sameClusterInGraph2)
					truePositive++;
				if (sameClusterInGraph1 && !sameClusterInGraph2)
					falseNegative++;
				if (!sameClusterInGraph1 && sameClusterInGraph2)
					falsePositive++;
				if (!sameClusterInGraph1 && !sameClusterInGraph2)
					trueNegative++;

				if (sameClusterInGraph1 != sameClusterInGraph2) {
					String[] falsePair = new String[6];
					falsePair[0] = e1;
					falsePair[1] = e1ClusterG1;
					falsePair[2] = e1ClusterG2;
					falsePair[3] = e2;
					falsePair[4] = e2ClusterG1;
					falsePair[5] = e2ClusterG2;

					analysis.addFalsePair(falsePair);
				}
			}
		}

		analysis.setTruePositive(truePositive);
		analysis.setTrueNegative(trueNegative);
		analysis.setFalsePositive(falsePositive);
		analysis.setFalseNegative(falseNegative);

		float accuracy = (float)(truePositive + trueNegative) / (truePositive + trueNegative + falsePositive + falseNegative);
		accuracy = BigDecimal.valueOf(accuracy).setScale(2, RoundingMode.HALF_UP).floatValue();
		float precision = (float)truePositive / (truePositive + falsePositive);
		precision = BigDecimal.valueOf(precision).setScale(2, RoundingMode.HALF_UP).floatValue();
		float recall = (float)truePositive / (truePositive + falseNegative);
		recall = BigDecimal.valueOf(recall).setScale(2, RoundingMode.HALF_UP).floatValue();
		float specificity = (float)trueNegative / (trueNegative + falsePositive);
		specificity = Float.isNaN(specificity) ? specificity : BigDecimal.valueOf(specificity).setScale(2, RoundingMode.HALF_UP).floatValue();
		float fmeasure = 2*precision*recall / (precision + recall);
		fmeasure = BigDecimal.valueOf(fmeasure).setScale(2, RoundingMode.HALF_UP).floatValue();
		analysis.setAccuracy(accuracy);
		analysis.setPrecision(precision);
		analysis.setRecall(recall);
		analysis.setSpecificity(specificity);
        analysis.setFmeasure(fmeasure);
		
		return new ResponseEntity<>(analysis, HttpStatus.OK);
	}
}