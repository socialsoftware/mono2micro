import python

from Call call, Function f
where 
    f.getScope().inSource() and
    call.getEnclosingModule().toString().matches("%.urls") and
    call.toString().matches("path%") and
    call.getArg(1) instanceof Attribute and
    f.getName() = ((Attribute) call.getArg(1)).getName()
select 
    f.getScope(), 
    f, 
    f.getLocation()