package ru.java.project;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        Parser parser = new Parser();

        Map<Class<?>, List<Object>> result = parser.parse("file.xlsx");
        result.forEach((clazz, objects) -> {
            if (clazz.equals(Individual.class)) {
                List<Individual> employees = objects.stream()
                        .filter(Individual.class::isInstance) // Filter only Employee instances
                        .map(Individual.class::cast)
                        .collect(Collectors.toList());

                // Имя и фамилия сотрудников, которым меньше 20 лет
                employees
                        .stream()
                        .filter(e -> e.getAge() > 20)
                        .forEach(System.out::println);
            }

            // Количество компаний среди сотрудников
            if (clazz.equals(Company.class)) {
                System.out.println(objects.size());
            }
        });
    }
}
