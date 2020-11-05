export function addSearchParamsToUrl(
    endpointURL: string,
    params: Record<string, string | string[]>
): string {
    const urlSearchParams = new URLSearchParams();

    const paramNames = Object.keys(params);
    
    if (paramNames.length) {
        paramNames.forEach(paramName => {
            if (Array.isArray(params[paramName])) {
                (params[paramName] as string[]).forEach(paramValue => 
                    urlSearchParams.append(paramName, paramValue)
                )
            } else {
                urlSearchParams.append(paramName, params[paramName] as string)
            }
        });
        
        endpointURL += `?${urlSearchParams.toString()}`;
    }

    return endpointURL;
}