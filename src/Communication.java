import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Scanner;



@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Communication {

    private static Communication instance;
    private static final Scanner scanner = new Scanner(System.in);

    @Setter
    private long dataFileRecordPosition;
    @Setter
    private long indexFilePageNumberPosition;

    public static Communication getInstance() {
        if (instance == null) {
            instance = new Communication();
        }
        return instance;
    }

    public void closeScanner() {
        scanner.close();
    }

    public void welcome() {
        System.out.println("---------------------------------");
        System.out.println("Welcome in B-Tree simulator!");
    }

    public void somethingWentWrong(String cause) {
        System.out.println("---------------------------------");
        System.out.println("Something went wrong, program will be stopped. \nCAUSE:\n" + cause);
    }

    public void say(String message) {
        System.out.println(message);
    }

    public void sayDataFileRecordParameters(String parametersInString) {
        System.out.println(dataFileRecordPosition++ + ") " + parametersInString);
    }

    public void sayIndexFileNodePage(String page) {
        System.out.println(indexFilePageNumberPosition++ + ") " + page);
    }

    public RecordsSource whatRecordsSource() {
        System.out.println("---------------------------------");
        System.out.println(
                """
                        What records source do you want? (type the number)
                        1. Input commands MANUALLY.
                        2. Commands in the TEST FILE.
                        """
        );

        int choice = Integer.parseInt(scanner.nextLine());
        switch (choice) {
            case 1 -> {
                return RecordsSource.MANUALLY;
            }
            case 2 -> {
                return RecordsSource.TEST_FILE;
            }
            default -> {
                return RecordsSource.UNKNOWN;
            }
        }
    }

    public String testFilePath() {
        System.out.println("---------------------------------");
        System.out.println("What is the test file path?");
        return scanner.nextLine();
    }

    public Operation getOperation() {
        System.out.println("---------------------------------");
        while (true) {
            String input;
            System.out.println("What operation: (s)earch, (o)rdered traverse, (a)dd, (f)inish?");
            input = scanner.nextLine();
            switch (input) {
                case "s" -> {
                    return Operation.SEARCH;
                }
                case "o" -> {
                    return Operation.ORDERED_TRAVERSE;
                }
                case "a" -> {
                    return Operation.ADD;
                }
//                case "d" -> {
//                    return Operation.DELETE;
//                }
                case "f" -> {
                    return Operation.FINISH;
                }
                default -> say("'" + input + "' command is invalid.");
            }
        }
    }

    public Command whatToDo() {
        Operation operation = getOperation();
        Command command = new Command(operation);
        if (operation == Operation.ADD || operation == Operation.DELETE) {
            do {
                System.out.println(getTypeInKeyMessage());
                command.setKey(Long.parseLong(scanner.nextLine()));

            } while (ifKeyInRange(command.getKey()));

            String[] parameters;
            do {
                System.out.println(getTypeInParametersMessage());
                parameters = scanner.nextLine().split(" ");

            } while (!ifParametersCorrect(parameters));

            command.setParameters(parameters);

        } else if (operation == Operation.SEARCH) {
            do {
                System.out.println(getTypeInKeyMessage());
                command.setKey(Long.parseLong(scanner.nextLine()));

            } while (ifKeyInRange(command.getKey()));
        }

        return command;
    }

    public boolean whetherPrintFiles() {
        System.out.println("Show files? (y)es, (n)o");
        String input = scanner.nextLine();
        switch (input) {
            case "y" -> { return true; }
            case "n" -> { return false; }
            default -> {
                say("'" + input + "' command is invalid.");
                return false;
            }
        }
    }

    private String getTypeInKeyMessage() {
        return "Type in key [" +
                Consts.MIN_KEY.getValue() +
                ";" +
                Consts.MAX_KEY.getValue() +
                "]:";
    }

    private String getTypeInParametersMessage() {
        return "Type in " +
                Consts.PARAMETERS_NUMBER.getValue() +
                " whole parameters delimited by spaces in-between (ex. '" +
                Consts.MIN_PARAMETER.getValue() +
                " " +
                (Consts.MAX_PARAMETER.getValue() - 2) +
                " ... " +
                (Consts.MIN_PARAMETER.getValue() + 2) +
                " " +
                Consts.MAX_PARAMETER.getValue() +
                "'):";
    }

    private boolean ifKeyInRange(Long key) {
        return key < Consts.MIN_KEY.getValue() || key > Consts.MAX_KEY.getValue();
    }

    private boolean ifParametersCorrect(String[] parameters) {
        if (!(parameters.length == Consts.PARAMETERS_NUMBER.getValue())) {
            return false;
        }

        for (String parameter : parameters) {
            if (ifParameterInRange(Integer.parseInt(parameter))) {
                return false;
            }
        }

        return true;
    }

    private boolean ifParameterInRange(int parameter) {
        return parameter < Consts.MIN_PARAMETER.getValue() || parameter > Consts.MAX_PARAMETER.getValue();
    }
}

