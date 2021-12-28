package Package1;

import java.util.*;
import java.io.*;

public class Course implements Comparable, Serializable{

    private String ID;
    private String department;
    private float averageGPA;
    private ArrayList<String> genEds;

    private static final long serialVersionUID = 1L;


    public Course(String ID, String department, float averageGPA, ArrayList<String> genEds) {
        this.ID = ID;
        this.department = department;
        this.averageGPA = averageGPA;
        this.genEds = genEds;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public float getAverageGPA() {
        return averageGPA;
    }

    public void setAverageGPA(float averageGPA) {
        this.averageGPA = averageGPA;
    }

    public ArrayList<String> getGenEds() {
        return genEds;
    }

    public void setGenEds(ArrayList<String> genEds) {
        this.genEds = genEds;
    }

    public void addGenEd(String genEd) {
        this.genEds.add(new String(genEd));
    }


    @Override
    public String toString() {
        return "Course: " + getDepartment() + getID() + " GPA: " + averageGPA;
    }


    @Override
    public int compareTo(Object o) {
        Course other = (Course)o;

        float thisGPA = this.getAverageGPA();
        float otherGPA = other.getAverageGPA();

        float diff = thisGPA - otherGPA;

        if (diff < 0) {
            return -1;
        } else if (diff > 0) {
            return 1;
        } else { // diff == 0
            return 0;
        }
    }
}
