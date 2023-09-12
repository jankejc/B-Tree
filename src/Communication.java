import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Scanner;


// TODO: remove useless methods

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Communication {

    private static Communication instance;
    private static Scanner scanner = new Scanner(System.in);

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

    public RecordsSource whatRecordsSource() {
        System.out.println("---------------------------------");
        System.out.println(
                """
                        What records source do you want? (type the number)
                        1. Put records manually.
                        2. Records are in the file.
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

    public Long recordsNumber() {
        System.out.println("---------------------------------");
        System.out.println("How many records?");

        return Long.parseLong(scanner.nextLine());
    }

    public String testFilePath() {
        System.out.println("---------------------------------");
        System.out.println("What is the test file path?");
        return scanner.nextLine();
    }

    public boolean whetherStartSort() {
        System.out.println("---------------------------------");
        System.out.println("Start sort? (type: 'yes' or 'no')");
        String input = scanner.nextLine();
        switch (input) {
            case "yes" -> {
                return true;
            }
            case "no" -> {
                return false;
            }
            default -> {
                say("'" + input + "' command is invalid.");
                return false;
            }
        }
    }

    public boolean whetherPrintFile() {
        System.out.println("Show file? (type: 'yes' or 'no')");
        String input = scanner.nextLine();
        switch (input) {
            case "yes" -> {
                return true;
            }
            case "no" -> {
                return false;
            }
            default -> {
                say("'" + input + "' command is invalid.");
                return false;
            }
        }
    }

    public Operation getOperation() {
        System.out.println("---------------------------------");
        while (true) {
            String input;
            System.out.println("What operation: (a)dd, (d)elete, (f)inish?");
            input = scanner.nextLine();
            switch (input) {
                case "a" -> {
                    return Operation.ADD;
                }
                case "d" -> {
                    return Operation.DELETE;
                }
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
        if (operation == Operation.FINISH) {
            return command;
        }

        do {
            System.out.println(getTypeInKeyMessage());
            command.setKey(Integer.parseInt(scanner.nextLine()));

        } while (ifKeyInRange(command.getKey()));

        String[] parameters;
        do {
            System.out.println(getTypeInParametersMessage());
            parameters = scanner.nextLine().split(" ");

        } while (!ifParametersCorrect(parameters));

        command.setParameters(parameters);

        return command;
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
                Consts.PARAMETER_NUMBERS.getValue() +
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

    private boolean ifKeyInRange(int key) {
        return key < Consts.MIN_KEY.getValue() || key > Consts.MAX_KEY.getValue();
    }

    private boolean ifParametersCorrect(String[] parameters) {
        if (!(parameters.length == Consts.PARAMETER_NUMBERS.getValue())) {
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

