package fi.nls.dbquality.exception;

public class QualityException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public QualityException(Exception e) {
        super(e);
    }
}
