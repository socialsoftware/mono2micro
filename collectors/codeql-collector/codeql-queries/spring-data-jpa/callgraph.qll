import java
import entity

predicate callInsideCaller(Callable caller, Call call) {
  caller.getLocation().getFile() = call.getLocation().getFile() and
  call.getLocation().getStartLine() >= caller.getBody().getLocation().getStartLine() and
  call.getLocation().getEndLine() <= caller.getBody().getLocation().getEndLine()
}

predicate callablesHaveNames(Callable caller, Callable callee) {
  not caller.getDeclaringType().toString().matches("new %") and
  not caller.toString().matches("<obinit>") and
  not caller.toString().matches("Object") and
  not callee.getDeclaringType().toString().matches("new %") and
  not callee.toString().matches("<obinit>") and
  not callee.toString().matches("Object")
}

predicate callsCallee(Callable c, Call call) {
  call.getCallee() = c or
  (
    c instanceof Constructor and call.getCallee() = c
  )
  or exists(Method m, Method override |
    call.getCallee() = m and
    m.isAbstract() and
    override.overrides(m) and
    c = override
  )
}

predicate isControllerMethod(Method cal) {
  exists(Class c |
    cal.getDeclaringType() = c and
    (
      (
        cal.hasAnnotation("org.springframework.web.bind.annotation", "RequestMapping") or
        cal.hasAnnotation("org.springframework.web.bind.annotation", "GetMapping") or
        cal.hasAnnotation("org.springframework.web.bind.annotation", "PostMapping") or
        cal.hasAnnotation("org.springframework.web.bind.annotation", "PatchMapping") or
        cal.hasAnnotation("org.springframework.web.bind.annotation", "PutMapping") or
        cal.hasAnnotation("org.springframework.web.bind.annotation", "DeleteMapping")
      ) and
      (
        c.hasAnnotation("org.springframework.web.bind.annotation", "RestController") or
        c.hasAnnotation("org.springframework.web.bind.annotation", "RequestMapping") or
        c.hasAnnotation("org.springframework.stereotype", "Controller")
      )
    )
  )
  or (
    exists(Class c, Class sup |
      cal.getDeclaringType() = c and
      c.getASupertype() = sup and
      sup.getName() = "DispatchAction"
    )
  )
}