package Package1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.*;

public class Main {

  //  static ArrayList<Course> courses = new ArrayList<>();
    static double totalEnrolment = 0;
    static double tempTotalEnrolment = 0;
    // private ArrayList<Integer> grade

    static String[] departments = {"AASP", "AAST", "AGNR","AMSC","AMST","ANSC","ANTH","AOSC","ARAB","ARCH","AREC","ARHU", "ARMY","ARSC","ARTH","ARTT","ASTR",
            "BCHM","BEES","BIOE","BIOL","BIOM","BIPH","BISI","BMGT","BMSO","BSCI","BSCV","BSGC","BSOS","BSST","BUAC","BUDT","BUFN","BULM","BUMK","BUSI","BUSM","BUSO",
            "CBMG","CCJS","CHBE","CHEM","CHIN","CHPH","CHSE","CLAS","CLFS","CMLT","CMSC","COMM","CPBE","CPET","CPGH","CPJT","CPMS","CPPL","CPSA","CPSD","CPSF","CPSG","CPSN","CPSP","CPSS",
            "DANC","DATA",
            "EALL","ECON","EDCP","EDHD","EDHI","EDMS","EDSP","EDUC","ENAE","ENCE","ENCH","ENCO","ENEB","ENEE","ENES","ENFP","ENGL","ENMA","ENME","ENPM","ENRE","ENSE","ENSP","ENST","ENTM","ENTS","EPIB",
            "FGSM","FILM","FIRE","FMSC","FREN",
            "GEMS","GEOG","GEOL","GERM","GREK","GVPT",
            "HACS","HDCC","HEBR","HEIP","HESI","HESP","HHUM","HISP","HIST","HLSA","HLSC","HLTH","HNUH","HONR",
            "IDEA","IMDM","IMMR","INAG","INFM","INST","ISRL","ITAL",
            "JAPN","JOUR","JWST",
            "KNES","KORA",
            "LARC","LASC","LATN","LBSC","LGBT","LING",
            "MAIT","MATH","MEES","MIEH","MITH","MLAW","MLSC","MSBB","MSML","MUED","MUCS",
            "NACS","NAVY","NEUR","NFSC","NIAP","NIAV",
            "PEER","PERS","PHIL","PHPE","PHSC","PHYS","PLCY","PLSC","PORT","PSYC",
            "RDEV","RELS","RUSS",
            "SLAA","SLLC","SMLP","SOCY","SPAN","SPHL","STAT","SURV",
            "TDPS","THET","TLPL","TLTC",
            "UMEI","UNIV","URSP","USLT",
            "VMSC",
            "WMST"};

    static double[] GPAs = new double[10];
    static ArrayList<ArrayList<String>> classArr = new ArrayList<ArrayList<String>>();
    static ArrayList<ArrayList<String>> courseID2DArr = new ArrayList<ArrayList<String>>();//indeces align with departments



    public static void main(String[] args) {
        // make2DClassArr();
        GPAs[0] = 0;
        gradesRequest("Engl101");

        for (int i = 0; i < classArr.size(); i++) {

        }

        //gradesRequestAndParse("ENGL101");
        System.out.println("\n\nTotal Enrolment: " + totalEnrolment + "  GPA: " + GPAs[0]/tempTotalEnrolment);
    }


    public static void make2DCourseIDArr() {
        for (int i = 0; i < departments.length; i++) {
            //classArr.add(departments[i]);
        }
    }


    /* Returns 1 on success */
    // this gets all the course numbers for a department
    public static int courseIDsRequest(String departmentName) {
        int status = 1;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.planetterp.com/v1/courses?department=" + departmentName))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(Main::parseCourseIDs)
                .join();

        return status;
    }

    public static int parseCourseIDs(String responseBody) {
        int status = 1;
        JSONArray albums = new JSONArray(responseBody);

        for (int i = 0; i < albums.length(); i ++) {
            JSONObject album = albums.getJSONObject(i);
            String courseNumber;

            courseNumber = album.getString("course_number");
        }

        return status;
    }


    /* Returns 1 on success */
    public static int gradesRequest(String courseName) {
        int status = 1;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.planetterp.com/v1/grades?course=" + courseName))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(Main::parseGrades)
                .join();

        return status;
    }


    private static String parseGrades(String responseBody) {
        JSONArray albums = new JSONArray(responseBody);
        ArrayList<Integer> grades = new ArrayList<>();
        int totalStudents = 0;

        int A_p, A_, A_m, B_p, B_, B_m, C_p, C_, C_m, D_p, D_, D_m, F_, W_, other = 0, Semester;


        for (int i = 0; i < albums.length(); i++) {
            JSONObject album = albums.getJSONObject(i);

            String professor;

            try {
                professor = album.getString("professor");
            } catch (org.json.JSONException e) {
                professor = "NULL Professor";
            }

            Semester = album.getInt("semester");

            A_p = album.getInt("A+");
            GPAs[0]+= 4*(A_p);


            A_= album.getInt("A");
            GPAs[0]+= 4*A_;

            A_m = album.getInt("A-");
            GPAs[0]+= 3.7*A_m;

            B_p = album.getInt("B+");
            GPAs[0]+= 3.3*B_p;

            B_= album.getInt("B");
            GPAs[0]+= 3*B_;

            B_m = album.getInt("B-");
            GPAs[0]+= 2.7*B_m;

            C_p = album.getInt("C+");
            GPAs[0]+= 2.3*C_p;

            C_ = album.getInt("C");
            GPAs[0]+= 2.0*C_;

            C_m = album.getInt("C-");
            GPAs[0]+= 1.7*C_m;

            D_p = album.getInt("D+");
            GPAs[0]+= 1.3*D_p;

            D_ = album.getInt("D");
            GPAs[0]+= 1*D_;



            D_m = album.getInt("D-");
            GPAs[0]+= 0*D_m;

            F_ = album.getInt("F");
            GPAs[0]+= 0*F_;

            W_ = album.getInt("W");

            other = album.getInt("Other");

            totalStudents = A_p + A_ + A_m + B_p + B_ + B_m + C_p + C_ + C_m +
                    D_p + D_ + D_m + F_ + W_ + other;

            int tempTotalStudents = A_p + A_ + A_m + B_p + B_ + B_m + C_p + C_ + C_m +
                    D_p + D_ + D_m + F_ ;

            totalEnrolment += totalStudents;
            tempTotalEnrolment =+ tempTotalStudents;

            System.out.print(professor + ":");


            for (int j = 0; j < 30 - professor.length(); j++)
                System.out.print(" ");

            int strLength = 5;
            String finalSemester;
            String tempSemester = String.valueOf(Semester);
            if ((tempSemester.substring(strLength)).equals("8")) {
                finalSemester = "Fall " + tempSemester.substring(0, strLength - 1);
            } else {
                finalSemester = "Spring " + tempSemester.substring(0, strLength - 1);
            }


            System.out.print("A+: " + A_p + " A: " + A_ + " A-: " + A_m +
                    " B+: " + B_p + " B: " + B_ + " B-: " + B_m +
                    " C+: " + C_p + " C: " + C_ + " C-: " + C_m +
                    " D+: " + D_p + " D: " + D_ + " D-: " + D_m +
                    " F: " + F_ + " W: " + W_ + " Other: " + other +
                    " Total Students: " + totalStudents +
                    " Semester: " + finalSemester + "\n");

        }

        return null;

    }



}
