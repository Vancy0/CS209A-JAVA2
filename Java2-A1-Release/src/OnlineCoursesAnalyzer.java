package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is just a demo for you, please run it on JDK17.
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) throws IOException {
        readCourseInfo(datasetPath);
    }

    public void readCourseInfo(String datasetPath) throws IOException {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info);
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> instPtcp = courses.stream().collect(
                Collectors.groupingBy(Course::getInstitution,
                        Collectors.summingInt(Course::getPtcp)));
        return instPtcp.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new));
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> instSubjectPtcp = courses.stream().map(l ->
                        new Course.InstSubject(l.getInstSubject(), l.getPtcp()))
                .collect(Collectors.groupingBy(Course.InstSubject::getInstSubject,
                        Collectors.summingInt(Course.InstSubject::getPtcp)));
        return instSubjectPtcp.entrySet().stream().sorted(
                        Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed()
                                .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new));
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        List<Course.InstructorCourse> instructorsCourse = courses.stream().map(l ->
                        new Course.InstructorCourse(l.getInstructors(), l.getCourseTitle()))
                .toList();
        List<Course.InstructorCourse> instorCourseList = new ArrayList<>();
        for (Course.InstructorCourse instructorCourse : instructorsCourse) {
            String[] keys = instructorCourse.instructors.split(", ");
            String value = instructorCourse.courseName;
            if (keys.length == 1) {
                instorCourseList.add(new Course.InstructorCourse(keys[0], value, 1));
            } else {
                for (String key : keys) {
                    instorCourseList.add(new Course.InstructorCourse(key, value));
                }
            }
        }

        Map<String, List<Map<Integer, String>>> res = instorCourseList.stream().distinct().collect(
                Collectors.groupingBy(Course.InstructorCourse::getInstructors,
                        Collectors.mapping(a -> {
                            Map<Integer, String> map = new HashMap<>();
                            map.put(a.single, a.courseName);
                            return map;
                        }, Collectors.toList())));
        Map<String, List<List<String>>> courseListOfInstructor = new HashMap<>();
        for (Map.Entry<String, List<Map<Integer, String>>> entry : res.entrySet()) {
            List<Map<Integer, String>> mapValue = entry.getValue();
            List<String> list0 = new ArrayList<>();
            List<String> list1 = new ArrayList<>();
            for (Map<Integer, String> integerStringMap : mapValue) {
                if (integerStringMap.containsKey(1)) {
                    list0.add(integerStringMap.get(1));
                } else {
                    list1.add(integerStringMap.get(1));
                }
            }
            List<List<String>> lists = new ArrayList<>();
            lists.add(list0);
            lists.add(list1);
            String mapKey = entry.getKey();
            courseListOfInstructor.put(mapKey, lists);
        }
        return courseListOfInstructor;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        List<String> topCourses = new ArrayList<>();
        if (by.equals("hours")) {
            Map<String, Double> tmp;
            tmp = courses.stream().map(l ->
                            new Course.TopCourse(l.getCourseTitle(), l.getHours(), l.getPtcp()))
                    .collect(Collectors.groupingBy(Course.TopCourse::getCourseName,
                            Collectors.summingDouble(Course.TopCourse::getHours)));
            topCourses = tmp.entrySet().stream().sorted(
                            Comparator.comparing(Map.Entry<String, Double>::getValue).reversed()
                                    .thenComparing(Map.Entry::getKey)).limit(topK)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } else {
            Map<String, Integer> tmp;
            tmp = courses.stream().map(l ->
                            new Course.TopCourse(l.getCourseTitle(), l.getHours(), l.getPtcp()))
                    .collect(Collectors.groupingBy(Course.TopCourse::getCourseName,
                            Collectors.summingInt(Course.TopCourse::getPtcp)));
            topCourses = tmp.entrySet().stream().sorted(
                            Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed()
                                    .thenComparing(Map.Entry::getKey)).limit(topK)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
        return topCourses;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        String regex = ".*" + courseSubject.toLowerCase() + ".*";
        return courses.stream().map(l ->
                        new Course.SearchCourse(l.getCourseTitle(), l.getCourseSubject()
                                , l.getPcOfAudit(), l.getHours()))
                .filter(obj -> obj.pcOfAudit >= percentAudited)
                .filter(obj -> obj.hours <= totalCourseHours)
                .filter(obj -> obj.courseSubject.toLowerCase().matches(regex))
                .map(Course.SearchCourse::getCourseName).distinct().sorted().toList();
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        /*for (int i = 0; i < this.courses.size(); i++) {
            if (courses.get(i).getCourseId().equals("PH525.2x")){
                System.out.println(courses.get(i).courseBasicInfo.courseTitle);
            }
        }*/
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).avgPcOfMale != 0) continue;
            String id = courses.get(i).courseBasicInfo.courseNumber;
            List<Course> tmp = courses.stream()
                    .filter(o -> o.courseBasicInfo.courseNumber.equals(id))
                    .toList();
            double totalAge = 0;
            double totalPcMale = 0;
            double totalPcBachelor = 0;
            for (Course course : tmp) {
                totalAge += course.getMidAge();
                totalPcMale += course.getPcOfMale();
                totalPcBachelor += course.getPcOfBachelor();
            }
            double avgAge = totalAge / tmp.size();
            double avgMale = totalPcMale / tmp.size();
            double avgBachelor = totalPcBachelor / tmp.size();
            for (Course course : tmp) {
                course.setAvgMidAge(avgAge);
                course.setAvgPcOfMale(avgMale);
                course.setAvgPcOfBachelor(avgBachelor);
            }
        }
        Map<String, Course.RecommendCourse> filterSameId = courses.stream().map(l -> {
            try {
                return new Course.RecommendCourse(l.getCourseId(), l.getCourseTitle(),
                        l.getMidAge(), l.getPcOfMale(), l.getPcOfBachelor(), l.getLaunchDate(),
                        age, gender, isBachelorOrHigher, l.getAvgMidAge(), l.getAvgPcOfMale(),
                        l.getAvgPcOfBachelor());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toMap(Course.RecommendCourse::getCourseId, Function.identity(),
                (o1, o2) -> o1.launchDate.compareTo(o2.launchDate) > 0 ? o1 : o2));
        /*for (String id : filterSameId.keySet()) {
            if (filterSameId.get(id).courseId.equals("PH525.2x")){
                System.out.println(filterSameId.get(id).courseName);
            }
        }*/
        List<Course.RecommendCourse> intermediateList = new ArrayList<>(filterSameId.values());
        Map<String, Course.RecommendCourse> filterSameTitle = intermediateList.stream()
                .collect(Collectors.toMap(Course.RecommendCourse::getCourseName,
                        Function.identity(),
                        (o1, o2) -> o1.similarity - o2.similarity > 0 ? o2 : o1));
        /*for (String id : filterSameTitle.keySet()) {
            if (filterSameTitle.get(id).courseId.equals("PH525.2x")) {
                System.out.println(filterSameTitle.get(id).courseName);
            }
        }*/
        List<Course.RecommendCourse> filterList = new ArrayList<>(filterSameTitle.values());
        /*List<Course.RecommendCourse> a = filterList.stream().sorted(
                        Comparator.comparingDouble(Course.RecommendCourse::getSimilarity)
                                .thenComparing(Course.RecommendCourse::getCourseName))
                .limit(10).toList();
        for (int i = 0; i < a.size(); i++) {
            Course.RecommendCourse tmp = a.get(i);
            System.out.println(tmp.courseId+": "+tmp.courseName+": "+tmp.similarity);
        }*/
        return filterList.stream().sorted(
                        Comparator.comparingDouble(Course.RecommendCourse::getSimilarity)
                                .thenComparing(Course.RecommendCourse::getCourseName))
                .limit(10).map(obj -> obj.courseName).toList();
    }

}

