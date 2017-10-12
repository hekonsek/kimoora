package kimoora.invoke

import kimoora.server.KimooraServer

interface Invoker {

    Map<String, Object> invoke(KimooraServer kimoora, String operation, Map<String, Object> event)

}