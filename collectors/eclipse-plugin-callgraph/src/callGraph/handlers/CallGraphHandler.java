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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CallGraphHandler extends AbstractHandler {

	JSONObject callgraph;
	JSONObject callgraph2;
	List<String> allEntities;
	Set<IType> controllers;
	Set<String> abstractEntities;
	ASTParser parser;
	Map<String,List<String>> subclasses;

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

			subclasses = new HashMap<>();
			callgraph = new JSONObject();
			callgraph2 = new JSONObject();
			allEntities = new ArrayList<String>();
			controllers = new HashSet<IType>();
			abstractEntities = new HashSet<String>();
			parser = ASTParser.newParser(AST.JLS11);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			
			collectControllersAndEntities(project);
			
			for (IType controller : controllers) {
				processController(controller);
			}
			
			long elapsedTimeMillis = System.currentTimeMillis() - startTime;
			float elapsedTimeSec = elapsedTimeMillis/1000F;
			System.out.println("Complete. Elapsed time: " + elapsedTimeSec + " seconds");
			
			
			//prompt for file location
			FileDialog fileDialog = new FileDialog(window.getShell(), SWT.OPEN);
			fileDialog.setFilterExtensions(new String [] {"*.json"});
			fileDialog.setFileName(projectName + "_callgraph.json");
			String filepath = fileDialog.open();
			
			//file store
			if (filepath != null) {
				try (FileWriter file = new FileWriter(filepath)) {
					file.write(callgraph2.toString());//.toString(4));
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

								if (type.getAnnotation("Controller").exists()) {
									controllers.add(type);
									allEntities.add(type.getElementName());
								} else if (!type.getElementName().endsWith("_Base")) {
									if (Flags.isAbstract(type.getFlags())) {
										abstractEntities.add(type.getElementName());
										
										ITypeHierarchy th= type.newTypeHierarchy(null);
										ArrayList<String> subclassesTemp = new ArrayList<>();
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
				Collections.sort(allEntities, (string1, string2) -> Integer.compare(string2.length(), string1.length()));
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
				JSONObject entities = new JSONObject();
				JSONArray entities2 = new JSONArray();
				ArrayList<IMethod> methodPool = new ArrayList<>();
				methodPool.add(controllerMethod);
					
				
				int i = 0;
				while (i < methodPool.size()) {
					for (IMethod callee : getCalleesOf(methodPool.get(i))) {
						
						if (callee.getParent().getElementName().endsWith("_Base")) {
							
							String className = callee.getParent().getElementName();
							className = className.substring(0, className.length()-5);
							if (abstractEntities.contains(className)) {
								IMethod caller = methodPool.get(i);
								parser.setSource(methodPool.get(i).getCompilationUnit());
						        parser.setResolveBindings(true);
						        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
						        
						        cu.accept(new ASTVisitor() {
						        	boolean foundMethod = false;
						        	
						        	public boolean visit(MethodDeclaration node) {
										if (node.getName().toString().equals(caller.getElementName())) {
											foundMethod = true;
										} else { 
											foundMethod = false;
										}
										return true;
									}
						        	
									public boolean visit(MethodInvocation node) {
										if (foundMethod) {
											if (callee.getElementName().equals(node.getName().toString())) {
												
												try {
													
													String className = callee.getParent().getElementName();
													className = className.substring(0, className.length()-5);
													
													if (!node.getExpression().resolveTypeBinding().getName().equals(className)) {
														registerBaseClass(callee, entities, node.getExpression().resolveTypeBinding().getName(), entities2);
													} else {
														
														registerBaseClass(callee, entities, "", entities2);
													}
												} catch (Exception e) {
													registerBaseClass(callee, entities, "", entities2);
												}
											}
										}
										
										return true;
									}
						 
								});
							} else {
								registerBaseClass(callee, entities, "", entities2);
							}
							
						} else if (callee.getElementName().equals("getDomainObject")) {
							registerDomainObject(methodPool.get(i), entities, entities2);
						} else if (allEntities.contains(callee.getParent().getElementName())) {
							boolean similar = false;
							for (IMethod method : methodPool) {
								if (method.isSimilar(callee) && method.getParent().getElementName().equals(callee.getParent().getElementName())) {
									similar = true;
									break;
								}
							}
							if (!similar) {
								methodPool.add(callee);
							}
						}
					}
					i++;
				}
				
				JSONObject newEntities = new JSONObject();
				Iterator<String> entitiesKeys = entities.keys();
				while (entitiesKeys.hasNext()) {
					String entity = entitiesKeys.next();
					if (abstractEntities.contains(entity)) {
						for (String subclass : subclasses.get(entity)) {
							try {
								entities.get(subclass);
							} catch (JSONException e) {
								try {
									newEntities.put(subclass, entities.get(entity));
								} catch (JSONException e1) {
									e1.printStackTrace();
								}
							}
						}
					} else {
						try {
							newEntities.put(entity, entities.get(entity));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
				
				JSONArray newEntities2 = new JSONArray();
				for (int e = 0; e < entities2.length(); e++) {
					try {
						JSONArray entityArray = (JSONArray) entities2.get(e);
						String entity = (String) entityArray.get(0);
						String mode = (String) entityArray.get(1);
						
						
						if (abstractEntities.contains(entity)) {
							for (String subclass : subclasses.get(entity)) {
								JSONArray newEntity = new JSONArray();
								newEntity.put(subclass);
								newEntity.put(mode);
								newEntities2.put(newEntity);
							}
						} else {
							JSONArray newEntity = new JSONArray();
							newEntity.put(entity);
							newEntity.put(mode);
							newEntities2.put(newEntity);
						}
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
				
				if (newEntities.length() > 0) {
					try {
						callgraph.put(controllerFullName, newEntities);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				if (entities2.length() > 0) {
					try {
						callgraph2.put(controllerFullName, entities2);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}


	private void registerDomainObject(IMethod caller, JSONObject entities, JSONArray entities2) {
		
		String mode = "R";

        parser.setSource(caller.getCompilationUnit());
        parser.setResolveBindings(true);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
       
        cu.accept(new ASTVisitor() {
        	boolean foundMethod = false;
        	
        	public boolean visit(MethodDeclaration node) {
				if (node.getName().toString().equals(caller.getElementName())) {
					foundMethod = true;
				} else {
					foundMethod = false;
				}
				return true;
			}
        	
			public boolean visit(MethodInvocation node) {
				if (foundMethod) {
					if (node.getName().toString().equals("getDomainObject")) {
						String resolvedType;
						if (node.getParent().getNodeType() == ASTNode.CAST_EXPRESSION) {
							CastExpression castExpression = (CastExpression) node.getParent();
							resolvedType = castExpression.getType().toString();
						} else {
							resolvedType = node.resolveTypeBinding().getName();
						}
						
						try {
							if (allEntities.contains(resolvedType)) {
								if (entities.has(resolvedType)) {
		        					JSONArray entityModes = entities.getJSONArray(resolvedType);
		        					if(!entityModes.join(":").contains(mode)) {
		        						entityModes.put(mode);
		        					}
		        				} else {
		        					JSONArray entityModes = new JSONArray();
		        					entityModes.put(mode);
		        					entities.put(resolvedType, entityModes);
		        				}
								JSONArray entityAccess = new JSONArray();
								entityAccess.put(resolvedType);
								entityAccess.put(mode);
								entities2.put(entityAccess);
							}
						} catch (JSONException e) {
		        			e.printStackTrace();
		        		}
					}
				}
				return true;
			}
		});
	}


	private void registerBaseClass(IMethod method, JSONObject entities, String actualClass, JSONArray entities2) {
		
		String className = method.getParent().getElementName();
		if (actualClass.equals(""))
			className = className.substring(0, className.length()-5);
		else
			className = actualClass;
		String methodName = method.getElementName();
		String mode = "";
		if (methodName.startsWith("get"))
			mode = "R";
		if (methodName.startsWith("set"))
			mode = "W";
		if (methodName.startsWith("add"))
			mode = "W";
		if (methodName.startsWith("remove"))
			mode = "W";
		
		try {
			if (allEntities.contains(className)) {
				if (entities.has(className)) {
					JSONArray entityModes = entities.getJSONArray(className);
					if(!entityModes.join(":").contains(mode)) {
						entityModes.put(mode);
					}
				} else {
					JSONArray entityModes = new JSONArray();
					entityModes.put(mode);
					entities.put(className, entityModes);
				}
				JSONArray entityAccess = new JSONArray();
				entityAccess.put(className);
				entityAccess.put(mode);
				if (!mode.equals("")) entities2.put(entityAccess);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			for (String entityName : allEntities) {
				if (method.getSignature().contains(entityName)) {
					if (entities.has(entityName)) {
						JSONArray entityModes = entities.getJSONArray(entityName);
						if(!entityModes.join(":").contains(mode)) {
							entityModes.put(mode);
						}
					} else {
						JSONArray entityModes = new JSONArray();
						entityModes.put(mode);
						entities.put(entityName, entityModes);
					}
					JSONArray entityAccess = new JSONArray();
					entityAccess.put(entityName);
					entityAccess.put(mode);
					if (!mode.equals("")) entities2.put(entityAccess);
					break;
				}
			}
		} catch (JSONException | JavaModelException e) {
			e.printStackTrace();
		}
	}


	public HashSet<IMethod> getCalleesOf(IMethod m) {
		 
		CallHierarchy callHierarchy = CallHierarchy.getDefault();
		 
		IMember[] members = {m};

		MethodWrapper[] methodWrappers = callHierarchy.getCalleeRoots(members);
		HashSet<IMethod> callees = new HashSet<IMethod>();
		for (MethodWrapper mw : methodWrappers) {
			MethodWrapper[] mw2 = mw.getCalls(new NullProgressMonitor());
			HashSet<IMethod> temp = getIMethods(mw2);
			callees.addAll(temp);    
		} 
		return callees;
	}
		 
	HashSet<IMethod> getIMethods(MethodWrapper[] methodWrappers) {
		HashSet<IMethod> c = new HashSet<IMethod>(); 
		for (MethodWrapper m : methodWrappers) {
			IMethod im = getIMethodFromMethodWrapper(m);
			if (im != null) {
				c.add(im);
			}
		}
		return c;
	}
		 
	IMethod getIMethodFromMethodWrapper(MethodWrapper m) {
		try {
			IMember im = m.getMember();
			if (im.getElementType() == IJavaElement.METHOD) {
				return (IMethod)m.getMember();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
