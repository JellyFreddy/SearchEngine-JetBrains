package search;

import java.nio.file.Path;
import java.util.*;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner;
        //флаг --data позволяет обрабать ин-ию из файла с названием text.txt, расположенного в корневой директории прокекта
        if (args[0].equals("--data")) {
            Path pathToFile = Paths.get(args[1]);
            scanner = new Scanner(pathToFile.toAbsolutePath());
        } else {
            //также доступна обработка ин-ии, получаемой через стандартный ввод
            scanner = new Scanner(System.in);
        }
        Scanner systemScanner = new Scanner(System.in);
        try (scanner; systemScanner) {
            //статический метод данного класса, используется для заполнения статических полей (т.е. базы данных),
            // которые используются другими классами и методами в дальнейшем
            FindInformation.fillInPeople(scanner);
            //данный класс отвечает за вывод пользовательского меню и запускает методы класса FindInformation,
            // которые занимаются обработкой полученной ранее базы данных)
            Menu.execute(systemScanner);
        } catch (Exception ex) {
            throw ex;
        }
    }
}

class doSearch {
    static void print(SearchStrategy strategy, Map<String, ArrayList<Integer>> map, String[] keys, ArrayList<String> data) {
        strategy.search(map, keys, data);
    }
}

class ChooseStrategy {
    static SearchStrategy setStrategy;

    static SearchStrategy setStrategy(String strategy) throws Exception {
        if (strategy.equals("ANY")) {
            setStrategy = new SearchAny();
        } else if (strategy.equals("ALL")) {
            setStrategy = new SearchAll();
        } else if (strategy.equals("NONE")) {
            setStrategy = new SearchNone();
        } else {
            throw new Exception("Strategy not found");
        }
        return setStrategy;
    }
}

interface SearchStrategy {
    void search(Map<String, ArrayList<Integer>> map, String[] keys, ArrayList<String> data);
}

class SearchAll implements SearchStrategy {
    public void search(Map<String, ArrayList<Integer>> map, String[] keys, ArrayList<String> data) {
        int[] printableLines = new int[data.size()];
        int count = 0;
        for(String key:keys) {
            ArrayList<Integer> linesIndexes = map.get(key);
            if (linesIndexes == null)
                continue;
            for(int index:linesIndexes) {
                printableLines[index]++;
            }
        }
        for(int lineIndex = 0; lineIndex < data.size(); lineIndex++) {
            if (printableLines[lineIndex] == keys.length)
                count++;
        }
        if (count > 0)
            System.out.println(count + " persons found:");
        else
            System.out.println("No matching people found.");
        for(int lineIndex = 0; lineIndex < data.size(); lineIndex++) {
            if (printableLines[lineIndex] == keys.length)
                System.out.println(data.get(lineIndex));
        }
    }
}

class SearchAny implements SearchStrategy{
    public void search(Map<String, ArrayList<Integer>> map, String[] keys, ArrayList<String> data) {
        int[] printableLines = new int[data.size()];
        int count = 0;
        for(String key:keys) {
            ArrayList<Integer> linesIndexes = map.get(key);
            if (linesIndexes == null)
                continue;
            for(int index:linesIndexes) {
                printableLines[index]++;
            }
        }
        for(int lineIndex = 0; lineIndex < data.size(); lineIndex++) {
            if (printableLines[lineIndex] > 0)
                count++;
        }
        if (count > 0)
            System.out.println(count + " persons found:");
        else
            System.out.println("No matching people found.");
        for(int lineIndex = 0; lineIndex < data.size(); lineIndex++) {
            if (printableLines[lineIndex] > 0)
                System.out.println(data.get(lineIndex));
        }
    }
}

