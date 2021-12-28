package Package1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.*;

public class runner {

    //  static ArrayList<Course> courses = new ArrayList<>();
    private static double totalEnrolment = 0;
    private static double totalNonWStudents = 0;
    // private ArrayList<Integer> grade


    private static ArrayList<String> departments = new ArrayList<>(); // pulling data from departments.txt

    // should each gen ed have its own TreeSet? Map of Gen Ed ID (DSHU...) to a sorted tree set of classes with that gen ed

    private static String[] firstArr = {"FSAW", "FSAR", "FSMA", "FSOC", "FSPW", "DSHS", "DSHU", "DSNS","DSNL", "DSSP", "DVCC", "DVUP", "SCIS"};
    private static Collection<String> genEdsArr = new ArrayList<String>(Arrays.asList(firstArr));
    private static HashSet<String> eds = new HashSet<>(genEdsArr);

    private static HashMap<String, TreeSet<Course>> masterMap = new HashMap<>();

    private static HashSet<String> genEds = new HashSet<>();

    //static double[] GPAs = new double[10];
    //static ArrayList<ArrayList<String>> classArr = new ArrayList<ArrayList<String>>();
    //static ArrayList<ArrayList<String>> courseID2DArr = new ArrayList<ArrayList<String>>();//indeces align with departments


    public static void main(String[] args) {
        // Only uncomment when making a new masterMap
        /*
        loadDepartments();
        createMasterMap();

        WriteObjectToFile(System.getProperty("user.dir") + "/masterMap" ,masterMap);
        */

        // Reads the masterMap from the file masterMap
        masterMap = (HashMap<String, TreeSet<Course>>) ReadObjectFromFile(System.getProperty("user.dir") +  "/masterMap");
        startSession();
    }

