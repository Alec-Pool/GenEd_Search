# GenEd_Search

## Intro
Students at the Univeristy of Maryland have a variety of general education requirements they must fulfill before they grduate.
I came to notice that most students, including myself, were trying to find the easiest classes possible to cover these requirements.
This task would normally require a large amount of manual work, but I made this Java program to automate the process.<br>

## Technical
### The *Course* Class
- I **developed** a *Course* object which represents a course at UMD.
  - Each *Course* has a department, ID, average GPA, and list of Gen Eds in which they can cover.
- Two *Course* objects are comparable, which is by their average GPA.
- A *Course* is serializable, allowing for instances of them to be written and read from a file.

### The *runner* Class
1. **Sent** HTTP GET Requests to the PlanetTerp API to retrieve the *Average GPA* and *ID* of each course for each department at UMD.
2. Did html webscraping using JSoup 
3. **Processed** JSON formatted data with a third party JSON Parser.
4. **Stored** processed data in self-made Data Structure, a Hash Map which mappings of String -> TreeSet<Course>.
    - The **Key:** *String* represents a Gen Ed from the set, {"FSAW", "FSAR", "FSMA", "FSOC", "FSPW", "DSHS", "DSHU", "DSNS","DSNL", "DSSP", "DVCC", "DVUP", "SCIS"}
    - The **Value:** *TreeSet<Course>* is an ordered Set of *Course* objects, ordered by their average GPA per their *compareTo* function
