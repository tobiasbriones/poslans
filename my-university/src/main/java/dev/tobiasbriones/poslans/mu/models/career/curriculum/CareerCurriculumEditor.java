/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.models.career.curriculum;

import dev.tobiasbriones.poslans.mu.models.career.Course;

import java.util.*;

public final class CareerCurriculumEditor {
    private final List<Map<String, CourseNode>> levels;
    private final Map<String, Integer> addedCourseCodes;
    private final Set<String> requirementsForValidation;
    private int size;
    private String careerName;
    private CareerCurriculum curriculum;
    private int checkUntilThisLevel;

    public CareerCurriculumEditor() {
        this.levels = new ArrayList<>();
        this.addedCourseCodes = new HashMap<>();
        this.requirementsForValidation = new HashSet<>();
        this.careerName = "";
    }

    public CareerCurriculum getCurriculum() {
        return curriculum;
    }

    public void setCareerName(String careerName) {
        this.careerName = careerName;
    }

    public void addNode(Course course, List<Course> requirements)
        throws CourseDuplicationException,
               InvalidCourseRequirementException,
               CourseRequirementsDuplicationException,
               ClosedCourseRequirementException {
        if (addedCourseCodes.containsKey(course.getCode())) {
            throw new CourseDuplicationException(course);
        }
        final List<Course> requiredCourses = (requirements != null)
                                             ? requirements
                                             : new ArrayList<>();
        int level = 0;
        if (requirements != null) {
            for (Course requirement : requirements) {
                final Integer requirementLevel = addedCourseCodes.get(
                    requirement.getCode());
                if (requirementLevel == null) {
                    throw new InvalidCourseRequirementException(
                        course,
                        requirement
                    );
                }
                final CourseNode requirementNode = levels.get(requirementLevel)
                                                         .get(requirement.getCode());
                if (level <= requirementNode.getLevel()) {
                    level = requirementNode.getLevel() + 1;
                }
                if (requirementsForValidation.contains(requirement.getCode())) {
                    throw new CourseRequirementsDuplicationException(
                        course,
                        requirement
                    );
                }
                requirementsForValidation.add(requirement.getCode());
            }
            checkUntilThisLevel = level;
            for (Course requirement : requirements) {
                final int requirementLevel =
                    addedCourseCodes.get(requirement.getCode());
                final CourseNode requirementNode = levels.get(requirementLevel)
                                                         .get(requirement.getCode());
                if (!validateRequirement(requirementNode)) {
                    throw new ClosedCourseRequirementException(
                        course,
                        requirement
                    );
                }
                requirementNode.addNextCourse(course);
            }
        }
        if (level == levels.size()) {
            levels.add(new HashMap<>());
        }
        levels.get(level).put(
            course.getCode(),
            new CourseNode(level, course, requiredCourses)
        );
        addedCourseCodes.put(course.getCode(), level);
        requirementsForValidation.clear();
        checkUntilThisLevel = -1;
        size++;
    }

    public void addNode(Course course)
        throws CourseDuplicationException,
               InvalidCourseRequirementException,
               CourseRequirementsDuplicationException,
               ClosedCourseRequirementException {
        addNode(course, null);
    }

    public void build() {
        final CourseNode[] nodes = new CourseNode[size];
        int i = 0;
        int levelOnePosition = -1;

        for (Map<String, CourseNode> level : levels) {
            final SortedSet<String> keys = new TreeSet<String>(level.keySet());
            for (String key : keys) {
                nodes[i] = level.get(key);
                i++;
            }
            if (levelOnePosition == -1) {
                levelOnePosition = i;
            }
        }
        for (int k = levelOnePosition; k < nodes.length; k++) {
            final CourseNode node = nodes[k];
            for (Course requirement : node.getRequiredCourses()) {
                final int requirementLevel =
                    addedCourseCodes.get(requirement.getCode());
                final CourseNode requirementNode = levels.get(requirementLevel)
                                                         .get(requirement.getCode());
                requirementNode.addNextCourse(node.getCourse());
            }
        }
        curriculum = CareerCurriculum.newCurriculum(
            careerName,
            nodes,
            levels.size()
        );
    }

    private boolean validateRequirement(CourseNode node) {
        for (Course check : node.getNextCourses()) {
            if (requirementsForValidation.contains(check.getCode())) {
                return false;
            }
            System.out.println(check.getCode() + " - " + addedCourseCodes.get(
                check.getCode()));
            final int level = addedCourseCodes.get(check.getCode());
            if (level >= checkUntilThisLevel) {
                continue;
            }
            final CourseNode nextNode = levels.get(level).get(check.getCode());
            validateRequirement(nextNode);
        }
        return true;
    }
}
