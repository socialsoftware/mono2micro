export function addSearchParamsToUrl(endpointURL: string, params?: Partial<Record<string, string[]>>): string {
    const urlSearchParams = new URLSearchParams();

    if (params) {
        const paramNames = Object.keys(params);

        if (paramNames.length) {
            paramNames.forEach(paramName =>
                params[paramName]!.forEach(paramValue => 
                    urlSearchParams.append(paramName, paramValue)
                )
            );
            
            endpointURL += `?${urlSearchParams.toString()}`;
        }
    }

    return endpointURL;
}