    public static void loadDepartments() {
        BufferedReader reader;

        // test
        //int i = 0;
        try {
            reader = new BufferedReader(new FileReader("departments.txt"));
            String line = reader.readLine();

            while (line != null) {// && i < 3) {
                //i++; //test
                departments.add(line);

                // read next line
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Departments Successfully Loaded");
    }




    // add the new course to a hash map of genEd -> sorted TreeSet of courses by av GPA
    private static void addCourse(Course newCourse) {
        for (String currGenEd : newCourse.getGenEds()) {
            if (masterMap.containsKey(currGenEd)) {
                TreeSet temp = masterMap.get(currGenEd);
                temp.add(newCourse);
                //masterMap.put(currGenEd, temp);
            } else {
                TreeSet temp = new TreeSet<>();
                temp.add(newCourse);
                masterMap.put(currGenEd, temp);
            }
        }
    }



    /*
    Gets the course data for every course in a department
     */
    private static int departmentDataRequest(String department) {
        int status = 1;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.planetterp.com/v1/courses?department=" + department + "&limit=1000")) //courses?department=ENGL&limit=1000
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(runner::parseDepartment)
                .join();

        return status;
    }


    /*
    Parses the course data body from the departmentDataRequest
     */
    private static String parseDepartment(String responseBody) {
        JSONArray albums = null;
        try {
            albums = new JSONArray(responseBody);
        } catch (Exception e) {
            return null;
        }

        ArrayList<Integer> grades = new ArrayList<>();
        int totalStudents = 0;

        for (int i = 0; i < albums.length(); i++) {
            // each album is a course in the @param department
            JSONObject album = albums.getJSONObject(i);

            String ID = album.getString("course_number");
            String department = album.getString("department");
            float averageGPA;
            try {
                averageGPA = album.getFloat("average_gpa");
            } catch (Exception e) {
                continue;
            }
            ArrayList<String> genEds = new ArrayList<>();

            if (Integer.valueOf(ID.substring(0,1)) >= 5) {
                continue;
            }

            // scrape the testudo page for the gen eds of the current class
            try {
                String courseID = department + ID;
                Document doc = Jsoup.connect("https://app.testudo.umd.edu/soc/search?courseId="+courseID+"&sectionId=&termId=202201&_openSectionsOnly=on&creditCompare=%3E%3D&credits=0.0&courseLevelFilter=ALL&instructor=&_facetoface=on&_blended=on&_online=on&courseStartCompare=&courseStartHour=&courseStartMin=&courseStartAM=&courseEndHour=&courseEndMin=&courseEndAM=&teachingCenter=ALL&_classDay1=on&_classDay2=on&_classDay3=on&_classDay4=on&_classDay5=on").userAgent("Mozilla/17.0").get();
                Elements temp = doc.select("span.course-subcategory");

                for (Element edsList : temp) {
                    genEds.add(edsList.getElementsByTag("a").first().text());
                }
                System.out.println(i + " " + department + ID + " Gen Eds: " +genEds);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Course newCourse = new Course(ID,department,averageGPA,genEds);

            // add the new course to a hash map of genEd -> sorted TreeSet of courses by av GPA
            addCourse(newCourse);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void createMasterMap() {
        int i = 0;
        for (String department : departments) {
            departmentDataRequest(department);
            System.out.println(i);
        }
    }

    private static void startSession() {
        boolean done = false;
        Scanner scanner = new Scanner(System.in);

        while (!done) {
            System.out.print("List of Gen Eds: " + eds.toString());
            System.out.print("\nEnter a gen ed: ");
            String next = scanner.next().toUpperCase();

            if (next.equals("exit")){
                System.out.println("Goodbye");
                break;
            }
            else if (!eds.contains(next)) {
                System.out.println("Invalid Gen-Ed, please try again");
                continue;
            } else {
                System.out.println("Valid Gen-Ed Given, Printing Ordered List of Classes by GPA For "+ next +":");
                System.out.println(next);
                try{
                    System.out.println(masterMap.get(next).descendingSet());
                } catch (NullPointerException e) {
                    System.out.println("Error: No Classes With This Gen Ed");
                }
            }
        }
    }

    public static void WriteObjectToFile(String filepath,Object serObj) {
        try {

            FileOutputStream fileOut = new FileOutputStream(filepath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.close();
            System.out.println("The Master Map was succesfully written to a file");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Object ReadObjectFromFile(String filepath) {
        try {

            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);

            Object obj = objectIn.readObject();

            System.out.println("The Master Map has been read from the file");
            objectIn.close();
            return obj;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
















    // course data request (

    // course data parse


    // get gen eds from testudo

   // https://app.testudo.umd.edu/soc/search?courseId=GEOG330&sectionId=&termId=202201&_openSectionsOnly=on&creditCompare=&credits=&courseLevelFilter=ALL&instructor=&_facetoface=on&_blended=on&_online=on&courseStartCompare=&courseStartHour=&courseStartMin=&courseStartAM=&courseEndHour=&courseEndMin=&courseEndAM=&teachingCenter=ALL&_classDay1=on&_classDay2=on&_classDay3=on&_classDay4=on&_classDay5=on















    /*
    public static void make2DCourseIDArr() {
        for (int i = 0; i < departments.size(); i++) {
            //classArr.add(departments[i]);
        }
    }
    */

    /* Returns 1 on success */
    // this gets all the course numbers for a department
    /*
    public static int courseNumbersRequest(String departmentName) {
        int status = 1;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.planetterp.com/v1/courses?department=" + departmentName))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(runner::parseCourseNumbers)
                .join();

        return status;
    }
    */


    /*
    Parses the courseIDRequest into JSONArray albums of

    Returns 1 on success.
     */
    /*
    public static int parseCourseNumbers(String responseBody) {
        int status = 1;
        JSONArray albums = new JSONArray(responseBody);

        for (int i = 0; i < albums.length(); i ++) {
            JSONObject album = albums.getJSONObject(i);
            String courseNumber;

            courseNumber = album.getString("course_number");

            // need to do something with the course number, put in datastructure or something
        }

        return status;
    }
    */

    /* Returns 1 on success */
    /*
    Retrieves the course grade data for
    @Param courseName, in format "<department><courseNumber>" ex:MATH140
     */
    /*
    public static int gradesRequest(String courseName) {
        int status = 1;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.planetterp.com/v1/grades?course=" + courseName))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(runner::parseGrades)
                .join();

        return status;
    }
    */

    /*
    Parses the grades from the gradesRequest
     */

    /*
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
            GPAs[0]+= 0.7*D_m;

            F_ = album.getInt("F");
            GPAs[0]+= 0*F_;

            W_ = album.getInt("W");

            other = album.getInt("Other");

            totalStudents = A_p + A_ + A_m + B_p + B_ + B_m + C_p + C_ + C_m +
                    D_p + D_ + D_m + F_ + W_ + other;

            int nonWStudents = A_p + A_ + A_m + B_p + B_ + B_m + C_p + C_ + C_m +
                    D_p + D_ + D_m + F_ ;

            totalEnrolment += totalStudents;
            totalNonWStudents += nonWStudents;

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
    */






}
