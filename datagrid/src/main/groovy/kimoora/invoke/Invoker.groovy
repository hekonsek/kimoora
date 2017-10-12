package kimoora.invoke

import kimoora.Kimoora

interface Invoker {

    Map<String, Object> invoke(Kimoora kimoora, String operation, Map<String, Object> event)

}