package Package1;

import java.util.*;

public class Course implements Comparable{

    private String ID;
    private String department;
    private float averageGPA;
    private HashSet<String> genEds;


    public Course(String ID, String department, float averageGPA, HashSet<String> genEds) {
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

    public HashSet<String> getGenEds() {
        return genEds;
    }

    public void setGenEds(HashSet<String> genEds) {
        this.genEds = genEds;
    }

    public void addGenEd(String genEd) {
        this.genEds.add(new String(genEd));
    }


    @Override
    public String toString() {
        return "Department: " + getDepartment() + " ID: " + getID() + " GPA: " + averageGPA;
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