class SearchNone implements SearchStrategy{
    public void search(Map<String, ArrayList<Integer>> map, String[] keys, ArrayList<String> data) {
        int[] printableLines = new int[data.size()];
        int count = 0;
        for(String key:keys) {
            ArrayList<Integer> linesIndexes = map.get(key);
            if (linesIndexes == null)
                continue;
            for(int index:linesIndexes) {
                printableLines[index]++;
            }
        }
        for(int lineIndex = 0; lineIndex < data.size(); lineIndex++) {
            if (printableLines[lineIndex] == 0)
                count++;
        }
        if (count > 0)
            System.out.println(count + " persons found:");
        else
            System.out.println("No matching people found.");
        for(int lineIndex = 0; lineIndex < data.size(); lineIndex++) {
            if (printableLines[lineIndex] == 0)
                System.out.println(data.get(lineIndex));
        }
    }
}

class FindInformation {
    static int numberOfPeople;
    //в данном массиве содержатся непосредственно сами строки, входящие в базу данных
    static ArrayList<String> data = new ArrayList<>();
    //в данной карте используется инвертированный индекс (ключ - слово, значение номера стро, в которых оно содержится)
    //это позволяет ускорить поиск выводимых на экран строк
    //необходимо использовать именно TreeMap, потому что другие типы карт не позволяют обрабатывать ин-ию независимо от регистра
    static Map<String, ArrayList<Integer>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    protected static void fillInPeople(Scanner  scanner) {
        while(scanner.hasNext()) {
            //заполняем базу данных
            data.add(scanner.nextLine());
        }
        numberOfPeople = data.size();
        //заполняем карту с инвертированными индексами
        for(Integer i = 0; i < numberOfPeople; i++) {
            //каждая из строк разбивается на отдельные слова
            String[] words = data.get(i).split("\\s");
            for(String key:words) {
                ArrayList<Integer> array;
                //в случай, если такое ключевое слово уже встречалось, нам нужно будет добавить номер строки в соответствующий интовый массив
                //иначе нужно будет создать массив и только потом добавить в него элемент
                if (map.containsKey(key))
                    array = map.get(key);
                else
                    array = new ArrayList<>();
                array.add(i);
                map.put(key, array);
            }
        }
    }

    protected static void findInformation(Scanner  scanner) throws Exception {
        scanner.nextLine();
        System.out.println("Select a matching strategy: ALL, ANY, NONE\n");
        String strategy = scanner.nextLine();
        System.out.println("Enter a name or email to search all suitable people.");
        String[] keys = scanner.nextLine().split(" ");
        //в данном фрагменте программы используется архитектура "стратегия"
        //класс ChooseStrategy определяет какую именно стратегию мы будем использовать
        //Интерфейс SearchStrategy является базовым для конкретных классов, выполняющих поиск информации на основе определенных режимов
        SearchStrategy ourStrategy = ChooseStrategy.setStrategy(strategy);
        //doSearch вызывает конкретную реализацию поиска информации с помощью обзего для данных классов интерфейса SearchStrategy
        doSearch.print(ourStrategy, map, keys, data);
    }

    protected static void printAll() {
        System.out.println("\n=== List of people ===");
        for (int index = 0; index < numberOfPeople; index++) {
            System.out.println(data.get(index));
        }
    }
}

//отражение пользовательского меню и обработка взаимодействия пользователя с данным меню
class Menu {
    protected static void execute(Scanner scanner) throws Exception {
        System.out.println("=== Menu ===\n" +
                "1. Find a person\n" +
                "2. Print all people\n" +
                "0. Exit");
        int inputOption = scanner.nextInt();
        while (inputOption != 0) {
            if (inputOption != 1 && inputOption != 2) {
                System.out.println("\nIncorrect option! Try again.\n");
            }
            else if (inputOption == 1) {
                //запускается метод отвечающий за обработку запроса и вывод результатов
                FindInformation.findInformation(scanner);
            } else {
                FindInformation.printAll();
            }
            System.out.println("\n=== Menu ===\n" +
                    "1. Find a person\n" +
                    "2. Print all people\n" +
                    "0. Exit");
            inputOption = scanner.nextInt();
        }
        System.out.println("Bye!");
    }
}

