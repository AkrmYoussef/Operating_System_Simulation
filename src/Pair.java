public class Pair {
    String variable;
    Object data;

    Pair(String variable, Object data){
        this.variable = variable;
        this.data = data;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "("+variable+","+data+")";
    }

}
