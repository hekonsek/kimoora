package kimoora

interface Kimoora {

    void registerFunctionDefinition(String function, Map<String, Object> functionDefinition)

    Map<String, Object> getFunctionDefinition(String function)

    Map<String, Object> invoke(String operation, Map<String, Object> event)

}