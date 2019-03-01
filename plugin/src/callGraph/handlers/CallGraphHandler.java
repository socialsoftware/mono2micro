package callGraph.handlers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
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
	Set<String> entitiesSet;
	Set<IType> controllersSet;

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
			IProject project = getProject(projectName);
						
			callgraph = new JSONObject();
			entitiesSet = new HashSet<String>();
			controllersSet = new HashSet<IType>();
			
			collectControllersAndEntities(project);
			
			for (IType controller : controllersSet)
				processController(controller);
			
			//prompt for file location
			FileDialog fileDialog = new FileDialog(window.getShell(), SWT.OPEN);
			fileDialog.setFilterExtensions(new String [] {"*.json"});
			fileDialog.setFileName(projectName + "_callgraph.json");
			String filepath = fileDialog.open();
			
			//file store
			try (FileWriter file = new FileWriter(filepath)) {
				file.write(callgraph.toString(4));
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Done");

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
							
							if (unit.getElementName().equals("EditionController.java")) {
								ASTParser parser = ASTParser.newParser(AST.JLS3);
				                parser.setKind(ASTParser.K_COMPILATION_UNIT);
				                parser.setSource(unit);
				                parser.setResolveBindings(true);
				                CompilationUnit cu = (CompilationUnit) parser.createAST(null); // parse
				                
				                cu.accept(new ASTVisitor() {
				                	public boolean visit(MethodDeclaration node) {
				                		List<Object> s = node.getBody().statements();
				                		for (Object o : s)
				                			System.out.println(o instanceof MethodInvocation);
				                		return false;
				                	}
				                	
				        			public boolean visit(MethodInvocation node) {
				        				System.out.println(node.resolveTypeBinding().getName());
				        				SimpleName name = node.getName();
				        				System.out.println("Declaration of '"+name+"' at line"+cu.getLineNumber(name.getStartPosition()));
				        				return false; // do not continue to avoid usage info
				        			}
				         
				        		});
							
							}
							
							/*for (IType type : unit.getTypes()) {
								
								for (IAnnotation a : type.getAnnotations()) {
									if (a.getElementName().equals("Controller")) {// || a.getElementName().equals("RestController")) {
										controllersSet.add(type);
									}
								}
								entitiesSet.add(type.getElementName());
							}*/
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	

	public void processController(IType controller) {
		System.out.println("Processing Controller: " + controller.getElementName());

		try {
			for (IMethod controllerMethod : controller.getMethods()) {
				JSONArray entities = new JSONArray();
				ArrayList<String> entitiesDescription = new ArrayList<String>();
				ArrayList<IMethod> methodPool = new ArrayList<>();
				String controllerDescription = controllerMethod.getParent().getElementName() + "." + controllerMethod.getElementName();
				for (IMethod controllerCall : getCalleesOf(controllerMethod)) {
					methodPool.add(controllerCall);
				}
				
				int i = 0;
				while (i < methodPool.size()) {
					for (IMethod callee : getCalleesOf(methodPool.get(i))) {
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
					i++;
				}
				
				if (controllerMethod.getElementName().equals("getEditionTableOfContentsbyId")) {
					for (IMethod s : methodPool) {
						if (s.getElementName().equals("getDomainObject")) {
							System.out.println(s.getSignature());
							System.out.println(s.getReturnType());
						}
					}
				}

				for (IMethod method : methodPool) {
					String className = method.getParent().getElementName();
					String methodName = method.getElementName();
					if (className.endsWith("_Base")) {
						className = className.substring(0, className.length()-5);
						String mode = "";
						if (methodName.startsWith("get"))
							mode = "R";
						if (methodName.startsWith("set"))
							mode = "W";
						if (methodName.startsWith("add"))
							mode = "W";
						if (methodName.startsWith("remove"))
							mode = "W";
						
						String entityDescription = className + ":" + mode;
						if (!entitiesDescription.contains(entityDescription) && !className.equals("DomainRoot")) {
							entitiesDescription.add(entityDescription);
							
							JSONArray entity = new JSONArray();
							entity.put(className);
							entity.put(mode);
							
							entities.put(entity);
						}
						
						for (String entityName : entitiesSet) {
							if (method.getSignature().contains(entityName)) {
								entityDescription = entityName + ":" + mode;
								if (!entitiesDescription.contains(entityDescription) && !entityName.equals("DomainRoot")) {
									entitiesDescription.add(entityDescription);
									
									JSONArray entity = new JSONArray();
									entity.put(entityName);
									entity.put(mode);
									
									entities.put(entity);
								}
							}
						}
					}
				}
				
				if (entities.length() > 0) {
					try {
						callgraph.put(controllerDescription, entities);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (JavaModelException e) {
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
