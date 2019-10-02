package callGraph.handlers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;


public class CallGraphHandler extends AbstractHandler {

	JsonObject callSequence;
	JsonArray entitiesSequence;
	List<String> allEntities;
	Set<String> abstractEntities;
	Map<String,Set<String>> subclasses;
	Set<IType> controllers;
	ASTParser parser;
	Map<String,List<CallLocation>> methodCallees;


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IInputValidator validator = new IInputValidator() {
			public String isValid(String text) {
				if (getProject(text) == null) {
					return "Unable to find project " + text + " in the workspace.";
				} else {
					return null;
				}
			}
		};
		InputDialog inputDialog = new InputDialog(window.getShell(), "CallGraph", "Please enter project name:", "", validator);
		if (inputDialog.open() == Window.OK) {
			String projectName = inputDialog.getValue();
			System.out.println("Processing Project: " + projectName);
			long startTime = System.currentTimeMillis();
			IProject project = getProject(projectName);
			
			if (project == null) return null;

			
			callSequence = new JsonObject();
			allEntities = new ArrayList<>();
			abstractEntities = new HashSet<>();
			subclasses = new HashMap<>();
			controllers = new HashSet<>();
			parser = ASTParser.newParser(AST.JLS11);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			methodCallees = new HashMap<>();
			
			collectControllersAndEntities(project);
			
			for (IType controller : controllers) {
				processController(controller);
			}
			
			long elapsedTimeMillis = System.currentTimeMillis() - startTime;
			float elapsedTimeSec = elapsedTimeMillis/1000F;
			System.out.println("Complete. Elapsed time: " + elapsedTimeSec + " seconds");
			
			
			//prompt for file location
			FileDialog fileDialog = new FileDialog(window.getShell(), SWT.SAVE);
			fileDialog.setFilterExtensions(new String [] {"*.json"});
			fileDialog.setFileName(projectName + "_callSequence.json");
			String filepath = fileDialog.open();
			
			//file store
			if (filepath != null) {
				try (FileWriter file = new FileWriter(filepath)) {
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					file.write(gson.toJson(callSequence));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
	
	
	public IProject getProject(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();    // Get all projects in the workspace
		for (IProject p : projects) {
			if (p.getName().equals(projectName))
				return p;
		}
		return null;
	}


	private void collectControllersAndEntities(IProject project) {
		try {
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();
				
				for (IPackageFragment aPackage : packages) {
					
					// We will only look at the package from the source folder
					if (aPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
						
						for (ICompilationUnit unit : aPackage.getCompilationUnits()) {
							
							for (IType type : unit.getTypes()) {
								if (type.getAnnotation("Controller").exists()) { //Controller class
									controllers.add(type);
									allEntities.add(type.getElementName());
								} else if (!type.getElementName().endsWith("_Base")) {  //Domain class
									if (Flags.isAbstract(type.getFlags())) {
										abstractEntities.add(type.getElementName());
										
										ITypeHierarchy th= type.newTypeHierarchy(null);
										Set<String> subclassesTemp = new HashSet<>();
										IType[] subtypes = th.getAllSubtypes(type);
										for (IType t : subtypes) {
											if (!t.getElementName().endsWith("_Base"))
												subclassesTemp.add(t.getElementName());
										}
										subclasses.put(type.getElementName(), subclassesTemp);
									}
									allEntities.add(type.getElementName());
								}
							}
						}
					}
				}
				Collections.sort(allEntities, (string1, string2) -> Integer.compare(string2.length(), string1.length()));  //Longer length first
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	

	public void processController(IType controller) {
		try {
			for (IMethod controllerMethod : controller.getMethods()) {
				
				if (!controllerMethod.getAnnotation("RequestMapping").exists())
					continue;
				
				
				String controllerFullName = controllerMethod.getParent().getElementName() + "." + controllerMethod.getElementName();
				System.out.println("Processing Controller: " + controllerFullName);
				entitiesSequence = new JsonArray();
				Stack<IMethod> methodStack = new Stack<>();
				
				methodCallDFS(controllerMethod, methodStack);
				
				//expand abstract classes
				/*JSONArray expandedSubclassesEntities = new JSONArray();
				for (int i = 0; i < entitiesSequence.length(); i++) {
					try {
						JSONArray entityArray = (JSONArray) entitiesSequence.get(i);
						String entity = (String) entityArray.get(0);
						String mode = (String) entityArray.get(1);
						
						if (abstractEntities.contains(entity)) {
							for (String subclass : subclasses.get(entity)) {
								JSONArray subclassEntity = new JSONArray();
								subclassEntity.put(subclass);
								subclassEntity.put(mode);
								expandedSubclassesEntities.put(subclassEntity);
							}
						} else {
							JSONArray subclassEntity = new JSONArray();
							subclassEntity.put(entity);
							subclassEntity.put(mode);
							expandedSubclassesEntities.put(subclassEntity);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}*/
				
				if (entitiesSequence.size() > 0) {
					callSequence.add(controllerFullName, entitiesSequence);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	
	public void methodCallDFS(IMethod method, Stack<IMethod> methodStack) throws JavaModelException {
		methodStack.push(method);
		
		if (!methodCallees.containsKey(method.toString()))
			methodCallees.put(method.toString(), getCalleesOf(method));
		
		for (CallLocation calleeLocation : methodCallees.get(method.toString())) {
			IMethod callee = getIMethodFromCallLocation(calleeLocation);
			if (callee == null) {
				continue;
			} else if (callee.getParent().getElementName().endsWith("_Base")) {
				registerBaseClass(method, callee, calleeLocation);
			} else if (callee.getParent().getElementName().equals("FenixFramework") && callee.getElementName().equals("getDomainObject")) {
				registerDomainObject(method, calleeLocation);
			} else if (allEntities.contains(callee.getParent().getElementName())) {
				boolean recursive = false;
				Iterator<IMethod> value = methodStack.iterator();
		        while (value.hasNext()) { 
		            IMethod methodInStack = (IMethod) value.next();
		            if (callee.isSimilar(methodInStack) && callee.getParent().getElementName().equals(methodInStack.getParent().getElementName())) {
		            	recursive = true;
		            	break;
		            }
		        }
		        if (!recursive)
		        	methodCallDFS(callee, methodStack);
			}
		}
		methodStack.pop();
	}


	private void registerBaseClass(IMethod caller, IMethod callee, CallLocation calleeLocation) {
		
		String[] className = new String[] {callee.getParent().getElementName()};
		className[0] = className[0].substring(0, className[0].length()-5);
		
		if (abstractEntities.contains(className[0])) {
			parser.setSource(caller.getCompilationUnit());
	        parser.setResolveBindings(true);
	        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
	        
	        cu.accept(new ASTVisitor() {
				public boolean visit(MethodInvocation node) {
					if (calleeLocation.getStart() == node.getStartPosition() && callee.getElementName().equals(node.getName().toString())) {
						try {
							if (!node.getExpression().resolveTypeBinding().getName().equals(className[0])) {
								className[0] = node.getExpression().resolveTypeBinding().getName();
							}
						} catch (Exception e) {
							
						}
					}
					return true;
				}
			});
		}
		
		String methodName = callee.getElementName();
		String mode = "";
		if (methodName.startsWith("get"))
			mode = "R";
		if (methodName.startsWith("set"))
			mode = "W";
		if (methodName.startsWith("add"))
			mode = "W";
		if (methodName.startsWith("remove"))
			mode = "W";
		
		if (allEntities.contains(className[0]) && !mode.equals("")) {
			JsonArray entityAccess = new JsonArray();
			entityAccess.add(className[0]);
			entityAccess.add(mode);
			entitiesSequence.add(entityAccess);
		}
		
		try {
			for (String entityName : allEntities) {
				if (callee.getSignature().contains(entityName)) {
					JsonArray entityAccess = new JsonArray();
					entityAccess.add(entityName);
					entityAccess.add(mode);
					if (!mode.equals("")) entitiesSequence.add(entityAccess);
					break;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	
	private void registerDomainObject(IMethod method, CallLocation calleeLocation) {
		
		String mode = "R";

        parser.setSource(method.getCompilationUnit());
        parser.setResolveBindings(true);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
       
        cu.accept(new ASTVisitor() {
        	
			public boolean visit(MethodInvocation node) {
				if (node.getStartPosition() == calleeLocation.getStart()) {
					String resolvedType;
					if (node.getParent().getNodeType() == ASTNode.CAST_EXPRESSION) {
						CastExpression castExpression = (CastExpression) node.getParent();
						resolvedType = castExpression.getType().toString();
					} else {
						resolvedType = node.resolveTypeBinding().getName();
					}
					
					if (allEntities.contains(resolvedType)) {
						JsonArray entityAccess = new JsonArray();
						entityAccess.add(resolvedType);
						entityAccess.add(mode);
						entitiesSequence.add(entityAccess);
					}
				}
				return true;
			}
		});
	}


	public List<CallLocation> getCalleesOf(IMethod method) {
		List<CallLocation> callees = new ArrayList<>();
		Map<Integer,CallLocation> orderedCallees = new TreeMap<>();
		
		CallHierarchy callHierarchy = CallHierarchy.getDefault();

		MethodWrapper wrapper = callHierarchy.getCalleeRoots(new IMember[]{method})[0];
		for (MethodWrapper calleeWrapper : wrapper.getCalls(new NullProgressMonitor())) {
			for (CallLocation cl : calleeWrapper.getMethodCall().getCallLocations()) {
				orderedCallees.put(cl.getEnd(), cl);
			}
			
		}
		
		callees.addAll(orderedCallees.values());
		
		return callees;
	}
	
	IMethod getIMethodFromCallLocation(CallLocation cl) {
		try {
			IMember im = cl.getCalledMember();
			if (im.getElementType() == IJavaElement.METHOD) {
				return (IMethod) cl.getCalledMember();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
