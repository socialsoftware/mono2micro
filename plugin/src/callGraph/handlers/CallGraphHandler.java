package callGraph.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CallGraphHandler extends AbstractHandler {

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
		InputDialog dialog = new InputDialog(window.getShell(), "CallGraph", "Please enter project name:", "", validator);
		if (dialog.open() == Window.OK) {
		    String projectName = dialog.getValue();		
			IProject project = getProject(projectName);
			File f = new File("callgraph.txt");
			if(f.exists() && !f.isDirectory()) { 
				f.delete();
			}
			try {
				processRootDirectory(project);
			} catch (JavaModelException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		try {
			Runtime.getRuntime().exec("cmd.exe /C start /min python UserInterface.py callgraph.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	private void processRootDirectory(IProject project) throws JavaModelException, CoreException {
		System.out.println("Project: " + project.getName());
		if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = javaProject.getPackageFragments();

			// process each package
			for (IPackageFragment aPackage : packages) {
	
				// We will only look at the package from the source folder
				if (aPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
	
					for (ICompilationUnit unit : aPackage.getCompilationUnits()) {						
	
						IType[] allTypes = unit.getAllTypes();
						for (IType type : allTypes) {
							
							for (IAnnotation a : type.getAnnotations()) {
								if (a.getElementName().equals("Controller") || a.getElementName().equals("RestController")) {
									processController(type);
								}
							}
						}
					}
				}
			}
		}
	}  

	

	public void processController(IType type) {
		System.out.println("Processing Controller: " + type.getElementName());
		String output = "";

		try {
			for (IMethod controller_method : type.getMethods()) {
				ArrayList<IMethod> methodPool = new ArrayList<>();
				String controller_description = controller_method.getParent().getElementName() + "." + controller_method.getElementName();
				for (IMethod controller_calls : getCalleesOf(controller_method)) {
					methodPool.add(controller_calls);
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

				//only _Base classes
				ArrayList<String> completeDescriptions = new ArrayList<>();
				for (IMethod method : methodPool) {
					String classname = method.getParent().getElementName();
					String methodname = method.getElementName();
					if (classname.endsWith("_Base")) {
						classname = classname.substring(0, classname.length()-5);
						String mode = "";
						if (methodname.startsWith("get"))
							mode = "R";
						if (methodname.startsWith("set"))
							mode = "W";
						if (methodname.startsWith("add"))
							mode = "W";
						if (methodname.startsWith("remove"))
							mode = "W";
						String complete_description = controller_description + ":" + classname + ":" + mode;
						if (!completeDescriptions.contains(complete_description) && !classname.equals("DomainRoot"))
							completeDescriptions.add(complete_description);
					}
				}
				
				for (String a : completeDescriptions)
					output += a + "\n";
			}
			
			
			
			writeToFile(output);

		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void writeToFile(String text) {
		try {
	    	BufferedWriter writer = new BufferedWriter(new FileWriter("callgraph.txt",true));
			writer.append(text);
			writer.close();
		} catch (IOException e) {
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
