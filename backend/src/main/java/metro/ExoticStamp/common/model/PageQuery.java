package metro.ExoticStamp.common.model;

public record PageQuery(int page, int size) {
    public static PageQuery of(int page, int size) {
        return new PageQuery(page, size);
    }
}
