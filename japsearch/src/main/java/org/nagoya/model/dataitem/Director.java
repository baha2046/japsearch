package org.nagoya.model.dataitem;


public class Director extends Person {

    private static final long serialVersionUID = 6191933432944186334L;

    public Director(String name, FxThumb thumb) {
        super(name, thumb);
    }

    public Director() {
        super();
    }

    @Override
    public String toXML() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        return "Director [toString()=" + super.toString() + " ,\"" + this.dataItemSourceToString() + "\"]";
    }

}
