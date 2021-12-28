package Package1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class runner {


    final private static String[] genEdsArr = {"FSAW", "FSAR", "FSMA", "FSOC", "FSPW", "DSHS", "DSHU", "DSNS","DSNL", "DSSP", "DVCC", "DVUP", "SCIS"};
    final private static Collection<String> genEdsList = new ArrayList<String>(Arrays.asList(genEdsArr));
    final private static HashSet<String> genEdsSet = new HashSet<>(genEdsList);


    private static ArrayList<String> departments = new ArrayList<>(); // pulling data from departments.txt
    private static HashMap<String, TreeSet<Course>> masterMap = new HashMap<>();



    public static void main(String[] args) {
        // Only uncomment when making a new masterMap
        /*
        loadDepartments();
        createMasterMap();

        WriteObjectToFile(System.getProperty("user.dir") + "/masterMap" ,masterMap);
        */

        // Reads the masterMap object from the file masterMap
        masterMap = (HashMap<String, TreeSet<Course>>) ReadObjectFromFile(System.getProperty("user.dir") +  "/masterMap");
        startSession();
    }

    public static void loadDepartments() {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("departments.txt"));
            String line = reader.readLine();

            while (line != null) {
                departments.add(line);
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Departments Successfully Loaded");
    }


    /* Add the new course to a hash map of genEd -> sorted TreeSet of courses by average GPA */
    private static void addCourse(Course newCourse) {
        for (String currGenEd : newCourse.getGenEds()) {
            if (masterMap.containsKey(currGenEd)) {
                TreeSet<Course> temp = masterMap.get(currGenEd);
                temp.add(newCourse);
            } else {
                TreeSet<Course> temp = new TreeSet<>();
                temp.add(newCourse);
                masterMap.put(currGenEd, temp);
            }
        }
    }



    /* Gets the course data for every course in a department */
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


    /* Parses the course data body from the departmentDataRequest */
    private static String parseDepartment(String responseBody) {
        JSONArray albums = null;
        try {
            albums = new JSONArray(responseBody);
        } catch (Exception e) {
            return null;
        }

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

            if (Integer.parseInt(ID.substring(0, 1)) < 5) {/* scrape the testudo page for the gen eds of the current course */
                try {
                    String courseID = department + ID;
                    Document doc = Jsoup.connect("https://app.testudo.umd.edu/soc/search?courseId=" + courseID + "&sectionId=&termId=202201&_openSectionsOnly=on&creditCompare=%3E%3D&credits=0.0&courseLevelFilter=ALL&instructor=&_facetoface=on&_blended=on&_online=on&courseStartCompare=&courseStartHour=&courseStartMin=&courseStartAM=&courseEndHour=&courseEndMin=&courseEndAM=&teachingCenter=ALL&_classDay1=on&_classDay2=on&_classDay3=on&_classDay4=on&_classDay5=on").userAgent("Mozilla/17.0").get();
                    Elements temp = doc.select("span.course-subcategory");

                    for (Element edsList : temp) {
                        genEds.add(edsList.getElementsByTag("a").first().text());
                    }
                    System.out.println(i + " " + department + ID + " Gen Eds: " + genEds);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Course newCourse = new Course(ID, department, averageGPA, genEds);

                /* Add the new course to a hash map of genEd -> sorted TreeSet of courses by av GPA */
                addCourse(newCourse);
            }

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
            System.out.print("List of Gen Eds: " + genEdsSet);
            System.out.print("\nEnter a Valid Gen Ed, or \"exit\" to finish: ");
            String next = scanner.next().toUpperCase();

            if (next.equalsIgnoreCase("exit")){
                System.out.println("Goodbye");
                done = true;
            }
            else if (!genEdsSet.contains(next)) {
                System.out.println("Invalid Gen-Ed, please try again");
            } else {
                System.out.println("Valid Gen-Ed Given, Printing Ordered List of Classes by GPA For " + next + ":");
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
}
