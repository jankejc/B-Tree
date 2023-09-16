import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Stat {
    private Long recordsNumber;
    private Long readPages;
    private Long wrotePages;
    private Operation operation;
}
