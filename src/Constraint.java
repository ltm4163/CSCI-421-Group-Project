public class Constraint implements IConstraint{
    private String constraint;

    public Constraint(String constraint){
        this.constraint=constraint;
    }
    public void setConstraint(String constraint){
        this.constraint=constraint;
    }
    public String getConstraint(){
        return this.constraint;
    }
}
