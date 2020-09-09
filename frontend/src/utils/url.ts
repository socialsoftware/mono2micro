export function addSearchParamsToUrl(endpointURL: string, params: Record<string, string[]>): string {
    const urlSearchParams = new URLSearchParams();

    const paramNames = Object.keys(params);
    
    if (paramNames.length) {
        paramNames.forEach(paramName =>
            params[paramName]!.forEach(paramValue => 
                urlSearchParams.append(paramName, paramValue)
            )
        );
        
        endpointURL += `?${urlSearchParams.toString()}`;
    }

    return endpointURL;
}