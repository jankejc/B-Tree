import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Command {
    @NonNull private Operation operation;
    private Long key;
    private String[] parameters;

    public String getParametersToFile() {
        StringBuilder parametersToFileBuilder = new StringBuilder();
        for (String parameter : parameters) {
            parametersToFileBuilder.append(formatParameterToFile(parameter));
        }

        return parametersToFileBuilder.toString();
    }

    private String formatParameterToFile(String parameterString) {
        return String.format("%0" + Consts.DATA_FILE_PARAMETER_CHARS_NUMBER.getValue() + "d", Integer.parseInt(parameterString));
    }

    public static List<Command> getCommandsFromFile(String filePath) {
        FileManager fileManager = FileManager.getInstance();
        List<String> words = fileManager.getWordsFromFile(filePath);
        List<Command> commands = new ArrayList<>();

        Iterator<String> wordsIterator = words.iterator();
        while (wordsIterator.hasNext()) {
            Command command = null;
            switch (wordsIterator.next()) {
                case "a" -> {
                    command = new Command(Operation.ADD);
                    command.setKey(Long.parseLong(wordsIterator.next()));
                    String[] parameters = new String[Consts.PARAMETERS_NUMBER.getValue()];
                    for (int i = 0; i < Consts.PARAMETERS_NUMBER.getValue(); i++) {
                        parameters[i] = wordsIterator.next();
                    }
                    command.setParameters(parameters);
                }
                case "s" -> {
                    command = new Command(Operation.SEARCH);
                    command.setKey(Long.parseLong(wordsIterator.next()));
                }
                case "o" -> command = new Command(Operation.ORDERED_TRAVERSE);
                default -> {}
            }

            if (command != null) {
                commands.add(command);
            }
        }

        return commands;
    }
}
