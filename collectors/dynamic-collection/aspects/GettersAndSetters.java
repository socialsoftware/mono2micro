import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Bernardo Andrade
 */
@Aspect
public class GettersAndSetters extends AbstractOperationExecutionAspect {

	public GettersAndSetters() {
		// empty default constructor
	}

	// --------------------------------------------------------------------- EXCEPTIONS ----------------------------------------------------------------------

	@Pointcut("execution(pt.ist.fenixframework..* get*(..))")
	public void gettersReturningFenixFrameworkObject() {}
	
	@Pointcut("execution(* set*(pt.ist.fenixframework..*, ..))")
	public void settersWithFenixFrameworkObjectArgument() {}
	
	@Pointcut("!gettersReturningFenixFrameworkObject() && !settersWithFenixFrameworkObjectArgument()")
    public void noGettersOrSettersWithFenixFramework() {}
	
    // --------------------------------------------------------------------------- _BASE --------------------------------------------------------------------------- 

    @Pointcut("execution(public void pt.ist..*.*_Base.add*(..)) && within(pt.ist..*.*_Base)")
	public void publicBaseAddMethods() {}

	@Pointcut("execution(public void pt.ist..*.*_Base.remove*(..)) && within(pt.ist..*.*_Base)")
    public void publicBaseRemoveMethods() {}
    
    @Pointcut("execution(public * pt.ist..*.*_Base.get*(..)) && within(pt.ist..*.*_Base)")
    public void publicBaseGetMethods() {}
    
    @Pointcut("execution(public void pt.ist..*.*_Base.set*(..)) && within(pt.ist..*.*_Base)")
	public void publicBaseSetMethods() {}

	@Pointcut("publicBaseAddMethods() || publicBaseRemoveMethods() || publicBaseGetMethods() || publicBaseSetMethods()")
	public void baseClassMethods() {}

	//----------------------------------------------------------------------------- FENIX FRAMEWORK -----------------------------------------------------------------------------

	@Pointcut("execution(public * pt.ist.fenixframework.FenixFramework.getDomainObject(..))")
	public void fenixFrameworkGetDomainObject() {} 

	// @Pointcut("execution(public * pt.ist.fenixframework.FenixFramework.getDomainModel(..))")
	// public void fenixFrameworkGetDomainModel() {} // This one isn't used on the domain classes

	// @Pointcut("execution(public * pt.ist.fenixframework.FenixFramework.getDomainRoot(..))") //
	// public void fenixFrameworkGetDomainRoot() {}

	@Pointcut("execution(public * pt.ist.fenixframework.DomainRoot_Base.get*(..)) || execution(public * pt.ist.fenixframework.DomainRoot_Base.set*(..))")
	public void gettersAndSettersInDomainRootBase() {}

	@Pointcut("gettersAndSettersInDomainRootBase() || fenixFrameworkGetDomainObject()")
	public void fenixFrameworkGettersAndSetters() {}

	// -------------------------------------------------- OTHER CLASSES (AND METHODS) THAT MAY BE IMPORTANT TO CATCH --------------------------------------------------

	@Pointcut("execution(public * pt.ist.fenixframework.backend.jvstmojb.pstm.AbstractDomainObject.getExternalId(..))")
	public void fenixFrameworkAbstractDomainObjectGetExternalId() {}

	@Pointcut("execution(protected * pt.ist.fenixframework.backend.jvstmojb.pstm.OneBoxDomainObject.deleteDomainObject(..))")
    public void fenixFrameworkAbstractDomainObjectDeleteDomainObject() {}

	@Pointcut("fenixFrameworkAbstractDomainObjectGetExternalId() || fenixFrameworkAbstractDomainObjectDeleteDomainObject()")
	public void otherMethodsThatMayBeImportant() {} // unused

	// -------------------------------------------------------------------- CONTROLLER CLASSES -------------------------------------------------------------------- 
	@Pointcut("(@target(org.springframework.stereotype.Controller) || @target(org.springframework.web.bind.annotation.RestController)) && execution(* *(..))")
	public void controllerClasses() {}

	// -------------------------------------------------------------------- CONTROLLER METHODS --------------------------------------------------------------------
	// Controller GET methods
	@Pointcut("(@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping)) && execution(* *(..))")
	public void controllerGetMethods() {}

	// Controller POST methods
	@Pointcut("(@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping)) && execution(* *(..))")
    public void controllerPostMethods() {}
    
    // Controller PATCH methods
	@Pointcut("@annotation(org.springframework.web.bind.annotation.PatchMapping) && execution(* *(..))")
    public void controllerPatchMethods() {}

    // Controller PUT methods
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping) && execution(* *(..))")
    public void controllerPutMethods() {}

    // Controller DELETE methods
    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping) && execution(* *(..))")
    public void controllerDeleteMethods() {}

    // Controller MESSAGE methods
    @Pointcut("@annotation(org.springframework.messaging.handler.annotation.MessageMapping) && execution(* *(..))")
    public void controllerMessageMethods() {}

	@Pointcut("controllerGetMethods() || controllerPostMethods() || controllerPatchMethods() || controllerPutMethods() || controllerDeleteMethods() || controllerMessageMethods()")
	public void controllerMethods() {}

	// ------------------------------------------------------------------------------ KIEKER METHOD ------------------------------------------------------------------------------

    @Pointcut("(baseClassMethods() && noGettersOrSettersWithFenixFramework()) || fenixFrameworkGettersAndSetters() || otherMethodsThatMayBeImportant()")
	public void domainAndORMMethods() {}

    @Pointcut("(cflow(controllerMethods()) && domainAndORMMethods()) || controllerMethods()")
	public void monitoredOperation() {
		// Aspect Declaration (MUST be empty)
	}
	
}