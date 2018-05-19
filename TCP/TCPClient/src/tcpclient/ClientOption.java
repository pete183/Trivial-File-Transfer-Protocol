package tcpclient;

/**
 * ClientOption
 */
public enum ClientOption {
    Read("1"),
    Write("2"),
    Error("3");

    private String value;

    ClientOption(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

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