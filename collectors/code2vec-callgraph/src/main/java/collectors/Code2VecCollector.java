package collectors;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;

import com.github.javaparser.StaticJavaParser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import org.json.JSONObject;

import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

import dto.MethodPredict;

import explorer.DirExplorer;
import models.Project;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class Code2VecCollector {

	public static final List<String> CONFIGURATION_ANNOTATIONS = Arrays.asList(new String[]{"SpringBootApplication", "Configuration"});
	public static final List<String> ENDPOINT_ANNOTATIONS = Arrays.asList(new String[]{"RequestMapping", "GetMapping", "PostMapping", "PatchMapping", "PutMapping", "DeleteMapping"});

	public static final String CONTROLLER_ANNOTATION = "Controller";
	public static final String ENTITY_ANNOTATION = "Entity";
	public static final String SERVICE_ANNOTATION = "Service";
	public static final String REPOSITORY_ANNOTATION = "Repository";

	public static String SCRIPTS_ADDRESS = "http://localhost:5002";

	public static HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();

	public static MethodPredict predict(String name, String body) throws Exception {
		try {
			Map<String, String> bodyMap = new HashMap<String, String>();
            bodyMap.put("name", name);
            bodyMap.put("body", body);
            JSONObject jsonBody = new JSONObject(bodyMap);

			HttpRequest post = HttpRequest.newBuilder()
				.uri(new URI(SCRIPTS_ADDRESS + "/code2vec/predict"))
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(jsonBody.toString()))
				.build();

			HttpResponse<String> postResponse = client.send(post, BodyHandlers.ofString());
			JSONObject jsonPostReponse = new JSONObject(postResponse.body());

			MethodPredict mp = new MethodPredict(jsonPostReponse);

			return mp;
		} catch (Exception e) {
			System.err.println(e);
			throw e;
		}
	}

	public static String parseConstructorToMethod(ConstructorDeclaration constructorDeclaration) {
		String body = constructorDeclaration.getAccessSpecifier().toString().toLowerCase();
		body += " void " + constructorDeclaration.getNameAsString() + "(";
		int length = constructorDeclaration.getParameters().size();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				body += constructorDeclaration.getParameter(i).toString();
			} else {
				body += ", " + constructorDeclaration.getParameter(i).toString();
			}
		}
		body += ") " + constructorDeclaration.getBody().toString().replaceAll("(super[(])", "sup(");
		// TODO: Can't have super() method calls inside methods... 
		return body;
	}

	public static void extractMethods(File file, JavaParserFacade jpf, Project project) {

		try {

			CompilationUnit cu = StaticJavaParser.parse(file);
			if (!cu.findFirst(ClassOrInterfaceDeclaration.class).isPresent()) { return; }

			String packageName = "";
			if (cu.findFirst(PackageDeclaration.class).isPresent()) {
				PackageDeclaration pd = (PackageDeclaration) cu.findFirst(PackageDeclaration.class).get();
				packageName = pd.getName().toString();
			}

			String className = "";
			String classType = "";
			String superQualifiedName = "";
			ClassOrInterfaceDeclaration coid = (ClassOrInterfaceDeclaration) cu.findFirst(ClassOrInterfaceDeclaration.class).get();
			for (String annotation : CONFIGURATION_ANNOTATIONS) {
				if (coid.getAnnotations().toString().contains(annotation)) {
					System.out.println("[+] Ignoring: " + coid.getName().toString());
					System.out.println("[+] Annotation: " + annotation);
					return;
				}
			}
			className = coid.getName().toString();
			classType = coid.isInterface() ? "interface" : "class";
			if (coid.isInterface()) {
				System.out.println("[+] Ignoring Interface: " + coid.getName().toString());
				return;
			}
			if (coid.getAnnotations().toString().contains(CONTROLLER_ANNOTATION)) {
				classType = CONTROLLER_ANNOTATION;
			} else if (coid.getAnnotations().toString().contains(ENTITY_ANNOTATION)) {
				classType = ENTITY_ANNOTATION;
			} else if (coid.getAnnotations().toString().contains(SERVICE_ANNOTATION)) {
				classType = SERVICE_ANNOTATION;
			} else if (coid.getAnnotations().toString().contains(REPOSITORY_ANNOTATION)) {
				classType = REPOSITORY_ANNOTATION;
			}

			try {
				NodeList<ClassOrInterfaceType> extendedTypes = coid.getExtendedTypes();
				if (extendedTypes.size() > 0) {
					superQualifiedName = extendedTypes.get(0).resolve().getQualifiedName();
				}
			} catch (Exception e) {
				System.out.println("[-] Resolution error: Unknown extended type");
			}
			project.addClass(packageName, className, classType, superQualifiedName);

			for (MethodDeclaration methodDeclaration : cu.findAll(MethodDeclaration.class)) {

				String signature = methodDeclaration.getSignature().asString();
				try {
					ResolvedMethodDeclaration resolvedMetDecl = methodDeclaration.resolve();
					if (resolvedMetDecl != null)
						signature = resolvedMetDecl.getQualifiedSignature();
				} catch(Exception _err) {}

				String body = methodDeclaration.toString();
				//  	.replaceAll("[\\r\\n\\t]", ""); //Code after comments unreachable
				String type = "Regular";
				for (String annotation : ENDPOINT_ANNOTATIONS) {
					if (methodDeclaration.getAnnotations().toString().contains(annotation)) {
						type = CONTROLLER_ANNOTATION;
					}
				}

				Boolean successfulPrediction = false;

				try {

					MethodPredict mp = predict(signature, body);

					project.addMethod(packageName, className, signature, mp.getCodeVector(), type);

					successfulPrediction = true;

				} catch (Exception e) {
					System.out.println("[ - ] On predict : " + e.toString());
				}

				if (successfulPrediction) {
					for (MethodCallExpr call : methodDeclaration.findAll(MethodCallExpr.class)) {
						try {

							System.out.println("[+] MethodCallExpr: " + call);

							SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = jpf.solve(call);

							project.addMethodCallToLast(
									packageName,
									className,
									resolvedMethodDeclaration.getCorrespondingDeclaration().getPackageName(),
									resolvedMethodDeclaration.getCorrespondingDeclaration().getClassName(),
									resolvedMethodDeclaration.getCorrespondingDeclaration().getQualifiedSignature()
							);

						} catch (Exception e) {
							System.out.println("[-] Resolution error: " + e);
						}
					}

					for (ObjectCreationExpr oce: methodDeclaration.findAll(ObjectCreationExpr.class)) {
						try {

							System.out.println("[+] ObjectCreationExpr: " + oce);

							ResolvedConstructorDeclaration rcd = oce.resolve();

							project.addMethodCallToLast(
									packageName,
									className,
									rcd.getPackageName(),
									rcd.getClassName(),
									rcd.getQualifiedSignature()
							);

						} catch (Exception e) {
							System.out.println("[-] Resolution error: " + e);
						}
					}
				}
			}

			for (ConstructorDeclaration constructorDeclaration : cu.findAll(ConstructorDeclaration.class)) {
				System.out.println("[+] ConstructorDeclaration: " + constructorDeclaration.getName());

				String signature = constructorDeclaration.getSignature().asString();
				try {
					ResolvedConstructorDeclaration resolvedConstDecl = constructorDeclaration.resolve();
					if (resolvedConstDecl != null)
						signature = resolvedConstDecl.getQualifiedSignature();
				} catch(Exception _err) {}

				Boolean successfulPrediction = false;

				try {

					String type = "Constructor";
					String body = parseConstructorToMethod(constructorDeclaration);

					MethodPredict mp = predict(signature, body);

					project.addMethod(packageName, className, signature, mp.getCodeVector(), type);

					successfulPrediction = true;

				} catch (Exception e) {
					System.out.println("[ - ] : " + e.toString());
				}

				if (successfulPrediction) {
					for (MethodCallExpr call : constructorDeclaration.findAll(MethodCallExpr.class)) {
						try {

							System.out.println("[+] MethodCallExpr: " + call);

							SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = jpf.solve(call);

							project.addMethodCallToLast(
									packageName,
									className,
									resolvedMethodDeclaration.getCorrespondingDeclaration().getPackageName(),
									resolvedMethodDeclaration.getCorrespondingDeclaration().getClassName(),
									resolvedMethodDeclaration.getCorrespondingDeclaration().getQualifiedSignature()
							);

						} catch (Exception e) {
							System.out.println("[-] Resolution error: " + e);
						}
					}

					for (ObjectCreationExpr oce: constructorDeclaration.findAll(ObjectCreationExpr.class)) {
						try {

							System.out.println("[+] ObjectCreationExpr: " + oce);

							ResolvedConstructorDeclaration rcd = oce.resolve();

							project.addMethodCallToLast(
									packageName,
									className,
									rcd.getPackageName(),
									rcd.getClassName(),
									rcd.getQualifiedSignature()
							);

						} catch (Exception e) {
							System.out.println("[-] Resolution error: " + e);
						}
					}
				}
			}

		} catch (Exception e) {
			System.out.println("Inside Error: " + e);
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			long startTime = System.currentTimeMillis();

			final String repoName = args[0];
			File projectDir = new File("../../repos/" + repoName + "/src/main/java");

			Project project = new Project(repoName);

			TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
			TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File("../../repos/" + repoName + "/src/main/java"));

			CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
			combinedTypeSolver.add(reflectionTypeSolver);
			combinedTypeSolver.add(javaParserTypeSolver);

			JavaParserFacade jpf = JavaParserFacade.get(combinedTypeSolver);

			JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
			StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

			new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
				// System.out.println();
				// System.out.println("[+] LEVEL: " + level + ", PATH: " + path);
				// System.out.println("======================================================================================");

				extractMethods(file, jpf, project);

			}).explore(projectDir);

			project.saveToFile();

			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("[+] PERFORMANCE TIME: " + estimatedTime);

		} catch (Exception e) {
			System.err.println(e);
		}
	}

}