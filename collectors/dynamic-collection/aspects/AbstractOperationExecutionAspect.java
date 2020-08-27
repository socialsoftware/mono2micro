import java.util.Iterator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.core.registry.ControlFlowRegistry;
import kieker.monitoring.probe.aspectj.AbstractAspectJProbe;
import kieker.monitoring.timer.ITimeSource;
import pt.ist.fenixframework.backend.jvstmojb.pstm.RelationList;

/**
 * @author Andre van Hoorn, Jan Waller, Bernardo
 *
 * @since 1.4
 */
@Aspect
public abstract class AbstractOperationExecutionAspect extends AbstractAspectJProbe {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOperationExecutionAspect.class);

	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
	private static final ITimeSource TIME = CTRLINST.getTimeSource();
	// private static final String VMNAME = CTRLINST.getHostname();
	private static final ControlFlowRegistry CFREGISTRY = ControlFlowRegistry.INSTANCE;

	@Pointcut
	public abstract void monitoredOperation();

	@Around("monitoredOperation() && notWithinKieker()")
	public Object operation(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS (Throwable)
		if (!CTRLINST.isMonitoringEnabled()) {
			return thisJoinPoint.proceed();
        }
        // FIXME WIP this should be commented to not introduce unnecessary extra overhead
		// final String signature = this.signatureToLongString(thisJoinPoint.getSignature());
		// if (!CTRLINST.isProbeActivated(signature)) {
		// 	return thisJoinPoint.proceed();
		// }
		// collect data
		final boolean entrypoint;
		// final String hostname = VMNAME;
		final int eoi; // this is executionOrderIndex-th execution in this trace
		final int ess; // this is the height in the dynamic call tree of this execution
		long traceId = CFREGISTRY.recallThreadLocalTraceId(); // traceId, -1 if entry point
		if (traceId == -1) {
			entrypoint = true;
			traceId = CFREGISTRY.getAndStoreUniqueThreadLocalTraceId();
			CFREGISTRY.storeThreadLocalEOI(0);
			CFREGISTRY.storeThreadLocalESS(1); // next operation is ess + 1
			eoi = 0;
			ess = 0;
		} else {
			entrypoint = false;
			eoi = CFREGISTRY.incrementAndRecallThreadLocalEOI(); // ess > 1
			ess = CFREGISTRY.recallAndIncrementThreadLocalESS(); // ess >= 0
			if ((eoi == -1) || (ess == -1)) {
				LOGGER.error("eoi and/or ess have invalid values: eoi == {} ess == {}", eoi, ess);
				CTRLINST.terminateMonitoring();
			}
		}
		// measure before
		final long tin = TIME.getTime();
		// execution of the called method
		Object retval = null;
        final StringBuilder newSignature = new StringBuilder(128);
		try {
            // final StringBuilder sb = new StringBuilder(1000);
            // sb.append("-------------------------------START TESTING---------------------------");
            // sb.append("\n");
            // sb.append("filename: " + thisJoinPoint.getSourceLocation().getFileName());
            // sb.append("\n");
            // sb.append("line: " + thisJoinPoint.getSourceLocation().getLine());
            // sb.append("\n");
            // sb.append("Kieker's signature: " + signature);
            // sb.append("\n");

            // 1) Getting the entity declaring type that executes the given method to accomodate the cases where the return type is an empty list (suitable during the processing phase)
            try {
                // sb.append("Entity declaring type: " + thisJoinPoint.getSignature().getDeclaringType().getSimpleName());
                newSignature.append(thisJoinPoint.getSignature().getDeclaringType().getSimpleName());

            } catch (Exception e) {
                // sb.append("Error getting entity declaring type: " + e.getMessage());            
                throw e;

            } finally {
                // sb.append("\n");
                // Separator
                newSignature.append(":");
            }

            // 2) Getting the method name
            try {
                // sb.append("Method name: " + thisJoinPoint.getSignature().getName());
                newSignature.append(thisJoinPoint.getSignature().getName());

            } catch (Exception e) {
                // sb.append("Error getting method name: " + e.getMessage());            
                throw e;
            } finally {
                // sb.append("\n");
                // Separator
                newSignature.append(":");
            }
            
            // 3) Getting the entity runtime type that executes the given method
            try {
                Object target = thisJoinPoint.getTarget();

                if (target != null) {
                    // sb.append("Target entity dynamic type: " + thisJoinPoint.getTarget().getClass().getSimpleName());            
                    newSignature.append(thisJoinPoint.getTarget().getClass().getSimpleName());
                } 
                else {
                    // sb.append("Target entity static type: " + thisJoinPoint.getSignature().getDeclaringType().getSimpleName());
                    // newSignature.append(thisJoinPoint.getSignature().getDeclaringType().getSimpleName()); // not necessary since we're already adding the declaring type
                }

            } catch (Exception e) {
                // sb.append("Error getting target entity type: " + e.getMessage());            
                throw e;
            } finally {
                // sb.append("\n");
                // Separator
                newSignature.append(":");
            }
            
            // 4) Getting the arguments types of the given method
            try {
                if (thisJoinPoint.getArgs().length > 0) {
                    Object[] args = thisJoinPoint.getArgs();
                    String argTypes = "[";
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] == null) {
                            String argStaticType = ((MethodSignature) thisJoinPoint.getSignature()).getParameterTypes()[i].getSimpleName();
                            // sb.append("Arg " + i + " static type: " + argStaticType);
                            argTypes += "\"" + argStaticType + "\"";
    
                        } else {
                            String argDynamicType = args[i].getClass().getSimpleName();
                            // sb.append("Arg " + i + " dynamic type: " + argDynamicType);
                            argTypes += "\"" + argDynamicType + "\"";
                        }
                        // sb.append("\n");
                        if (i < args.length - 1) {
                            argTypes += ",";
                        }
                    }
                    argTypes += "]";
                    newSignature.append(argTypes);
                } else {
                    // sb.append("No args (probably a get)");
                }

            } catch (Exception e) {
                // sb.append("Error getting args types: " + e.getMessage());            
                throw e;
            } finally {
                // sb.append("\n");
                // Separator
                newSignature.append(":");
            }

            // 5) Getting the returned value type of the given method
            try {
                retval = thisJoinPoint.proceed();

                if (retval != null) {
                    if (retval.getClass().getSimpleName().equals("RelationList")) {
                        RelationList retValAux = (RelationList) retval;
                        Iterator<?> i = retValAux.iterator();
                        
                        if (i.hasNext()) {
                            Object firstElement = i.next();
                            
                            if (firstElement != null) {
                                // sb.append("Returned value is a List of type: " + firstElement.getClass().getSimpleName());
                                // ASSUMPTION: returned value dynamic type is the dynamic type of the first element of the list
                                newSignature.append(firstElement.getClass().getSimpleName()); 
                            
                            } else {
                                // sb.append("Returned value is a List and has first element null");            
                            }
                            // sb.append("\n");
                        } else {
                            // sb.append("Returned value is an empty List");  
                        }
                    } else {
                        newSignature.append(retval.getClass().getSimpleName());
                        // sb.append("Returned value type: " + retval.getClass().getSimpleName());
                        // sb.append("\n");
                    }
                } else {
                    String returnedValueStaticType = ((MethodSignature) thisJoinPoint.getSignature()).getReturnType().getSimpleName();
                    // sb.append("Null return value. Static type is: " + returnedValueStaticType);
                    newSignature.append(returnedValueStaticType);
                }

            } catch (Exception e) {
                // sb.append("Error getting returned value type: " + e.getMessage());  
                throw e;
            } finally {
                // sb.append("\n");
            }
            
            // sb.append("New signature: " + newSignature);
            // sb.append("\n");
            // sb.append("-------------------------------END TESTING---------------------------");
            // System.out.println(sb);

        } finally {
			// measure after
            final long tout = TIME.getTime();
			CTRLINST.newMonitoringRecord(new OperationExecutionRecord("", newSignature.toString(), traceId, tin, tout, "", eoi, ess));
			// cleanup
			if (entrypoint) {
				CFREGISTRY.unsetThreadLocalTraceId();
				CFREGISTRY.unsetThreadLocalEOI();
				CFREGISTRY.unsetThreadLocalESS();
			} else {
				CFREGISTRY.storeThreadLocalESS(ess); // next operation is ess
			}
		}
		return retval;
	}
}
