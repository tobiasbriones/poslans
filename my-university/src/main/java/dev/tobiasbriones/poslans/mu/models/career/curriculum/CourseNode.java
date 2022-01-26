/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.models.career.curriculum;

import dev.tobiasbriones.poslans.mu.models.career.Course;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a node for the {@link CareerCurriculum} type.
 *
 * @author Tobias Briones
 */
public final class CourseNode {
    private final int level;
    private final Course course;
    private final List<Course> requiredCourses;
    private final List<Course> nextCourses;
    private final String requirementCodes;

    // Called by CareerCurriculum
    CourseNode(
        int level,
        Course course,
        List<Course> requiredCourses,
        List<Course> nextCourses,
        String requirementCodes
    ) {
        this.level = level;
        this.course = course;
        this.requiredCourses = requiredCourses;
        this.nextCourses = nextCourses;
        this.requirementCodes = requirementCodes;
    }

    // Called by CareerCurriculumEditor
    CourseNode(int level, Course course, List<Course> requiredCourses) {
        this(
            level,
            course,
            requiredCourses,
            new ArrayList<>(),
            getCodes(requiredCourses)
        );
    }

    public int getLevel() {
        return level;
    }

    public Course getCourse() {
        return course;
    }

    public List<Course> getRequiredCourses() {
        return requiredCourses;
    }

    public List<Course> getNextCourses() {
        return nextCourses;
    }

    public String getRequirementCodes() {
        return requirementCodes;
    }

    public boolean isTopLevel() {
        return level == 0;
    }

    public boolean isEndLevel() {
        return nextCourses.isEmpty();
    }

    JSONArray getRequiredCoursesJSONArray() {
        return getJSONArray(requiredCourses);
    }

    JSONArray getNextCoursesJSONArray() {
        return getJSONArray(nextCourses);
    }

    @Override
    public String toString() {
        return course.toString();
    }

    public boolean isRequirement(Course requirement) {
        return requiredCourses.contains(requirement);
    }

    void addNextCourse(Course next) {
        nextCourses.add(next);
    }

    private static String getCodes(List<Course> requiredCourses) {
        final StringBuilder builder = new StringBuilder();
        for (Course requirement : requiredCourses) {
            builder.append(requirement.getCode());
            builder.append(" ");
        }
        return builder.toString();
    }

    private static JSONArray getJSONArray(List<Course> courses) {
        final JSONArray array = new JSONArray();
        if (courses == null) {
            return array;
        }
        for (Course course : courses) {
            array.put(course.getId());
        }
        return array;
    }
}
