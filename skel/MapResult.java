import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MapResult {

    // store the partial dictionary
    private final ConcurrentHashMap<Integer, Integer> dict;
    // store the partial maximalList
    private final List<String> maximalList;
    // documentId
    private final Integer id;

    public MapResult(Integer id) {
        this.dict = new ConcurrentHashMap<>();
        this.maximalList = Collections.synchronizedList(new ArrayList<>());
        this.id = id;
    }

    public ConcurrentHashMap<Integer, Integer> getDict() {
        return dict;
    }

    public List<String> getMaximalList() {
        return maximalList;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "dict=" + dict +
                " maximalList=" + maximalList +
                " id=" + id + "\n";
    }
}
