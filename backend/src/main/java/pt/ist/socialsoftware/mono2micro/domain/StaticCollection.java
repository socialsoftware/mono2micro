package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pt.ist.socialsoftware.mono2micro.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.AccessDtoDeserializer;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.StaticCollectionDeserializer;

import java.util.HashMap;
import java.util.List;

@JsonDeserialize(using = StaticCollectionDeserializer.class)
public class StaticCollection {
    /* Having to mess around with JSONs in various points is incredibly dirty and confusing. With this class,
    * the collection datafile can be read as an object, facilitating manipulations. Most importantly, it allows for
    * concurrency. */

    private HashMap<String, List<ReducedTraceElementDto>> controllerAccesses = new HashMap<String, List<ReducedTraceElementDto>>();


    public HashMap<String, List<ReducedTraceElementDto>> getControllerAccesses() {
        return controllerAccesses;
    }

    public void addNewControllerAccesses(String controllerName, List<ReducedTraceElementDto> accesses) {
        controllerAccesses.put(controllerName, accesses);
    }

    public List<ReducedTraceElementDto> getAccessesForController(String controllerName) {
        return controllerAccesses.get(controllerName);
    }
}
