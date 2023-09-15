import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Record {
    private Long nodePagePositionInIndex = (long) Consts.FILE_NULL.getValue();
    private Long leftChildPositionInIndex = (long) Consts.FILE_NULL.getValue();
    private Long key = (long) Consts.FILE_NULL.getValue();
    private Long positionInDataFile = (long) Consts.FILE_NULL.getValue();
    private Long rightChildPositionInIndex = (long) Consts.FILE_NULL.getValue();

    public Record(Long nodePagePositionInIndex) {
        this.nodePagePositionInIndex = nodePagePositionInIndex;
    }

    public Record(Long key, Long positionInDataFile) {
        this.key = key;
        this.positionInDataFile = positionInDataFile;
    }
}
