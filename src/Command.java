import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Command {
    @NonNull private Operation operation;
    private int key;
    private String[] parameters;
}
