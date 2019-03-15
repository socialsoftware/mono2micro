package callGraph.handlers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
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
	List<String> entitiesList;
	Set<IType> controllersSet;
	Set<String> abstractEntitiesSet;
	ASTParser parser;
	int start;
	int end;
	int a = 0;
	int b = 0;

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
			long startTime = System.currentTimeMillis();
			IProject project = getProject(projectName);

			callgraph = new JSONObject();
			entitiesList = new ArrayList<String>();
			controllersSet = new HashSet<IType>();
			abstractEntitiesSet = new HashSet<String>();
			parser = ASTParser.newParser(AST.JLS11);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			
			collectControllersAndEntities(project);
			
			for (IType controller : controllersSet) {
				processController(controller);
			}
			
			long elapsedTimeMillis = System.currentTimeMillis() - startTime;
			float elapsedTimeSec = elapsedTimeMillis/1000F;
			System.out.println("Complete. Elapsed time: " + elapsedTimeSec + " seconds");
			
			System.out.println(a);
			System.out.println(b);
			
			//prompt for file location
			FileDialog fileDialog = new FileDialog(window.getShell(), SWT.OPEN);
			fileDialog.setFilterExtensions(new String [] {"*.json"});
			fileDialog.setFileName(projectName + "_callgraph.json");
			String filepath = fileDialog.open();
			
			//file store
			if (filepath != null) {
				try (FileWriter file = new FileWriter(filepath)) {
					file.write(callgraph.toString(4));
				} catch (IOException | JSONException e) {
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
								
								for (IAnnotation a : type.getAnnotations()) {
									if (a.getElementName().equals("Controller")) { // || a.getElementName().equals("RestController")) {
										controllersSet.add(type);
									}
								}
								entitiesList.add(type.getElementName());
								if (Flags.isAbstract(type.getFlags()) && !type.getElementName().endsWith("_Base")) {
									abstractEntitiesSet.add(type.getElementName());
								}
							}
						}
					}
				}		
				Collections.sort(entitiesList, (string1, string2) -> Integer.compare(string2.length(), string1.length()));
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
				a+=1;
				JSONObject entities = new JSONObject();
				ArrayList<IMethod> methodPool = new ArrayList<>();
				methodPool.add(controllerMethod);
				
				int i = 0;
				while (i < methodPool.size()) {
					for (IMethod callee : getCalleesOf(methodPool.get(i))) {
						if (callee.getParent().getElementName().endsWith("_Base")) {
							registerBaseClass(callee, entities);
						} else if (callee.getElementName().equals("getDomainObject")) {
							registerDomainObject(methodPool.get(i), entities);
						} else if (entitiesList.contains(callee.getParent().getElementName())) {
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
				System.out.println(methodPool.size());
				
				if (entities.length() > 0) {
					b+=1;
					try {
						callgraph.put(controllerMethod.getParent().getElementName() + "." + controllerMethod.getElementName(), entities);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	

	private void registerDomainObject(IMethod caller, JSONObject entities) {
		
		String mode = "R";
		Map<Integer, String> domainObjectInvocations = new HashMap<>();

        parser.setSource(caller.getCompilationUnit());
        parser.setResolveBindings(true);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null); // parse
       
        cu.accept(new ASTVisitor() {
        	
			public boolean visit(MethodInvocation node) {
				if (node.getName().toString().equals("getDomainObject")) {
					domainObjectInvocations.put(node.getStartPosition(), node.resolveTypeBinding().getName());
				}
				return true;
			}
			
			public boolean visit(MethodDeclaration node) {
				if (node.getName().toString().equals(caller.getElementName())) {
					start = node.getStartPosition();
					end = start + node.getLength();
				}
				return true;
			}
 
		});
        
        
        for (Map.Entry<Integer, String> entry : domainObjectInvocations.entrySet()) {
            if (entry.getKey() >= start && entry.getKey() <= end) {
            	try {
        			if (!entry.getValue().equals("DomainRoot")) {
        				if (entities.has(entry.getValue())) {
        					JSONArray entityModes = entities.getJSONArray(entry.getValue());
        					if(!entityModes.join(":").contains(mode)) {
        						entityModes.put(mode);
        					}
        				} else if (!abstractEntitiesSet.contains(entry.getValue())){
        					JSONArray entityModes = new JSONArray();
        					entityModes.put(mode);
        					entities.put(entry.getValue(), entityModes);
        				}
        			}
        		} catch (JSONException e) {
        			e.printStackTrace();
        		}
            }
        }
	}


	private void registerBaseClass(IMethod method, JSONObject entities) {
		String className = method.getParent().getElementName();
		className = className.substring(0, className.length()-5);
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
			if (!className.equals("DomainRoot")) {
				if (entities.has(className)) {
					JSONArray entityModes = entities.getJSONArray(className);
					if(!entityModes.join(":").contains(mode)) {
						entityModes.put(mode);
					}
				} else if (!abstractEntitiesSet.contains(className)) {
					JSONArray entityModes = new JSONArray();
					entityModes.put(mode);
					entities.put(className, entityModes);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			for (String entityName : entitiesList) {
				if (method.getSignature().contains(entityName)) {
					if (!entityName.equals("DomainRoot")) {
						if (entities.has(entityName)) {
							JSONArray entityModes = entities.getJSONArray(entityName);
							if(!entityModes.join(":").contains(mode)) {
								entityModes.put(mode);
							}
						} else if (!abstractEntitiesSet.contains(entityName)){
							JSONArray entityModes = new JSONArray();
							entityModes.put(mode);
							entities.put(entityName, entityModes);
						}
					}
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