class Course {
    CourseBasicInfo courseBasicInfo; //String data information
    CourseDetail courseDetail; //Int data information
    CourseAnalyzer courseAnalyzer; //A part of double data information
    CoursePtcpAnalyzer coursePtcpAnalyzer; //Another part of double data info
    double avgMidAge = 0;
    double avgPcOfMale = 0;
    double avgPcOfBachelor = 0;

    public static class CourseBasicInfo {
        String institution; //online course holders
        String courseNumber; //the unique id of each course
        String launchDate; //the launch date of each course
        String courseTitle; //the title of each course
        String instructors; //the instructors of each course
        String courseSubject; //the subject of each course

        public CourseBasicInfo(String institution, String courseNumber,
                               String launchDate, String title,
                               String instructors, String subject) {
            this.institution = institution;
            this.courseNumber = courseNumber;
            this.launchDate = launchDate;
            if (title.startsWith("\"")) title = title.substring(1);
            if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
            this.courseTitle = title;
            if (instructors.startsWith("\"")) instructors = instructors.substring(1);
            if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
            this.instructors = instructors;
            if (subject.startsWith("\"")) subject = subject.substring(1);
            if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
            this.courseSubject = subject;
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

    public static class CoursePtcpAnalyzer {
        double medianHourForCertify; //median hours for certification
        double medianAge; //median age of the participants
        double pcOfMale; //the percent of the male
        double pcOfFemale; //the percent of the female
        double pcOfBachelor; //the percent of bachelor's degree of higher

        public CoursePtcpAnalyzer(double medianHourForCertify, double medianAge,
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
        this.coursePtcpAnalyzer = new CoursePtcpAnalyzer(
                Double.parseDouble(courseInfo[18]), Double.parseDouble(courseInfo[19]),
                Double.parseDouble(courseInfo[20]), Double.parseDouble(courseInfo[21]),
                Double.parseDouble(courseInfo[22]));
    }

    public static class InstSubject {
        String instSubject;
        int ptcp;

        public InstSubject(String instSubject, int ptcp) {
            this.instSubject = instSubject;
            this.ptcp = ptcp;
        }

        public String getInstSubject() {
            return instSubject;
        }

        public int getPtcp() {
            return ptcp;
        }
    }

    public static class InstructorCourse {
        String instructors;
        String courseName;
        int single;

        public InstructorCourse(String instructors, String courseName) {
            this.instructors = instructors;
            this.courseName = courseName;
        }

        public InstructorCourse(String instructors, String courseName, int single) {
            this.instructors = instructors;
            this.courseName = courseName;
            this.single = single;
        }

        public String getInstructors() {
            return instructors;
        }

        public String getCourseName() {
            return courseName;
        }
    }

    public static class TopCourse {
        String courseName;
        double hours;
        int ptcp;

        public TopCourse(String courseName, double hours, int ptcp) {
            this.courseName = courseName;
            this.hours = hours;
            this.ptcp = ptcp;
        }

        public String getCourseName() {
            return courseName;
        }

        public double getHours() {
            return hours;
        }

        public int getPtcp() {
            return ptcp;
        }
    }

    public static class SearchCourse {
        String courseName;
        String courseSubject;
        double pcOfAudit;
        double hours;

        public SearchCourse(String courseName, String courseSubject, double pcOfAudit, double hours) {
            this.courseName = courseName;
            this.courseSubject = courseSubject;
            this.pcOfAudit = pcOfAudit;
            this.hours = hours;
        }

        public String getCourseName() {
            return courseName;
        }

        public double getPcOfAudit() {
            return pcOfAudit;
        }

        public double getHours() {
            return hours;
        }
    }

    public static class RecommendCourse {
        String courseId;
        String courseName;
        double medianAge;
        double pcOfMale;
        double pcOfBachelor;
        double similarity;
        Date launchDate;
        double avgMidAge;
        double avgPcOfMale;
        double avgPcOfBachelor;
        public RecommendCourse(String courseId, String courseName, double medianAge, double pcOfMale,
                               double pcOfBachelor, String launchDate, int age,
                               int gender, int isBachelorOrHigher, double avgMidAge,
                               double avgPcOfMale, double avgPcOfBachelor) throws ParseException {
            this.courseId = courseId;
            this.courseName = courseName;
            this.medianAge = medianAge;
            this.pcOfMale = pcOfMale;
            this.pcOfBachelor = pcOfBachelor;
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            this.launchDate = format.parse(launchDate);
            this.avgMidAge = avgMidAge;
            this.avgPcOfMale = avgPcOfMale;
            this.avgPcOfBachelor = avgPcOfBachelor;
            calSimilarity(age, gender, isBachelorOrHigher);
        }

        public void calSimilarity(int age, int gender, int isBachelorOrHigher) {
            this.similarity = Math.pow((double) age - this.avgMidAge, 2)
                    + Math.pow((double) gender * 100 - this.avgPcOfMale, 2)
                    + Math.pow((double) isBachelorOrHigher * 100 - this.avgPcOfBachelor, 2);
        }

        public double getSimilarity() {
            return similarity;
        }

        public String getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }



    }

    public String getInstitution() {
        return courseBasicInfo.institution;
    }

    public int getPtcp() {
        return courseDetail.participants;
    }

    public String getInstSubject() {
        return courseBasicInfo.institution + "-" + courseBasicInfo.courseSubject;
    }

    public String getInstructors() {
        return courseBasicInfo.instructors;
    }

    public String getCourseTitle() {
        return courseBasicInfo.courseTitle;
    }

    public double getHours() {
        return courseAnalyzer.totalCourseHours;
    }

    public double getPcOfAudit() {
        return courseAnalyzer.pcOfAudit;
    }

    public String getCourseId() {
        return courseBasicInfo.courseNumber;
    }

    public double getMidAge() {
        return coursePtcpAnalyzer.medianAge;
    }

    public double getAvgMidAge() {
        return avgMidAge;
    }

    public double getAvgPcOfMale() {
        return avgPcOfMale;
    }

    public double getAvgPcOfBachelor() {
        return avgPcOfBachelor;
    }

    public double getPcOfMale() {
        return coursePtcpAnalyzer.pcOfMale;
    }

    public double getPcOfBachelor() {
        return coursePtcpAnalyzer.pcOfBachelor;
    }

    public String getLaunchDate() {
        return courseBasicInfo.launchDate;
    }

    public void setAvgMidAge(double avgMidAge) {
        this.avgMidAge = avgMidAge;
    }

    public void setAvgPcOfMale(double avgPcOfMale) {
        this.avgPcOfMale = avgPcOfMale;
    }

    public void setAvgPcOfBachelor(double avgPcOfBachelor) {
        this.avgPcOfBachelor = avgPcOfBachelor;
    }

    public String getCourseSubject(){
        return this.courseBasicInfo.courseSubject;
    }
}
/*
class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}*/
