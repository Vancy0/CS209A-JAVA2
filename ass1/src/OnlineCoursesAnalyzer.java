import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"checkstyle:Indentation", "checkstyle:MissingJavadocType",
        "checkstyle:MissingJavadocMethod"})
public class OnlineCoursesAnalyzer {
    Stream<Course> courseStream;

    public static class Course {
        CourseBasicInfo courseBasicInfo; //String data information
        CourseDetail courseDetail; //Int data information
        CourseAnalyzer courseAnalyzer; //A part of double data information
        CoursePtcpAnalyzer coursePtcpAnalyzer; //Another part of double data info

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
            double pcOfAudit;
            double hours;

            public SearchCourse(String courseName, double pcOfAudit, double hours) {
                this.courseName = courseName;
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

            public RecommendCourse(String courseId, String courseName, double medianAge,
                                   double pcOfMale, double pcOfBachelor, String launchDate, int age,
                                   int gender, int isBachelorOrHigher) throws ParseException {
                this.courseId = courseId;
                this.courseName = courseName;
                this.medianAge = medianAge;
                this.pcOfMale = pcOfMale;
                this.pcOfBachelor = pcOfBachelor;
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                this.launchDate = format.parse(launchDate);
                calSimilarity(age, gender, isBachelorOrHigher);
            }

            public void calSimilarity(int age, int gender, int isBachelorOrHigher) {
                this.similarity = Math.pow((double) age - this.medianAge, 2)
                        + Math.pow(gender * 100 - this.pcOfMale, 2)
                        + Math.pow(isBachelorOrHigher * 100 - this.pcOfBachelor, 2);
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

        public double getPcOfMale() {
            return coursePtcpAnalyzer.pcOfMale;
        }

        public double getPcOfBachelor() {
            return coursePtcpAnalyzer.pcOfBachelor;
        }

        public String getLaunchDate() {
            return courseBasicInfo.launchDate;
        }
    }


    public OnlineCoursesAnalyzer(String datasetPath) throws IOException {
        this.courseStream = readCourseInfo(datasetPath);
    }

    public Stream<Course> readCourseInfo(String datasetPath) throws IOException {
        return Files.lines(Paths.get(datasetPath))
                .map(l -> l.split(","))
                .filter(p -> p.length == 23)
                .map(Course::new);
    }

    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> instPtcp = courseStream.collect(
                Collectors.groupingBy(Course::getInstitution,
                        Collectors.summingInt(Course::getPtcp)));
        return instPtcp.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new));
    }

    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> instSubjectPtcp = courseStream.map(l ->
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

    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        List<Course.InstructorCourse> instructorsCourse = courseStream.map(l ->
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

    public List<String> getCourses(int topK, String by) {
        List<String> topCourses = new ArrayList<>();
        if (by.equals("hours")) {
            Map<String, Double> tmp;
            tmp = courseStream.map(l ->
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
            tmp = courseStream.map(l ->
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

    public List<String> searchCourses(String courseSubject, double
            percentAudited, double totalCourseHours) {
        String regex = ".*" + courseSubject.toLowerCase() + ".*";
        return courseStream.map(l ->
                        new Course.SearchCourse(l.getCourseTitle(), l.getPcOfAudit(), l.getHours()))
                .filter(obj -> obj.pcOfAudit >= percentAudited)
                .filter(obj -> obj.hours <= totalCourseHours)
                .filter(obj -> obj.courseName.toLowerCase().matches(regex))
                .map(Course.SearchCourse::getCourseName).sorted().toList();
    }

    public List<String> recommendCourses(int age, int gender, int
            isBachelorOrHigher) {
        Map<String, Course.RecommendCourse> filterSameId = courseStream.map(l -> {
            try {
                return new Course.RecommendCourse(l.getCourseId(), l.getCourseTitle(),
                        l.getMidAge(), l.getPcOfMale(), l.getPcOfBachelor(), l.getLaunchDate(),
                        age, gender, isBachelorOrHigher);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toMap(Course.RecommendCourse::getCourseId, Function.identity(),
                (o1, o2) -> o1.launchDate.compareTo(o2.launchDate) > 0 ? o1 : o2));
        List<Course.RecommendCourse> intermediateList = new ArrayList<>(filterSameId.values());
        Map<String, Course.RecommendCourse> filterSameTitle = intermediateList.stream()
                .collect(Collectors.toMap(Course.RecommendCourse::getCourseName,
                        Function.identity(),
                        (o1, o2) -> o1.launchDate.compareTo(o2.launchDate) > 0 ? o1 : o2));
        List<Course.RecommendCourse> filterList = new ArrayList<>(filterSameTitle.values());
        return filterList.stream().sorted(
                Comparator.comparingDouble(Course.RecommendCourse::getSimilarity)
                        .thenComparing(Course.RecommendCourse::getCourseName))
                .limit(10).map(obj -> obj.courseName).toList();
    }
}
