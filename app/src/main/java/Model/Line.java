package Model;

public class Line {
    private String IDLine;
    private String NameLine;

    public String getIDLine() {
        return IDLine;
    }

    public void setIDLine(String IDLine) {
        this.IDLine = IDLine;
    }

    public String getNameLine() {
        return NameLine;
    }

    public void setNameLine(String nameLine) {
        NameLine = nameLine;
    }

    public Line(String IDLine, String nameLine) {
        this.IDLine = IDLine;
        NameLine = nameLine;
    }

    public Line(){}
}
