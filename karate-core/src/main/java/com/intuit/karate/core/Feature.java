/*
 * The MIT License
 *
 * Copyright 2018 Intuit Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.intuit.karate.core;

import com.intuit.karate.Resource;
import com.intuit.karate.StringUtils;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author pthomas3
 */
public class Feature {

    public static final String KEYWORD = "Feature";

    private final Resource resource;

    private int line;
    private List<Tag> tags;
    private String name;
    private String description;
    private Background background;
    private List<FeatureSection> sections = new ArrayList();

    private List<String> lines;

    private String callTag;
    private String callName;
    private int callLine = -1;

    public Feature(Resource resource) {
        this.resource = resource;
    }

    public boolean isBackgroundPresent() {
        return background != null && background.getSteps() != null;
    }

    public String getNameAndDescription() {
        String temp = "";
        if (!StringUtils.isBlank(name)) {
            temp = temp + name;
        }
        if (!StringUtils.isBlank(description)) {
            if (!temp.isEmpty()) {
                temp = temp + " | ";
            }
            temp = temp + description;
        }
        return temp;
    }

    public String getNameForReport() {
        if (name == null) {
            return "[" + resource.getFileNameWithoutExtension() + "]";
        } else {
            return "[" + resource.getFileNameWithoutExtension() + "] " + name;
        }
    }

    public Step findStepByLine(int line) {
        for (FeatureSection section : sections) {
            List<Step> steps = section.isOutline()
                    ? section.getScenarioOutline().getSteps() : section.getScenario().getStepsIncludingBackground();
            for (Step step : steps) {
                if (step.getLine() == line) {
                    return step;
                }
            }
        }
        return null;
    }

    public Iterator<ScenarioExecutionUnit> getScenarioExecutionUnits(ExecutionContext exec) {
        return new FeatureScenarioIterator(exec, sections.iterator());
    }

    public void addSection(FeatureSection section) {
        section.setIndex(sections.size());
        sections.add(section);
    }

    public FeatureSection getSection(int sectionIndex) {
        return sections.get(sectionIndex);
    }

    public Scenario getScenario(int sectionIndex, int scenarioIndex) {
        FeatureSection section = getSection(sectionIndex);
        if (scenarioIndex == -1) {
            return section.getScenario();
        }
        ScenarioOutline outline = section.getScenarioOutline();
        return outline.getScenarios().get(scenarioIndex);
    }

    public Step getStep(int sectionIndex, int scenarioIndex, int stepIndex) {
        Scenario scenario = getScenario(sectionIndex, scenarioIndex);
        List<Step> steps = scenario.getSteps();
        if (stepIndex == -1 || steps.isEmpty() || steps.size() <= stepIndex) {
            return null;
        }
        return steps.get(stepIndex);
    }

    public Feature replaceStep(Step step, String text) {
        return replaceLines(step.getLine(), step.getEndLine(), text);
    }

    public Feature replaceLines(int start, int end, String text) {
        for (int i = start - 1; i < end - 1; i++) {
            lines.remove(start);
        }
        lines.set(start - 1, text);
        return replaceText(getText());
    }

    public Feature addLine(int index, String line) {
        lines.add(index, line);
        return replaceText(getText());
    }

    public String getText() {
        initLines();
        return joinLines();
    }

    public void initLines() {
        if (lines == null) {
            if (resource != null) {
                lines = StringUtils.toStringLines(resource.getAsString());
            }
        }
    }

    public String joinLines(int startLine, int endLine) {
        initLines();
        StringBuilder sb = new StringBuilder();
        if (endLine > lines.size()) {
            endLine = lines.size();
        }
        for (int i = startLine; i < endLine; i++) {
            String temp = lines.get(i);
            sb.append(temp).append("\n");
        }
        return sb.toString();
    }

    public String joinLines() {
        int lineCount = lines.size();
        return joinLines(0, lineCount);
    }

    public Feature replaceText(String text) {
        return FeatureParser.parseText(this, text);
    }

    public String getCallTag() {
        return callTag;
    }

    public void setCallTag(String callTag) {
        this.callTag = callTag;
    }

    public String getCallName() {
        return callName;
    }

    public void setCallName(String callName) {
        this.callName = callName;
    }

    public int getCallLine() {
        return callLine;
    }

    public void setCallLine(int callLine) {
        this.callLine = callLine;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public Resource getResource() {
        return resource;
    }

    public Path getPath() {
        return resource == null ? null : resource.getPath();
    }

    public String getRelativePath() {
        return resource == null ? null : resource.getRelativePath();
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public List<FeatureSection> getSections() {
        return sections;
    }

    public void setSections(List<FeatureSection> sections) {
        this.sections = sections;
    }

    @Override
    public String toString() {
        return resource.toString();
    }

}
