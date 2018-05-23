package client;

/**
 * ClientOption
 */
public enum ClientOption {
    Read("1"),
    Write("2"),
    Error("3");

    /**
     * Private option value
     */
    private String value;

    /**
     * ClientOption Constructor
     * @param value
     */
    ClientOption(final String value) {
        this.value = value;
    }

    /**
     * getValue
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * get
     * Returns the ClientOption related to the value
     * If the value doesn't exist the error option returns
     * @param value
     * @return ClientOption Type
     */
    public static ClientOption get(String value){
        for(ClientOption code: ClientOption.values()){
            if(code.value.equals(value)){
                return code;
            }
        }
        return ClientOption.Error;
    }


    @Override
    public String toString() {
        return this.getValue();
    }
}