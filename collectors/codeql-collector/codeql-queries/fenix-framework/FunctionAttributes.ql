import frameworks.FenixFramework

from CallableFunction function, string paramType
where
    (
        function.getNumberOfParameters() = 0 and
        paramType = "null"
    ) or
    (
        function.getNumberOfParameters() != 0 and
        paramType = function.getAParamType().getName()
    )
select 
    function.getId(),
    function.getDeclaringType(),
    function.getName(),
    function.getReturnType(),
    paramType
