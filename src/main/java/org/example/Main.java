package org.example;

import java.io.*;
import java.util.*;

public class Main {

    static int numberOfColumns = 0;
    static List<Set<Line>> groupsOfLines = new ArrayList<>();
    static List<Map<String, Set<Line>>> columns = new ArrayList<>();

    public static void main(String[] args) {
        long beginTime = System.currentTimeMillis();
        String fileName = args[0];
        Set<Line> parsedLines = readFile(fileName);
        for (int i = 0; i < numberOfColumns; i++) {
            columns.add(createHashMapForColumnOfValues(parsedLines, i));
        }
        for (Line line : parsedLines) {
            Set<Line> groupForLine = findGroupForLine(line);
            if (!groupForLine.isEmpty()) {
                groupsOfLines.add(groupForLine);
            }
        }
        groupsOfLines.sort((o1, o2) -> o2.size() - o1.size());
        groupsOfLines.removeIf(group -> group.size() <= 1);
        long endTime = System.currentTimeMillis();
        double workTime = (endTime - beginTime) / 1000.0;
        writeResultToFile(workTime);
    }

    static Set<Line> readFile(String fileName) {
        Set<Line> lines = new HashSet<>(1000000);
        try (FileReader reader = new FileReader(fileName);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (isLineValid(line)) {
                    lines.add(new Line(splitLine(line)));
                }
            }
        } catch (IOException e) {
            System.out.println("Invalid file name");
        }
        return lines;
    }

    static boolean isLineValid(String line) {
        int quotesCounter = 0;
        int lastSemicolonIndex = -1;
        int semicolonNumber = 0;
        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);
            if (currentChar == '"') {
                quotesCounter++;
                if (quotesCounter > 2) {
                    return false;
                }
            } else if (currentChar == ';') {
                semicolonNumber++;
                if (quotesCounter == 2 || i - 1 == lastSemicolonIndex) {
                    quotesCounter = 0;
                    lastSemicolonIndex = i;
                } else {
                    return false;
                }
            }
        }
        countNumberOfColumns(semicolonNumber);
        return true;
    }

    static void countNumberOfColumns(int semicolonNumber) {
        if (numberOfColumns < semicolonNumber + 1) {
            numberOfColumns = semicolonNumber + 1;
        }
    }

    static List<String> splitLine(String line) {
        List<String> values = new ArrayList<>();
        int lastIndexOfAfterSemicolon = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ';') {
                values.add(line.substring(lastIndexOfAfterSemicolon, i));
                lastIndexOfAfterSemicolon = i + 1;
            }
            if (i == line.length() - 1) {
                values.add(line.substring(lastIndexOfAfterSemicolon, line.length()));
            }
        }
        return values;
    }

    static Map<String, Set<Line>> createHashMapForColumnOfValues(Set<Line> lines, int columnNumber) {
        Map<String, Set<Line>> linesWithSameValue = new HashMap<>();
        for (Line line : lines) {
            if (line.getValues().size() > columnNumber) {
                String value = line.getValues().get(columnNumber);
                if (!value.isEmpty()) {
                    if (linesWithSameValue.containsKey(value)) {
                        linesWithSameValue.get(value).add(line);
                    } else {
                        Set<Line> group = new HashSet<>();
                        group.add(line);
                        linesWithSameValue.put(value, group);
                    }
                }
            }
        }
        // если нет парных значений, то удаляем из колонки
        Iterator<Map.Entry<String, Set<Line>>> iterator = linesWithSameValue.entrySet().iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getValue().size() == 1) {
                    iterator.remove();
            }
        }
        return linesWithSameValue;
    }

    private static Set<Line> findGroupForLine(Line line) {
        Set<Line> groupForLine = new HashSet<>();
        for (int i = 0; i < line.getValues().size(); i++) {
            String value = line.getValues().get(i);
            if (!value.isEmpty() && !value.equals("\"\"")) {
                Map<String, Set<Line>> column = columns.get(i);
                if (column.get(value) != null) {
                    Set<Line> group = column.remove(value);
                    groupForLine.addAll(group);
                    for (Line lineInner : group) {
                        groupForLine.addAll(findGroupForLine(lineInner));
                    }
                }
            }
        }
        return groupForLine;
    }

    static void writeResultToFile(double workTime) {
        try (FileWriter writer = new FileWriter("./lngOutput.txt", false);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.write("Количество групп с более чем одним элементом: " + groupsOfLines.size() + "\n");
            bufferedWriter.write("Время выполнения программы: " + workTime + " сек.\n");

            int groupCounter = 1;

            for (Set<Line> groupOfLines : groupsOfLines) {
                bufferedWriter.write("Группа " + groupCounter + "\n");
                for (Line line : groupOfLines) {
                    for (int i = 0; i < line.getValues().size(); i++) {
                        bufferedWriter.write(line.getValues().get(i));
                        if (i != line.getValues().size() - 1) {
                            bufferedWriter.write(";");
                        } else {
                            bufferedWriter.write("\n");
                        }
                    }
                }
                groupCounter++;
            }

        } catch (IOException e) {
            System.out.println("Output exception");
        }
    }

    private static class Line {

        private List<String> values;

        public Line(List<String> values) {
            this.values = values;
        }

        public List<String> getValues() {
            return values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Line line)) return false;
            if (values.size() != line.values.size()) return false;
            for (int i = 0; i < values.size(); i++) {
                if (!values.get(i).equals(line.values.get(i))) return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hashCode = 0;
            int multiplier = 31;
            for (String value : values) {
                hashCode += multiplier * value.hashCode();
                multiplier += 17;
            }
            return hashCode;
        }
    }
}