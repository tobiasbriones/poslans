/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.models.career.curriculum;

import dev.tobiasbriones.poslans.mu.models.career.Course;
import org.json.JSONArray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Provides an experimental data structure implementation for a student
 * career curriculum.
 */
public final class CareerCurriculum implements Iterable<CourseNode> {
    public static final class CurriculumCourses {
        public final int careerId;
        public final Map<String, Course> careerCourses;
        public final Map<String, Course> generalCourses;
        public final Map<String, Course> otherCourses;

        public CurriculumCourses(int careerId) {
            this.careerId = careerId;
            this.careerCourses = new HashMap<>();
            this.generalCourses = new HashMap<>();
            this.otherCourses = new HashMap<>();
        }
    }

    public static CareerCurriculum newCurriculum(
        String careerName,
        Map<String, Course> courses,
        ResultSet resultSet
    ) throws SQLException {
        final CourseNode[] nodes = new CourseNode[courses.size()];
        final Map<String, Integer> nodesMap = new HashMap<>();
        final StringBuilder requirementCodesBuilder = new StringBuilder();
        int levels = 0;
        int currentLevel = -1;
        Course currentCourse = null;
        JSONArray currentRequiredCoursesArray = null;
        JSONArray currentNextCoursesArray = null;
        int i = 0;

        while (resultSet.next()) {
            currentLevel = resultSet.getInt("level");
            currentCourse = courses.get(resultSet.getString("course_code"));
            currentRequiredCoursesArray = new JSONArray(resultSet.getString
                                                                     ("required_courses"));
            currentNextCoursesArray = new JSONArray(resultSet.getString
                                                                 ("next_courses"));
            final List<Course> currentRequiredCourses = new ArrayList<>();
            final List<Course> currentNextCourses = new ArrayList<>();

            requirementCodesBuilder.setLength(0);
            if ((currentLevel + 1) != levels) {
                levels++;
            }
            currentRequiredCoursesArray.forEach(courseCode -> {
                currentRequiredCourses.add(courses.get(courseCode));
                requirementCodesBuilder.append(courseCode);
                requirementCodesBuilder.append(" ");
            });
            currentNextCoursesArray.forEach(courseCode -> currentNextCourses
                .add(courses.get(courseCode)));
            nodes[i] = new CourseNode(
                currentLevel,
                currentCourse,
                Collections.unmodifiableList
                               (currentRequiredCourses),
                Collections.unmodifiableList
                               (currentNextCourses),
                requirementCodesBuilder.toString()
            );
            nodesMap.put(currentCourse.getCode(), i);
            i++;
        }
        System.out.println(nodes.length);
        if (i != courses.size()) {
            return new CareerCurriculum(careerName, new CourseNode[0], new
                HashMap<>(), levels);
        }
        return new CareerCurriculum(careerName, nodes, nodesMap, levels);
    }

    public static CareerCurriculum newCurriculum(
        String careerName,
        CurriculumCourses coursesHolder
    ) {
        return null;
    }

    private final String careerName;
    private final CourseNode[] nodes;
    private final Map<String, Integer> nodesMap;
    private final int levels;

    private CareerCurriculum(
        String careerName,
        CourseNode[] nodes,
        Map<String, Integer> nodesMap,
        int levels
    ) {
        this.careerName = careerName;
        this.nodes = nodes;
        this.nodesMap = nodesMap;
        this.levels = levels;
    }

    public String getCareerName() {
        return careerName;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        int currentLevel = -1;

        builder.append(careerName);
        builder.append("\n");
        for (CourseNode node : nodes) {
            if (currentLevel != node.getLevel()) {
                currentLevel = node.getLevel();
                builder.append(currentLevel);
                builder.append("\n");
            }
            builder.append("  - ");
            builder.append(node);
            builder.append("		 ");
            builder.append(node.getRequirementCodes());
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public Iterator<CourseNode> iterator() {
        return new CurriculumIterator(nodes);
    }

    public int size() {
        return nodes.length;
    }

    public int levels() {
        return levels;
    }

    public CourseNode getNode(String courseCode) {
        final Integer position = nodesMap.get(courseCode);
        return (position != null) ? nodes[position] : null;
    }

    public CourseNode getNode(Course course) {
        return getNode(course.getCode());
    }

    public boolean contains(String courseCode) {
        return nodesMap.containsKey(courseCode);
    }

    public boolean contains(Course course) {
        return contains(course.getCode());
    }

    static CareerCurriculum newCurriculum(
        String careerName,
        CourseNode[] nodes,
        int levels
    ) {
        final Map<String, Integer> nodesMap = new HashMap<>();
        int i = 0;

        for (CourseNode node : nodes) {
            nodesMap.put(node.getCourse().getCode(), i);
            i++;
        }
        return new CareerCurriculum(careerName, nodes, nodesMap, levels);
    }

    private static final class CurriculumIterator implements Iterator<CourseNode> {
        private final CourseNode[] nodes;
        private int i;

        private CurriculumIterator(CourseNode[] nodes) {
            this.nodes = nodes;
            this.i = 0;
        }

        @Override
        public boolean hasNext() {
            return i < nodes.length;
        }

        @Override
        public CourseNode next() {
            return nodes[i++];
        }
    }
}
