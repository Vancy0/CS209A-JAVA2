import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings({"checkstyle:Indentation", "checkstyle:MissingJavadocType",
        "checkstyle:MissingJavadocMethod"})
public class OnlineCoursesAnalyzer {
    String datasetPath;

    public static class Course {
        CourseBasicInfo courseBasicInfo; //String data information
        CourseDetail courseDetail; //Int data information
        CourseAnalyzer courseAnalyzer; //A part of double data information
        CourseParticipantAnalyzer courseParticipantAnalyzer; //Another part of double data info

        public static class CourseBasicInfo {
            String institution; //online course holders
            String courseNumber; //the unique id of each course
            String launchDate; //the launch date of each course
            String courseTitle; //the title of each course
            String instructors; //the instructors of each course
            String courseSubject; //the subject of each course

            public CourseBasicInfo(String institution, String courseNumber,
                                   String launchDate, String courseTitle,
                                   String instructors, String courseSubject) {
                this.institution = institution;
                this.courseNumber = courseNumber;
                this.launchDate = launchDate;
                this.courseTitle = courseTitle;
                this.instructors = instructors;
                this.courseSubject = courseSubject;
            }
        }

        public static class CourseDetail {
            int year; //the last time of each course
            int honorCodeCertificates; //with (1), without (0)
            int participants; //the number of participants who have accessed the course
            int halfFinishedParticipants; //who have audited more than 50% of the courses
            int certify; //Total number of votes

            public CourseDetail(int year, int honorCodeCertificates, int participants,
                                int halfFinishedParticipants, int certify) {
                this.year = year;
                this.honorCodeCertificates = honorCodeCertificates;
                this.participants = participants;
                this.halfFinishedParticipants = halfFinishedParticipants;
                this.certify = certify;
            }
        }

        public static class CourseAnalyzer {
            double pcOfAudit; //the percent of the audited
            double pcOfCertify; //the percent of the certified
            double pcOfHalfFinishedCertify; //certified with accessing the course more than 50%
            double pcOfPlayedVideo; //the percent of playing video
            double pcOfPostedInForum; //the percent of posting in forum
            double pcOfGradeHigherThanZero; //the percent of grade higher than zero
            double totalCourseHours; //total course hours(per 1000)

            public CourseAnalyzer(double pcOfAudit, double pcOfCertify,
                                  double pcOfHalfFinishedCertify, double pcOfPlayedVideo,
                                  double pcOfPostedInForum, double pcOfGradeHigherThanZero,
                                  double totalCourseHours) {
                this.pcOfAudit = pcOfAudit;
                this.pcOfCertify = pcOfCertify;
                this.pcOfHalfFinishedCertify = pcOfHalfFinishedCertify;
                this.pcOfPlayedVideo = pcOfPlayedVideo;
                this.pcOfPostedInForum = pcOfPostedInForum;
                this.pcOfGradeHigherThanZero = pcOfGradeHigherThanZero;
                this.totalCourseHours = totalCourseHours;
            }
        }

        public static class CourseParticipantAnalyzer {
            double medianHourForCertify; //median hours for certification
            double medianAge; //median age of the participants
            double pcOfMale; //the percent of the male
            double pcOfFemale; //the percent of the female
            double pcOfBachelor; //the percent of bachelor's degree of higher

            public CourseParticipantAnalyzer(double medianHourForCertify, double medianAge,
                                             double pcOfMale, double pcOfFemale,
                                             double pcOfBachelor) {
                this.medianHourForCertify = medianHourForCertify;
                this.medianAge = medianAge;
                this.pcOfMale = pcOfMale;
                this.pcOfFemale = pcOfFemale;
                this.pcOfBachelor = pcOfBachelor;
            }
        }

        public Course(String[] courseInfo) {
            this.courseBasicInfo = new CourseBasicInfo(courseInfo[0], courseInfo[1],
                    courseInfo[2], courseInfo[3], courseInfo[4], courseInfo[5]);
            this.courseDetail = new CourseDetail(Integer.parseInt(courseInfo[6]),
                    Integer.parseInt(courseInfo[7]), Integer.parseInt(courseInfo[8]),
                    Integer.parseInt(courseInfo[9]), Integer.parseInt(courseInfo[10]));
            this.courseAnalyzer = new CourseAnalyzer(Double.parseDouble(courseInfo[11]),
                    Double.parseDouble(courseInfo[12]), Double.parseDouble(courseInfo[13]),
                    Double.parseDouble(courseInfo[14]), Double.parseDouble(courseInfo[15]),
                    Double.parseDouble(courseInfo[16]), Double.parseDouble(courseInfo[17]));
            this.courseParticipantAnalyzer = new CourseParticipantAnalyzer(
                    Double.parseDouble(courseInfo[18]), Double.parseDouble(courseInfo[19]),
                    Double.parseDouble(courseInfo[20]), Double.parseDouble(courseInfo[21]),
                    Double.parseDouble(courseInfo[22]));
        }

    }


    public OnlineCoursesAnalyzer(String datasetPath) {
        this.datasetPath = datasetPath;
    }

    public Stream<Course> readCourseInfo() throws IOException {
        return Files.lines(Paths.get(this.datasetPath))
                .map(l -> l.split(","))
                .filter(p -> p.length == 23)
                .map(Course::new);
    }

    public Map<String, Integer> getPtcpCountByInst() {
        return null;
    }

    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        return null;
    }

    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        return null;
    }

    public List<String> getCourses(int topK, String by) {
        return null;
    }

    public List<String> searchCourses(String courseSubject, double
            percentAudited, double totalCourseHours) {
        return null;
    }

    public List<String> recommendCourses(int age, int gender, int
            isBachelorOrHigher) {
        return null;
    }
}
